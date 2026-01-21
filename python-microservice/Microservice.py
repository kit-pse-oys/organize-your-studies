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
        horizon = self.data.get('horizon', 2016)
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

        slots_per_day = 288
        morning_end = 72
        evening_start = 264

        for day in range(7):
            offset = day * slots_per_day

            if day in blocked_days:
                block_int = self.model.NewIntervalVar(offset, slots_per_day, offset + slots_per_day,
                                                      f"BlockedDay_{day}")
                all_intervals.append(block_int)

                continue

            night_a = self.model.NewIntervalVar(offset, morning_end, offset + morning_end, f"NightA_d{day}")
            all_intervals.append(night_a)

            night_b_duration = slots_per_day - evening_start
            night_b = self.model.NewIntervalVar(offset + evening_start, night_b_duration, offset + slots_per_day,
                                                f"NightB_d{day}")

            all_intervals.append(night_b)

        for task in tasks:
            t_id = task['id']
            duration = task['duration']
            deadline = task.get('deadline', horizon)

            min_start = max(0, current_slot)

            start_var = self.model.NewIntVar(min_start, horizon - duration, f'start_{t_id}')
            end_var = self.model.NewIntVar(min_start + duration, horizon, f'end_{t_id}')

            self.model.Add(end_var <= deadline)

            interval = self.model.NewIntervalVar(start_var, duration, end_var, f'interval_{t_id}')
            all_intervals.append(interval)

            self.solution_map[t_id] = {'start': start_var, 'duration': duration}

            cost_array = [0] * (horizon + 1)

            selected_prefs = [p.strip() for p in pref_time_string.split(',')]

            time_windows = []
            if "morgens" in selected_prefs:      time_windows.append((6, 9))
            if "vormittags" in selected_prefs:   time_windows.append((9, 12))
            if "mittags" in selected_prefs:      time_windows.append((12, 15))
            if "nachmittags" in selected_prefs:  time_windows.append((15, 18))
            if "abends" in selected_prefs:       time_windows.append((18, 22))

            for (h_start, h_end) in time_windows:
                for day in range(7):
                    s_slot = (day * 24 + h_start) * 12
                    e_slot = (day * 24 + h_end) * 12
                    for t in range(s_slot, e_slot):
                        if t < horizon:
                            cost_array[t] += -10

            if 'costs' in task:
                for c in task['costs']:
                    t_idx = c['t']
                    cost_val = c['c']
                    if 0 <= t_idx < horizon:
                        cost_array[t_idx] += cost_val

            cost_var = self.model.NewIntVar(-1000000, 1000000, f'cost_{t_id}')

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

        solver.parameters.max_time_in_seconds = 4.0

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
    uvicorn.run(app, host="0.0.0.0", port=5001)
