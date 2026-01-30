#!/usr/bin/env python3
"""
Microservice für die App Organize Your Studies.

Diese Komponente implementiert einen Constraint-Optimization-Solver (COP)
unter Verwendung von Google OR-Tools, um Lernpläne zu erstellen.
Der Service stellt eine REST-API bereit und er berücksichtigt Deadlines, Nachtruhe, geblockte Tage und Präferenzen.
"""
# CI pipline test
__author__ = "Nardi Hyseni"
__copyright__ = "Copyright 2026, PSE Projektgruppe Organize Your Studies"
__credits__ = ["Nardi Hyseni", "Dav Debler"]
__version__ = "1.0.2"
__email__ = "uhxch@student.kit.edu"

import json
import os

import uvicorn
from fastapi import FastAPI, Body
from ortools.sat.python import cp_model

SLOT_DURATION_MINUTES = 5
MINUTES_PER_HOUR = 60
HOURS_PER_DAY = 24
DAYS_PER_WEEK = 7

SLOTS_PER_HOUR = MINUTES_PER_HOUR // SLOT_DURATION_MINUTES
SLOTS_PER_DAY = HOURS_PER_DAY * SLOTS_PER_HOUR
DEFAULT_HORIZON = DAYS_PER_WEEK * SLOTS_PER_DAY

HOUR_MORNING_END = 6
HOUR_EVENING_START = 22

SLOT_MORNING_END = HOUR_MORNING_END * SLOTS_PER_HOUR
SLOT_EVENING_START = HOUR_EVENING_START * SLOTS_PER_HOUR

SOLVER_TIME_LIMIT_SECONDS = 4.0
COST_BONUS_PREFERENCE = -10
COST_MIN_BOUND = -1000000
COST_MAX_BOUND = 1000000

KEY_MORNING = "MORNING"
KEY_FORENOON = "FORENOON"
KEY_NOON = "NOON"
KEY_AFTERNOON = "AFTERNOON"
KEY_EVENING = "EVENING"

SERVER_HOST = "0.0.0.0"
SERVER_PORT = 5002


# Component DataTransformer

