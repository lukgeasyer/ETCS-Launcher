# ETCS-Launcher

The ETCS-Launcher is a multi-platform desktop application which
lets you organize and run `.jar` files from the ETCS@EBD project.

## Version 1.1

Supported ETCS-Modules:

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

# Structure
The structure of the backend and frontend is eplained in the corresponding README files: [Backend](backend/concept-backend.md), [Frontend](frontend/concept-frontend.md).

# Using the backend independently 
You can deploy your own frontend as you wish and use the backend for managing the ETCS Modules. The manual for using the backend API is found in [api-backend.md](backend/api-backend.md).

# Installing
An executable can be created by following the instructios in [the README for the frontend](/frontend/README.md).

The backend can be built independently by following the instructions in [the README for the backend](/backend/README.md).
