# Konzeptdokumentation

## etcsLauncher

## Version 1.1

In diesem Dokument soll die Motivation hinter dem etcsLauncher erläutert sowie dessen Funktionalität
dargelegt werden.

### Motivation

Im Projekt ETCS@EBD gibt es viele verschiedene Software-Bausteine, die eine Zugfahrt
unter den [ETCS-Richtlinien](https://de.wikipedia.org/wiki/European_Train_Control_System)
simulieren. Diese Bausteine sind als .jar-Dateien ausführbar. Um die .jar-Dateien miteinander auszuführen,
muss die Person, die diese Programme zusammen ausführen möchte, wissen, in welcher Reihenfolge
die .jar-Dateien gestartet werden müssen, welche zusätzlichen Dateien die Software-Bausteine
benötigen, um fehlerfrei laufen zu können und wie die Konfigurationsparameter der Software-Bausteine
eingestellt werden müssen, damit diese miteinander kommunzieren.

Um den Umgang mit diesen Software-Bausteinen bzw. den .jar-Dateien zu erleichtern, wurde der etcsLauncher entworfen.
Dieser soll eine grafische Oberfläche bereitstellen, mithilfe derer die einzelnen Software-Bausteine
alleine oder gemeinsam ausgeführt werden können, ohne, dass der Nutzer wissen muss,
in welcher Reihenfolge diese gestartet werden müssen oder wie die Konfigurationsparameter eingestellt
werden sollen.

Der Nutzer muss lediglich den etcsLauncher starten, die Programme auswählen, die er gerne ausführen möchte,
und auf "Start" klicken. Alles Weitere soll der etcsLauncher erledigen.

Zusätzlich soll der etcsLauncher auch dafür genutzt werden können, um als fachkundiger Nutzer Einstellungen
für die Ausführung vorzunehmen, beispielsweise die Konfigurationsparameter zu bearbeiten, ohne dass die entsprechenden
Dateien geöffnet werden müssen.

### Funktionsumfang

Folgende Funktionalität steht in Version **1.1** vom etcsLauncher bereit:

1. Das gemeinsame Ausführen einzelner ETCS-Module
2. Das Durchsuchen und Anpassen von config-Variablen der ETCS-Module
3. Das Einbringen eigener .jar-Dateien für die einzelnen ETCS-Module
4. Das Setzen von Kommandozeilen-Argumenten

Folgende ETCS-Module werden in Version **1.1** unterstützt:

1. Core
2. DMI
3. Graph
4. smartTMS
5. smartLogic
6. ScenarioControlServer

### Umsetzung

Um die im Funktionsumfang dargelegten Features umzusetzen, wurde der etcsLauncher
in ein Backend und Frontend unterteilt. Das Backend ist dabei über HTTP-Anfragen
erreichbar und stellt die Funktionalität bereit. Das Frontend kommuniziert mit dem
Backend und führt die Funktionen aus, die der Nutzer vorgibt.

Das Backend vom etcsLauncher erstellt ein internes Dateisystem, in dem es die
verfügbaren Versionen der ETCS-Module ablegt.

Das Dateisystem hat folgende Struktur:

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

Wenn ein neues ETCS-Modul (also eine neue .jar-Datei) hinzugefügt wird,
wird automatisch die entsprechende config-Datei aus der .jar extrahiert und
an der Stelle abgelegt, wo das Programm die config-Datei erwartet.

Die Ordner ``blueprints``, ``scenario`` und ``log`` sowie die Datei ``logback-spring-xml``
werden von den entsprechenden Modulen erstellt, sobald sie ausgeführt wurden.