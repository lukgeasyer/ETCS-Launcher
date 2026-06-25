# ETCS-Launcher

The ETCS-Launcher is a multi-platform desktop application which
lets you organize and run `.jar` files from the ETCS@EBD project.

# Concept Documentation

## Version 1.1

This document explains the motivation behind the ETCS-Launcher and outlines its functionality.

### Motivation

The ETCS@EBD project consists of various software components that simulate a train journey according to the [ETCS guidelines](https://en.wikipedia.org/wiki/European_Train_Control_System). These components can be executed as `.jar` files. To run the `.jar` files together, the user must know the order in which they need to be started, which additional files are required for the software components to run correctly, and how the configuration parameters must be set so that the components can communicate with one another.

To simplify the use of these software components and their corresponding `.jar` files, the ETCS-Launcher was developed. It provides a graphical user interface that allows individual software components to be executed either independently or together, without requiring the user to know the correct startup order or how the configuration parameters need to be configured.

The user only needs to start the ETCS-Launcher, select the programs they wish to run, and click "Start". The ETCS-Launcher handles everything else.

In addition, the ETCS-Launcher can be used by advanced users to configure execution settings, such as editing configuration parameters, without having to manually open and modify the corresponding files.

### Features

Version **1.1** of the ETCS-Launcher provides the following functionality:

1. Running multiple ETCS modules together
2. Browsing and modifying configuration variables of ETCS modules
3. Adding custom `.jar` files for individual ETCS modules
4. Setting command-line arguments

The following ETCS modules are supported in version **1.1**:

1. Core
2. DMI
3. Graph
4. smartTMS
5. smartLogic
6. ScenarioControlServer

**Note:** The ETCS-smartLogic module needs a `.ppxml` file within the
folder where the `.jar` lies and the ETCS-smartTMS module needs a SQL server
which is running in the background. See the corresponding
repositories for more information.

### Implementation

To implement the features described above, the ETCS-Launcher is divided into a backend and a frontend. The backend exposes its functionality via HTTP requests, while the frontend communicates with the backend and executes the actions requested by the user.

The ETCS-Launcher backend creates an internal file system in which it stores the available versions of the ETCS modules.

The file system has the following structure:


```
jarFiles
│    
└───CORE
│   │   
│   └───[Version 1]
│   │    │   [.jar-File]
│   │    │
│   │    └───config
│   │         │   application.properties
│   │         │   logback-spring.xml
│   │         │
│   │         └───blueprints
│   │         │
│   │         └───scenario
│   │         │
│   │         └───log
│   │
│   └───[Version 2]
│   │    │   [.jar-File]
│   │    │
│  ...   └───config
│             │   application.properties
│             │   logback-spring.xml
│             │
│             └───blueprints
│             │
│             └───scenario
│             │
│             └───log
│
└───DMI
│   │
│   └───[Version 1]
│   │    │   [.jar-File]
│   │    │
│   │    └───configuration
│   │         │   config.txt
│   │  
│   └───[Version 2]
│   │    │   [.jar-File]
│   │    │
│  ...   └───configuration
│             │   config.txt
│
│
└───Graph
│   │
│   └───[Version 1]
│   │    │   [.jar-File]
│   │    │
│   │    └───config
│   │         │   application.properties
│   │  
│   └───[Version 2]
│   │    │   [.jar-File]
│   │    │
│  ...   └───config
│             │   application.properties
│
│
└───TMS
    │
    └───ScenarioControlServer
    │    │   
    │    └───[Version 1]
    │    │    │   [.jar-File]
    │    │    │ 
    │    │    └───config
    │    │         │   configSCS.txt
    │    │    
    │    └───[Version 2]
    │    │    │   [.jar-File]
    │    │    │ 
    │   ...   └───config
    │              │   configSCS.txt
    │
    │
    └───smartLogic
    │    │
    │    └───[Version 1]
    │    │    │   [.jar-File]
    │    │    │   application.properties
    │    │    │
    │    │    └───log
    │    │
    │    └───[Version 2]
    │    │    │   [.jar-File]
    │    │    │   application.properties
    │   ...   │
    │         └───log
    │
    │
    └───smartTMS
         │   
         └───[Version 1]
         │    │   [.jar-File]
         │    │   application.properties
         │
         └───[Version 2]
         │    │   [.jar-File]
         │    │   application.properties
         │
        ...
``` 

If an ETCS module is added, for those that have their config file included within the ``.jar`` it automatically gets extracted and copied to the
folder the module expects the config file to be.

The folders ``blueprints``, ``scenario`` and ``log`` as well as the file ``logback-spring-xml`` will be created by the modules once they have
been started.

# Code structure
The structure of the backend and frontend is eplained in the corresponding README files: [Backend](backend/concept-backend.md), [Frontend](frontend/concept-frontend.md).

# Using the backend independently 
You can deploy your own frontend as you wish and use the backend for managing the ETCS Modules. The manual for using the backend API is found in [api-backend.md](backend/api-backend.md).

# Installing
An executable can be created by following the instructios in [the README for the frontend](/frontend/README.md).

The backend can be built independently by following the instructions in [the README for the backend](/backend/README.md).
