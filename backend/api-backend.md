# Schnittstellendokumentation

## etcsLauncher - Backend

Das Backend als .jar gepackt und ausgeführt öffnet
eine [REST API](https://de.wikipedia.org/wiki/Representational_State_Transfer#Umsetzung), mithilfe
derer die Funktionen aus dem Backend ausgeführt werden können sowie einen WebSocket, auf den sich verbunden werden kann.
Verbundene Klienten können sich auf den Output von laufenden ETCS-Modulen registrieren.

# REST API

Über die Adresse
> localhost:8090

können (statische) Anfragen an das Backend gestellt werden.

Folgende Anfragen sind verfügbar:

## ApplicationController

Den [ApplicationController](src/main/java/ebd/etcsLauncher/backend/api/controller/ApplicationController.java)
betreffende Anfragen
beinhalten das Terminieren der Backend-Anwendung.

### POST Anfragen

#### 1. Shutdown Backend

Terminiert die Backend-Anwendung, wenn keine ETCS-Module laufen.

> ```
> POST http://localhost:8090/shutdown
> ```

## ModuleManager

Den [ModuleManager](src/main/java/ebd/etcsLauncher/backend/model/moduleManager/ModuleManager.java) betreffende
Anfragen beinhalten das Verwalten
von ETCS-Modulen.

### GET Anfragen

#### 1. Get Available Modules

Liefert die Liste der verfügbaren ETCS-Module.

> ```
> GET http://localhost:8090/moduleManager/availableModules
> ```

#### 2. Get Config Values

Liefert die Liste der config-Variablen des verfügbaren ETCS-Moduls.

> ```
> GET http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION/configValues
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]
>

### POST Anfragen

#### 1. Scan File System

Durchsucht das interne Dateisystem nach verfügbaren ETCS-Modulen.

Liefert anschließend die Liste an verfügbaren ETCS-Modulen.

> ```
> POST http://localhost:8090/moduleManager/availableModules/scanFileSystem
> ```

### PUT Anfragen

#### 1. Add Available Module

Kopiert die .jar für ein ETCS-Modul in das interne Dateisystem und fügt
das ETCS-Modul der Liste an verfügbaren ETCS-Modulen hinzu.

> ```
> PUT http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION?pathToJar=PATHTOJARFILE
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]
>
> **PATHTOJARFILE =** ["path to .jar file in user directory"]

> **Example:**
> ```
> PUT http://localhost:8090/moduleManager/availableModules/CORE/1.3.4?pathToJar=/Users/bob/ETCS-Core-Programm-1.3.4.jar
> ```

#### 2. Change Config Value

Ändert den Wert einer Config-Variablen eines verfügbaren ETCS-Moduls.

> ```
> PUT http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION/configValues?variableName=VARIABLENAME&newValue=NEWVALUE
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]
>
> **VARIABLENAME =** "[name of variable]"
>
> **NEWVALUE =** "[new value of variable]"

> **Example:**
> ```
> PUT http://localhost:8090/moduleManager/availableModules/CORE/1.3.4/configValues?variableName=core.useTMSDummyServer&newValue=true
> ```

#### 3. Set Command Line Arguments

Speichert die Kommandozeilen-Argumente des verfügbaren ETCS-Moduls, die beim Ausführen
der .jar Datei verwendet werden.

> ```
> PUT http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION/commandLineArguments
> Content-Type: application/json
> [
>   "[arg1]",
>   "[arg2]",
>   "[arg3]",
>   ...
> ]
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]

> **Example:**
> ```
> PUT http://localhost:8090/moduleManager/availableModules/DMI/1.4.0/commandLineArguments
> Content-Type: application/json
> [
>   "headless",
>   "trainID=1"
> }
> ```

### DELETE Anfragen

#### 1. Remove Available Module

Löscht das verfügbare ETCS-Modul aus der Liste der verfügbaren ETCS-Module und löscht
den internen Ordner, der nach der Version des ETCS-Moduls benannt ist.

> ```
> DELETE http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]

#### 2. Delete Command Line Arguments

Löscht alle Kommandozeilen-Argumente des verfügbaren ETCS-Moduls.

> ```
> DELETE http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION/commandLineArguments
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]

## ProcessManager

Den [ProcessManager](src/main/java/ebd/etcsLauncher/backend/model/processManager/ProcessManager.java) betreffende
Anfragen beinhalten das Starten
und Verwalten von laufenden ETCS-Modulen.

### GET Anfragen

#### 1. Get Running Modules

Liefert die Liste aller laufenden ETCS-Module.

> ```
> GET http://localhost:8090/processManager/running
> ```

### POST Anfragen

#### 1. Run Modules To Run

Startet die spezifizierten und verfügbaren ETCS-Module.

> ```
> POST http://localhost:8090/processManager/run
> Content-Type: application/json
> [
>   MODULENAME_1: MODULEVERSION_1,
>   MODULENAME_2: MODULEVERSION_2,
>   MODULENAME_3: MODULEVERSION_3,
>   ...
> ]
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]

> **Example:**
> ```
> POST http://localhost:8090/processManager/run
> Content-Type: application/json
> {
>   "CORE"                      : "1.3.4",
>   "DMI"                       : "1.4.0",
>   "ScenarioControlServer"     : "1.0.5"
> }
> ```

#### 2. Send Input To Running Module

Sendet input an ein laufendes ETCS-Modul.

> ```
> POST http://localhost:8090/processManager/running/MODULENAME?input=INPUT
> ```
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **INPUT =** [input to send to module]

> **Example:**
> ```
> POST http://localhost:8090/processManager/running/CORE?input=start
> ```

#### 3. Stop Running Modules

Stoppt alle laufenden ETCS-Module.

> ```
> POST http://localhost:8090/processManager/stop
> ```

### PUT Anfragen

#### 1. Set Delay Between Starting Of Modules

Setzt die Zeit in Sekunden zwischen dem Starten der ETCS-Module, **wenn**
eine Verzögerung notwendig ist. Beim Ausführen von ETCS-Modulen, wo keine
Verzögerung notwendig ist, wird keine Verzögerung eingebaut.

> ```
> PUT http://localhost:8090/processManager/delay?delayBetweenStarting=DELAY
> ```
>
> **DELAY =** [delay between the start of processes in seconds]

# WebSocket

### 1. Mit WebSocket verbinden

Klienten können sich unter

> ```
> http://localhost:8090/processManager/webSocket
> ```

mit dem WebSocket verbinden.

### 2. ETCS-Modul-Output erhalten

Klienten können sich unter

> ```
> http://localhost:8090/processManager/running/MODULENAME
> ```
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]

registrieren, um den Output eines laufenden ETCS-Moduls mit dem Namen MODULENAME
in Echtzeit zu erhalten.