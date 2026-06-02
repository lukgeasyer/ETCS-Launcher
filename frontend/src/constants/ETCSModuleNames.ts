// Module Names
export enum ETCSModuleNames {
    CORE = "CORE",
    DMI = "DMI",
    GRAPH = "Graph",
    SMART_TMS = "smartTMS",
    SMART_LOGIC = "smartLogic",
    SCENARIOCONTROLSERVER = "ScenarioControlServer"
}

export function stringToEnum(moduleNameAsString: string) {
    let moduleName: ETCSModuleNames;

    switch (moduleNameAsString.toLowerCase()) {
        case "core":
            moduleName = ETCSModuleNames.CORE;
            break;
        case "dmi":
            moduleName = ETCSModuleNames.DMI;
            break;
        case "etcs-graph":
        case "graph":
            moduleName = ETCSModuleNames.GRAPH;
            break;
        case "tms":
        case "smarttms":
            moduleName = ETCSModuleNames.SMART_TMS;
            break;
        case "smartlogic":
            moduleName = ETCSModuleNames.SMART_LOGIC;
            break;
        case "scs":
        case "scenariocontrolserver":
            moduleName = ETCSModuleNames.SCENARIOCONTROLSERVER;
            break;
        default:
            throw Error("No such module: " + moduleNameAsString);
    }

    return moduleName;
}
