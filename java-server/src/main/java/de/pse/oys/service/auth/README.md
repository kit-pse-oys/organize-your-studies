# AuthService – Google OAuth & Lokale Authentifizierung

## Zweck
Der AuthService verwaltet die Authentifizierung von Benutzern im System.  
Unterstützte Authentifizierungsarten:

- **Lokale Authentifizierung** (Username/Passwort)
- **Externe Authentifizierung via Google OAuth2 (OpenID Connect)**
---

## Voraussetzungen / Google Einstellungen
1. **Google Cloud Projekt** erstellen
2. **OAuth2 Client-ID** generieren (Web-Anwendung oder Desktop, je nach Use Case)
3. Optional: Redirect-URIs für Web-Flow konfigurieren
4. **Scopes** definieren:
    - `openid` → zwingend für ID Token
    - `profile` → um den Namen des Benutzers zu erhalten und als Benutzernamen zu verwenden  
   
Die Client-ID muss auf der Server-Seite in den Einstellungen hinterlegt werden:

```properties
google.oauth2.client-id=DEINE_CLIENT_ID_HIER
````
## Ablauf der Authentifizierung

1. **Login**: Benutzer sendet Anmeldedaten (lokal: Username/Passwort, extern: Google Token).
2. **Validierung**: AuthService prüft die Daten und erstellt Access- und Refresh-Token.
3. **Token-Speicherung**: Das Refresh-Token wird gehasht in der Datenbank gespeichert.
4. **Zugriffsschutz**: Bei jedem Request wird das Access-Token geprüft.
5. **Logout**: Das Refresh-Token wird verworfen.

## Beispiel-Request: Lokaler Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "max",
  "password": "geheim",
  "authType": "BASIC"
}
