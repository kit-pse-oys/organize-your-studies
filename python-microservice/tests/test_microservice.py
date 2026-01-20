#!/usr/bin/env python3
"""
Unit-Tests für den Microservice.

Diese Komponente testet den Microservice und dessen funktionen/Klassen
"""

__author__ = "Nardi Hyseni"
__copyright__ = "Copyright 2026, PSE Projektgruppe Organize Your Studies"
__credits__ = ["Nardi Hyseni"]
__version__ = "1.0.1"
__email__ = "uhxch@student.kit.edu"

import unittest
# WICHTIG: TestClient von FastAPI importieren
from fastapi.testclient import TestClient
from Microservice import DataTransformer, COPSolver, app

class TestMicroservice(unittest.TestCase):
    """
    Haupt-Testklasse. Führt Tests isoliert voneinander aus.
    """
    def setUp(self):
        """
        Wird VOR jedem einzelnen Test ausgeführt.
        """
        self.base_data = {
            "horizon": 2016,
            "current_slot": 0,
            "tasks": [],
            "fixed_blocks": [],
            "blocked_days": [],
            "preference_time": "neutral"
        }
        # HIER IST DIE LÖSUNG:
        # Der TestClient nimmt deine 'app' und erlaubt normale Requests ohne async-Stress.
        self.client = TestClient(app)

    def test_data_tranformer(self):
        """
        Testet, ob der DataTransformer die Solver-Ergebnisse korrekt in eine Liste umwandelt.
        """
        class MockSolver:
            def Value(self, var):
                return 100

        mock_solver = MockSolver()

        fake_var ={
            "test_task":{"start":"Mock-object","duration":100}
        }

        result = DataTransformer.format_solution(mock_solver, fake_var)

        self.assertEqual(result[0]['start'],10)
        self.assertEqual(result[0]['end'],200)
        self.assertEqual(len(result),1)



    def test_assertNoOverlap(self):
        """
        Testet, ob der Solver verhindert, dass sich zwei Aufgaben überschneiden.
        """
        self.base_data["tasks"] = [
            {"id": "test_1", "duration": 50, "deadline": 300},
            {"id": "test_2", "duration": 50, "deadline": 300}
        ]

        solver = COPSolver(self.base_data)
        solver.build_model()
        solution = solver.solve()

        # Check, ob überhaupt eine Lösung da ist, sonst crasht Value()
        if not solution:
            self.fail("Sollte eine Lösung finden, hat aber keine gefunden.")

        start_1 = solution.Value(solver.solution_map["test_1"]["start"])
        start_2 = solution.Value(solver.solution_map["test_2"]["start"])

        self.assertNotEqual(start_1, start_2, "Aufgaben ueberlappen sich (gleicher Start)")

        # Logik-Korrektur: Task 1 endet vor Task 2 ODER Task 2 endet vor Task 1
        no_overlap = (start_1 + 50 <= start_2) or (start_2 + 50 <= start_1)
        self.assertTrue(no_overlap, "Aufgaben ueberlappen sich")


    def test_assertImpossibleDeadline(self):
        """
        Testet, ob der Solver 'None' zurückgibt, wenn eine Aufgabe unmöglich zu schaffen ist.
        """
        self.base_data["tasks"] = [
            {"id": "test_1", "duration": 50, "deadline":10},
        ]

        solver = COPSolver(self.base_data)
        solver.build_model()
        solution = solver.solve()
        self.assertIsNone(solution,"Es gibt eine unmoegliche Loesung")


    def test_assertHappyPath(self):
        """
        Testet den Standardfall (Happy Path): Eine lösbare Aufgabe.
        """
        self.base_data["tasks"].append({
            "id": "HappyTask",
            "duration": 50,
            "deadline": 150,
        })

        solver = COPSolver(self.base_data)
        solver.build_model()
        solution = solver.solve()
        self.assertIsNotNone(solution,"Es sollte eine Loesung geben")

    def test_api_endpoint(self):
        """
        INTEGRATIONSTEST:
        Viel einfacher mit TestClient!
        """
        self.base_data["tasks"].append({
            "id": "api_task",
            "duration": 20,
            "deadline": 500
        })

        # Kein 'await', kein 'asyncio.run'. Einfach aufrufen:
        response = self.client.post('/optimize', json=self.base_data)

        self.assertEqual(response.status_code, 200, "API sollte mit Status 200 antworten")

        # .json() statt .get_json()
        response_data = response.json()

        self.assertIsInstance(response_data, list, "Antwort sollte eine JSON-Liste sein")
        self.assertEqual(len(response_data), 1, "Sollte genau eine geplante Aufgabe zurückgeben")
        self.assertEqual(response_data[0]['id'], "api_task")

if __name__ == '__main__':
    unittest.main()