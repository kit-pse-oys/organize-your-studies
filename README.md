# Organize Your Studies 

## 📚 Überblick (Overview)
„Organize Your Studies“ ist ein Client-Server-basiertes System für Android-Geräte, das Studierende bei der strukturierten Kalender- und Lernzeitplanung unterstützt, um eine effektivere Organisation des Studienalltags zu ermöglichen.
Die Anwendung bietet eine übersichtliche Darstellung von Aufgaben, Lernzeiten und Fristen und unterstützt die Planung durch teilautomatisierte Funktionen zur Zeitstrukturierung unter Berücksichtigung individueller Präferenzen.


## 🔭 Grundlegende Spezifikationen
  - **Version:** 1.0.0 Stand Februar 2026
  - **Plattform:** Android (API Level 33)
  - **Architektur:** Client-Server-Architektur (mit ausgelagerten Microservice für schwere Logik)
  - **Programmiersprache:** Kotlin (Client), Java (Server), Python (Microservice)
  - **Datenbank:** PostgreSQL 
  - **Build-Tool:** Gradle (Kotlin DSL)
  - **CI/CD:** GitHub Actions [.github/workflows](.github/workflows)


## 🌲 Repository Struktur
```text
kotlin-client/app/               # Android Client Source Code
java-server/                     # Java Server Source Code
python-microservice/             # Python Microservice Source Code
database/                        # Datenbank-Schema
docs/                            # Projektdokumentation im Rahmen des PSE (nach Abschluss des Projekts)
.github/workflows/               # CI/CD Workflows
docker-compose.yml               # Docker Compose Setup File (Server + Microservice + DB)
```

## 🗨️ Anmerkungen
- Dieses Projekt wurde im Rahmen des Moduls "Praxis der Softwareentwicklung" (PSE) am Karlsruher Institut für Technologie (KIT) erstellt.
- Beachten sie die unten aufgeführten rechtlichen Hinweise.
- Diese Kurzanleitung dient zur Inbetriebnahme des Systems und setzt grundlegende Kenntnisse in der Nutzung von Android Studio, Docker und der Kommandozeile voraus. Nicht jeder Schritt wird im Detail erklärt.
- Die angegeben Befehle sind in der Windows PowerShell Syntax verfasst. Passen Sie diese ggf. für andere Betriebssysteme an.

# Inbetriebnahme Anleitung

