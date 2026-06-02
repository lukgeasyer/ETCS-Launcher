# Konzeptdokumentation

## etcsLauncher - Backend

> **Tools:** Java, Groovy/Spock, Maven, Spring

Die Funktionen des etcsLaunchers in Version 1.1 beinhalten:

1. Das gemeinsame Ausführen einzelner ETCS-Module
2. Das Durchsuchen und Anpassen von config-Variablen der ETCS-Module
3. Das Einbringen eigener .jar-Dateien für die einzelnen ETCS-Module

Folgende ETCS-Module werden unterstützt:

1. etcsCore
2. etcsDMI
3. smartTMS
4. smartLogic
5. ScenarioControlServer

Um die oben genannten Funktionalitäten zu implementieren, wurden folgende
Java-Klassen entworfen:

## 1. [ETCSModule](src/main/java/ebd/etcsLauncher/backend/model/etcsModule/ETCSModule.java)

Diese Klasse funktioniert als Wrapper um eine .jar Datei eines ETCS-Moduls. Sie speichert
informationen über das ETCS-Modul und verwaltet dessen config-Datei.

## 2. [ModuleManager](src/main/java/ebd/etcsLauncher/backend/model/moduleManager/ModuleManager.java)

Diese Klasse verwaltet verschiedene [ETCSModule](src/main/java/ebd/etcsLauncher/backend/model/etcsModule/ETCSModule.java).
Sie verwaltet insbesondere die Liste an verfügbaren ETCS-Modulen (verfügbare .jar-Dateien)
und eine Liste über die Module, die zusammen ausgeführt werden sollen.
Neue ETCS-Module können hinzugefügt werden, in dem der Dateipfad für die
entsprechende .jar übergeben wird.

## 3. [ETCSModuleProcessWorker](src/main/java/ebd/etcsLauncher/backend/model/processManager/ETCSModuleProcessWorker.java)

Diese Klasse funktioniert als Wrapper um einen laufenden ETCS-Modul-Prozess (also eine ausgeführte
.jar Datei). Sie implementiert eine [run()](src/main/java/ebd/etcsLauncher/backend/model/processManager/ETCSModuleProcessWorker.java)-Methode,
die beim Ausführen den Prozess startet. Die Klasse sendet den Output eines laufenden
ETCS-Moduls an eine WebSocket-Adresse, auf die sich Klienten registrieren können
(siehe [WebSocket-Dokumentation](api-backend.md)).

## 4. [ProcessManager](src/main/java/ebd/etcsLauncher/backend/model/processManager/ProcessManager.java)

Diese Klasse verwaltet die laufenden Prozesse, die
als [ETCSModuleProcessWorker](src/main/java/ebd/etcsLauncher/backend/model/processManager/ETCSModuleProcessWorker.java)
dargestellt sind. 