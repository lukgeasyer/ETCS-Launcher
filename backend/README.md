## Developing

Use the IDE of your choice, IntelliJ is recommended. The projects packages
are organized with maven, see the `pom.xml`.

## Building

Currently the backend is simply packaged into a `.jar` file and an appropriate JDK
is included with the ETCS-Launcher.

Building the `.jar` file can be done via

````bash
mvn package
````

Note: The tests typically take a couple of minutes.

**Please do not skip tests when building the `.jar` for production.**

