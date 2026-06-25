# Concept documentation ETCS-Launcher - Backend

> **Tools:** Java, Groovy/Spock, Maven, Spring

To implement the functionality described in the README, the following Java classes were designed:

## 1. [ETCSModule](src/main/java/ebd/etcsLauncher/backend/model/etcsModule/ETCSModule.java)

This class acts as a wrapper around a `.jar` file of an ETCS module. It stores information about the ETCS module and manages its configuration file.

## 2. [ModuleManager](src/main/java/ebd/etcsLauncher/backend/model/moduleManager/ModuleManager.java)

This class manages multiple [ETCSModule](src/main/java/ebd/etcsLauncher/backend/model/etcsModule/ETCSModule.java) instances. In particular, it maintains a list of available ETCS modules (available `.jar` files) and a list of modules that are to be executed together.

New ETCS modules can be added by providing the file path to the corresponding `.jar` file.

## 3. [ETCSModuleProcessWorker](src/main/java/ebd/etcsLauncher/backend/model/processManager/ETCSModuleProcessWorker.java)

This class acts as a wrapper around a running ETCS module process (i.e., an executed `.jar` file). It implements a [run()](src/main/java/ebd/etcsLauncher/backend/model/processManager/ETCSModuleProcessWorker.java) method that starts the process when executed.

The class forwards the output of a running ETCS module to a WebSocket endpoint to which clients can subscribe (see the WebSocket documentation in `api-backend.md`).

## 4. [ProcessManager](src/main/java/ebd/etcsLauncher/backend/model/processManager/ProcessManager.java)

This class manages the running processes, which are represented as [ETCSModuleProcessWorker](src/main/java/ebd/etcsLauncher/backend/model/processManager/ETCSModuleProcessWorker.java) instances.

