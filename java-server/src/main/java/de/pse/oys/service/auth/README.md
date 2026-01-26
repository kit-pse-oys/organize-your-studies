# AuthService – Google OAuth & Lokale Authentifizierung

## Zweck
Der AuthService verwaltet die Authentifizierung von Benutzern im System.  
Unterstützte Authentifizierungsarten:

- **Lokale Authentifizierung** (Username/Passwort)
- **Externe Authentifizierung via Google OAuth2

---

## Voraussetzungen / Google Einstellungen
1. **Google Cloud Projekt** erstellen
2. **OAuth2 Client-ID** generieren (Web-Anwendung oder Desktop, je nach Use Case)
3. Optional: Redirect-URIs für Web-Flow konfigurieren
4. **Scopes** definieren:
    - `openid` → zwingend für ID Token
    - `email` → optional, um Email zu erhalten
    - `profile` → optional, um Name/Bild zu erhalten

Die Client-ID muss auf der Server-Seite in den Einstellungen hinterlegt werden:

```properties
google.oauth2.client-id=DEINE_CLIENT_ID_HIER