class DataTransformer:
    """
    Verantwirtlich für das laden, valedieren und formatieren der Input-Daten.
    """

    @staticmethod
    def load_json_file(file_path):
        """
        Lädt eine JSON-Datei sicher von der Festplatte.

        Args:
            file_path (str): Der Pfad zur Eingabedatei.

        Returns:
            dict: Der Inhalt der JSON-Datei als Dictionary.

        Raises:
            FileNotFoundError: Wenn die Datei nicht existiert.
            ValueError: Wenn das JSON-Format ungültig ist.
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"Input file not found:{file_path}")

        with open(file_path, 'r', encoding='utf-8') as f:
            try:
                return json.load(f)
            except json.decoder.JSONDecodeError as e:
                raise ValueError(f"Invalid JSON forat:{str(e)}")

    @staticmethod
    def format_solution(solver, task_vars):
        """
        Nimmt die Lösungen aus dem Solver und wandelt diese in eine API konforme Liste um.

        Args:
            solver (cp_model.CpSolver): Der Solver nach erfolgreicher Berechnung.
            task_vars (dict): Mapping von Task-IDs zu den Solver-Variablen (start, duration).
        :return:
            list: Liste von Dictionaries mit 'id', 'start' und 'end' für jede Aufgabe.
        """
        solution_list = []

        for t_id, vars in task_vars.items():
            duration = vars['duration']

            start_val = solver.Value(vars['start'])
            end_val = start_val + duration

            solution_list.append({
                'id': t_id,
                'start': start_val,
                'end': end_val,
            })

        return solution_list


class COPSolver:
    """
    Kernkomponente für die Planung.
    Erstellt das Constraint-Programming-Modell und führt die Optimierung durch.
    """

    def __init__(self, data):
        """
        Initialisiert den Solver mit den Eingabedaten.

        Args:
            data (dict): Das JSON-Dictionary mit Tasks, Constraints und Einstellungen.
        """
        self.data = data
        self.model = cp_model.CpModel()
        self.solution_map = {}

    def build_model(self):
        """
        Konstruiert das Modell.

        Definiert:
        1. Zeitintervalle für alle Aufgaben.
        2. Erstellt Blockaden für Nachtruhe, feste Termine und freie Tage.
        3. Constraints (keine Überlappung, Deadlines).
        4. Zielfunktion (Minimierung der Kosten basierend auf Präferenzen).
        """
        horizon = self.data.get('horizon', DEFAULT_HORIZON)
        current_slot = self.data.get('current_slot', 0)
        tasks = self.data.get('tasks', [])
        fixed_blocks = self.data.get('fixed_blocks', [])
        blocked_days = self.data.get("blocked_days", [])
        pref_time_string = self.data.get('preference_time', '')

        all_intervals = []
        all_cost_terms = []

        for block in fixed_blocks:
            start = block['start']
            duration = block['duration']

            fixed_int = self.model.NewIntervalVar(start, duration, start + duration, f"Block_{start}")
            all_intervals.append(fixed_int)

        for day in range(DAYS_PER_WEEK):
            offset = day * SLOTS_PER_DAY

            if day in blocked_days:
                block_int = self.model.NewIntervalVar(offset, SLOTS_PER_DAY, offset + SLOTS_PER_DAY,
                                                      f"BlockedDay_{day}")
                all_intervals.append(block_int)

                continue

            night_a = self.model.NewIntervalVar(offset, SLOT_MORNING_END, offset + SLOT_MORNING_END, f"NightA_d{day}")
            all_intervals.append(night_a)

            night_b_duration = SLOTS_PER_DAY - SLOT_EVENING_START
            night_b = self.model.NewIntervalVar(offset + SLOT_EVENING_START, night_b_duration, offset + SLOTS_PER_DAY,
                                                f"NightB_d{day}")

            all_intervals.append(night_b)

        for task in tasks:
            t_id = task['id']
            duration = task['duration']
            deadline = task.get('deadline', horizon)
            start = task.get('start', 0)

            min_start = max(0, current_slot, start)

            start_var = self.model.NewIntVar(min_start, horizon - duration, f'start_{t_id}')
            end_var = self.model.NewIntVar(min_start + duration, horizon, f'end_{t_id}')

            self.model.Add(end_var <= deadline)

            interval = self.model.NewIntervalVar(start_var, duration, end_var, f'interval_{t_id}')
            all_intervals.append(interval)

            self.solution_map[t_id] = {'start': start_var, 'duration': duration}

            cost_array = [0] * (horizon + 1)

            selected_prefs = [p.strip() for p in pref_time_string.split(',')]

            time_windows = []
            if KEY_MORNING in selected_prefs:      time_windows.append((6, 9))
            if KEY_FORENOON in selected_prefs:   time_windows.append((9, 12))
            if KEY_NOON in selected_prefs:      time_windows.append((12, 15))
            if KEY_AFTERNOON in selected_prefs:  time_windows.append((15, 18))
            if KEY_EVENING in selected_prefs:       time_windows.append((18, 22))

            for (h_start, h_end) in time_windows:
                for day in range(DAYS_PER_WEEK):
                    s_slot = (day * HOURS_PER_DAY + h_start) * SLOTS_PER_HOUR
                    e_slot = (day * HOURS_PER_DAY + h_end) * SLOTS_PER_HOUR
                    for t in range(s_slot, e_slot):
                        if t < horizon:
                            cost_array[t] += COST_BONUS_PREFERENCE

            if 'costs' in task:
                for c in task['costs']:
                    t_idx = c['t']
                    cost_val = c['c']
                    if 0 <= t_idx < horizon:
                        cost_array[t_idx] += cost_val

            cost_var = self.model.NewIntVar(COST_MIN_BOUND, COST_MAX_BOUND, f'cost_{t_id}')

            self.model.AddElement(start_var, cost_array, cost_var)

            all_cost_terms.append(cost_var)

        self.model.AddNoOverlap(all_intervals)

        self.model.Minimize(sum(all_cost_terms))

    def solve(self):
        """
        Führt den Solver aus.

        Returns:
            cp_model.CpSolver: Das Solver-Objekt, wenn eine Lösung (Optimal oder Feasible) gefunden wurde.
            None: Wenn keine Lösung möglich ist (INFEASIBLE) oder ein Fehler auftrat.
        """
        solver = cp_model.CpSolver()

        solver.parameters.max_time_in_seconds = SOLVER_TIME_LIMIT_SECONDS

        status = solver.Solve(self.model)

        if status == cp_model.OPTIMAL or status == cp_model.FEASIBLE:
            return solver
        else:
            return None


app = FastAPI(title="Microservice Organize Your Studies")


@app.post("/optimize")
async def optimize(data: dict = Body(...)):
    """
    Empfängt die Daten als JSON-Body (dafür sorgt 'Body(...)').
    """
    # Die Zeile 'data = await request.json()' BRAUCHEN WIR NICHT MEHR!
    # 'data' ist jetzt automatisch schon das fertige Dictionary.

    print(f"--> DEBUG: Neue Anfrage empfangen! ({len(data.get('tasks', []))} Tasks)")

    solver_instance = COPSolver(data)
    solver_instance.build_model()
    solution = solver_instance.solve()

    if solution:
        result = DataTransformer.format_solution(solution, solver_instance.solution_map)
        print(f"--> DEBUG: Lösung gefunden, sende {len(result)} Einträge zurück.")
        return result
    else:
        print("--> DEBUG: Keine Lösung möglich.")
        return []


# --- 4. Server Starten ---
if __name__ == '__main__':
    # FastAPI braucht Uvicorn als Server
    uvicorn.run(app, host=SERVER_HOST, port=SERVER_PORT)