## 🚀 Voraussetzung
- Stellen Sie sicher, dass Sie Android Studio installiert (SDK Android 16 "Baklava" oder höher) haben und ein Android-Gerät (API Level 33+) oder -Emulator zur Verfügung steht.
- Stellen Sie sicher, dass ein Hosting für das Backend eingerichtet ist. Hier sind folgende Voraussetzungen zu beachten:
  - Docker und Docker Compose installiert
  - Certbot Standalone für SSL-Zertifikate installiert (inklusiver notwendiger Python Version). Anleitung hier: [Certbot Standalone](https://certbot.eff.org/instructions?ws=other&os=pip&tab=standard)
  - Domainname, der auf die öffentliche IP des Servers zeigt (z.B. mittels A-Record im DNS, Überprüfung z.B. mit [Whatsmydns](https://www.whatsmydns.net/))
  - Offene Sichterheitsgruppen/Firewall-Regeln für die Ports 80 (HTTP) und 443 (HTTPS)
- Stellen Sie sicher, dass Sie für die Google Authentifizierung ein OAuth 2.0 Client ID und Client Secret erstellt haben. Beachten Sie, dass für diesen Schritt der Fingerabdruck des Clients benötigt wird. 1.Anleitung hier: [Google OAuth 2.0](https://developers.google.com/identity/protocols/oauth2)
- Stellen Sie sicher, dass sie sich die Umgebungsvariablen für den Client und Server notiert haben (siehe unten).

## 🔧 Inbetriebnahme - Client
1. Öffnen Sie Android Studio und wählen Sie "Open from Version Control". Wählen Sie das geklonte Repository-Verzeichnis `kotlin-client` aus. (Android Studio hat möglicherweise Probleme damit ein Multi-Module Projekt zu öffnen, daher der Umweg über das Unterverzeichnis.)
2. Warten Sie, bis Android Studio alle Abhängigkeiten heruntergeladen und das Projekt synchronisiert hat.
3. Setzten sie die notwendigen Umgebungsvariablen im Code bzw. der `local.properties` Datei im `kotlin-client/` Verzeichnis:
4. Verbinden Sie Ihr Android-Gerät oder starten Sie einen Emulator.
5. Klicken Sie auf "Run" (grüner Pfeil) in Android Studio, um die App auf Ihrem Gerät/Emulator zu installieren und zu starten.

## 🔧 Inbetriebnahme - Server, Microservice und Datenbank
Das Backend besteht aus einem Java-Server, einem Python-Microservice und einer PostgreSQL-Datenbank. Diese Komponenten werden mittels Docker Compose orchestriert. Daher müssen Docker und Docker Compose auf dem Server installiert sein. Die Docker verwalten die Abhängigkeiten und die Laufzeitumgebung für alle drei Komponenten.
1. Klonen Sie das Repository auf Ihren Server.
2. Navigieren Sie in das Verzeichnis `java-server/` und erstellen Sie eine `.env` Datei nach dem Muster der `.env.example` Datei. Füllen Sie die notwendigen Umgebungsvariablen aus.
3. Nutzen Sie `docker-compose.yml` im Hauptverzeichnis, um den Server, Microservice und die Datenbank zu starten:
   ```bash
   docker compose up -d
   ```
4. Überprüfen Sie die Logs der Container, um sicherzustellen, dass alle Dienste korrekt gestartet wurden:
   ```bash
   docker compose logs -f
   ```
5. Stellen Sie sicher, dass Ihre Domain korrekt auf den Server zeigt und dass SSL-Zertifikate ordnungsgemäß eingerichtet sind (siehe Certbot Anleitung oben).
6. Testen Sie die Verbindung vom Client zum Server, indem Sie die App auf Ihrem Android-Gerät starten und sich anmelden oder im lokalen Terminal eine Test-Request an den Server senden.
7. Um den Server zu stoppen, verwenden Sie:
   ```bash
   docker compose down
   ```

## 🔧 Tests
- **Client-Tests:** Unit-Tests und UI-Tests sind im `\kotlin-client\app\src\test` zu finden. Führen Sie diese Tests in Android Studio aus oder verwenden Sie Gradle:
   ```bash
  .\gradlew test
   ```
- **Server-Tests:** Unit-Tests für den Java-Server sind im `\java-server\src\test` Verzeichnis zu finden.
   ```bash
  .\gradlew test
   ```
- **Microservice-Tests:** Unit-Tests für den Python-Microservice sind im `\python-microservice\tests` Verzeichnis zu finden. Führen Sie diese Tests mit pytest aus:
    ```bash
    pytest
    ```
Bei Änderungen am Code und entsprechendem Push werden durch Github Actions automatisch entsprechende Tests ausgeführt. Beachten sie dass für containerisierte lokale Tests auf Mac / Windows Systemen ggf. Docker-Desktop installiert sein muss. [Docker Desktop](https://www.docker.com/products/docker-desktop/).
   
## 🚩 Bekannte Einschränkungen
- Die Anwendung wurde hauptsächlich auf Android-Geräten mit API Level 33 getestet. Ältere Versionen könnten unerwartetes Verhalten zeigen.
- Die SSL-Zertifikatsverwaltung ist auf Certbot Standalone beschränkt. Andere Methoden (z.B nginx) sind nicht getestet.
- Die Anwendung unterstützt derzeit nur Google OAuth 2.0 für die externe Authentifizierung. Andere Anbieter sind nicht implementiert.

## 👥 Teammitglieder
- uhupo
- uhxch
- urfmo
- utgid
- uqvfm
- uqyjn

## ⚖️ Rechtliche Hinweise

Dieses Projekt wurde im Rahmen des Moduls "Praxis der Softwareentwicklung" (PSE) am Karlsruher Institut für Technologie (KIT) erstellt.

* **Lizenz:** Dieses Repository ist **nicht lizenziert** (No License). Alle Rechte am Code und an den Inhalten verbleiben bei den Autoren.
* **Nutzung:** Das Kopieren, Verändern oder Verbreiten des Codes für andere Zwecke (insbesondere für andere Studienleistungen) ohne vorherige Abklärung mit den Autoren dieses Projektes ist ausdrücklich **nicht gestattet**.
* **Akademische Integrität:** Die Verwendung von Teilen dieses Codes in anderen Studienleistungen kann als Plagiat gewertet werden.
* **Haftung:** Der Code wird "wie besehen" bereitgestellt, ohne jegliche Gewährleistung für Funktionalität oder Eignung für einen bestimmten Zweck. Die Nutzung (auch unbefugt) erfolgt auf eigene Gefahr.
* **Hintergrund:** Die öffentliche Bereitstellung dieses Repositories dient ausschließlich der Nutzung automatisierter Entwicklungs-Workflows (CI/CD) und stellt keine Freigabe zur Nutzung dar.
* **Externe Bibliotheken:** Sofern externe Bibliotheken verwendet werden, unterliegen diese deren jeweiligen Lizenzen. Bitte prüfen Sie die Lizenzbedingungen der verwendeten Bibliotheken vor der Nutzung.
