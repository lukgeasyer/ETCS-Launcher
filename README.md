# ETCS-Launcher

Der ETCS-Launcher ist eine plattformübergreifende Desktop-Anwendung, mit der sich `.jar`-Dateien des ETCS@EBD-Projekts verwalten und ausführen lassen.

## Version 1.1

Unterstützte ETCS-Module:

1. Core
2. DMI
3. Graph
4. smartTMS
5. smartLogic
6. ScenarioControlServer

**Hinweis:** Das Modul ETCS-smartLogic benötigt eine `.ppxml`-Datei im selben Verzeichnis, in dem sich die entsprechende `.jar`-Datei befindet. Das Modul ETCS-smartTMS benötigt einen SQL-Server, der im Hintergrund läuft. Weitere Informationen finden Sie in den jeweiligen Repositories.

# Struktur

Die Struktur von Backend und Frontend wird in den entsprechenden README-Dateien erläutert:

* Backend: `backend/concept-backend.md`
* Frontend: `frontend/concept-frontend.md`

# Verwendung des Backends unabhängig vom Frontend

Das Backend kann eigenständig zur Verwaltung der ETCS Module verwendet werden.
Eine Anleitung zur Nutzung der Backend-API ist in `backend/api-backend.md` zu finden.

# Installation

Eine ausführbare Datei kann erstellt werden, indem den Anweisungen in der README-Datei des Frontends (`/frontend/README.md`) gefolgt wird.

Das Backend kann unabhängig erstellt werden, indem den Anweisungen in der README-Datei des Backends (`/backend/README.md`) gefolgt wird.

