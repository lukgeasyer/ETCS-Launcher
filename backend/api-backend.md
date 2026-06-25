# API documentation ETCS-Launcher - Backend

The backend executed as a ``.jar`` opens a [REST API](https://de.wikipedia.org/wiki/Representational_State_Transfer#Umsetzung) which offers functionality for executing and managing the ETCS modules as 
well as for subscribing to the output of running ETCS modules via a WebSocket.

# REST API

ÜThe backend can be accessed via the address

> localhost:8090

to perform (static) requests.

The following requests are available:

## ApplicationController

Requests related to the [ApplicationController](src/main/java/ebd/etcsLauncher/backend/api/controller/ApplicationController.java) include terminating the backend application.

### POST Requests

#### 1. Shutdown Backend

Terminates the backend application if no ETCS modules are currently running.


> ```
> POST http://localhost:8090/shutdown
> ```

## ModuleManager
## ModuleManager

Requests related to the [ModuleManager](src/main/java/ebd/etcsLauncher/backend/model/moduleManager/ModuleManager.java) involve the management of ETCS modules.

### GET Requests

#### 1. Get Available Modules

Returns the list of available ETCS modules.

> ```
> GET http://localhost:8090/moduleManager/availableModules
> ```

#### 2. Get Config Values

Returns the list of configuration variables for the specified available ETCS module.

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

Scans the internal file system for available ETCS modules and returns the resulting list of available modules.

> ```
> POST http://localhost:8090/moduleManager/availableModules/scanFileSystem
> ```

### PUT Anfragen

#### 1. Add Available Module

Copies an ETCS module's `.jar` file into the internal file system and adds the module to the list of available ETCS modules.

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

Updates the value of a configuration variable for an available ETCS module.

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

Saves the command-line arguments for an available ETCS module, which are used when running the `.jar` file.

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

Deletes the available ETCS module from the list of available ETCS modules and removes the corresponding internal
folder named after the module’s version.

> ```
> DELETE http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]

#### 2. Delete Command Line Arguments

Removes all command-line arguments for the available ETCS module.

> ```
> DELETE http://localhost:8090/moduleManager/availableModules/MODULENAME/MODULEVERSION/commandLineArguments
> ```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]
>
> **MODULEVERSION =** ["X.Y.Z"]

## ProcessManager

Requests related to the [ProcessManager](src/main/java/ebd/etcsLauncher/backend/model/processManager/ProcessManager.java) handle the starting and management of running ETCS modules.


### GET Anfragen

#### 1. Get Running Modules

Returns all currently running ETCS modules.

> ```
> GET http://localhost:8090/processManager/running
> ```

### POST Anfragen

#### 1. Run Modules To Run

Starts the specified available ETCS modules.

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

Forwards input to a running ETCS module.

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

Terminates all running ETCS modules.

> ```
> POST http://localhost:8090/processManager/stop
> ```

### PUT Anfragen

#### 1. Set Delay Between Starting Of Modules

Sets the delay (in seconds) between starting ETCS modules when a delay is required. No delay is applied if it is not necessary.

> ```
> PUT http://localhost:8090/processManager/delay?delayBetweenStarting=DELAY
> ```
>
> **DELAY =** [delay between the start of processes in seconds]

# WebSocket

### 1. WebSocket Connection

Clients can connect using the following endpoint:


> ```
> http://localhost:8090/processManager/webSocket
> ```

### 2. Receive ETCS Module Output

Clients can connect at:

> ```
> http://localhost:8090/processManager/running/MODULENAME
>```
>
> **MODULENAME =** ["CORE", "DMI", "TMS", "smartLogic", "ScenarioControlServer"]

to register and receive the real-time output of a running ETCS module identified by MODULENAME.
