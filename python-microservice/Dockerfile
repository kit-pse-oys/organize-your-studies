# 1. Wir nutzen Python 3.11 (sehr stabil für OR-Tools)
FROM python:3.11-slim

# 2. Arbeitsordner im Container
WORKDIR /app

# 3. System-Updates & Poetry installieren
RUN apt-get update && apt-get install -y curl && \
    pip install poetry

# 4. Poetry-Dateien kopieren
COPY pyproject.toml poetry.lock* /app/

# 5. Poetry konfigurieren (keine virtuelle Umgebung im Container)
RUN poetry config virtualenvs.create false

# 6. Pakete installieren
RUN poetry install --no-root --only main

# 7. Den restlichen Code kopieren
COPY . /app

# 8. Port freigeben
EXPOSE 5001

# 9. Server starten
CMD ["uvicorn", "Microservice:app", "--host", "0.0.0.0", "--port", "5001"]