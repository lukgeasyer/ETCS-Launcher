package ebd.etcsLauncher.backend.utils.etcsModuleUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Manages the names of supported {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s.
 *
 * @author Lukas Geyer
 */
public class ETCSModuleNames {

    public static ModuleName stringToModuleName(@NotNull String moduleName) {
        switch(moduleName.toLowerCase()) {
            case "core" -> {return ModuleName.CORE;}
            case "dmi" -> {return ModuleName.DMI;}
            case "graph" -> {return ModuleName.Graph;}
            case "tms", "smarttms" -> {return ModuleName.smartTMS;}
            case "smartlogic" -> {return ModuleName.smartLogic;}
            case "scs", "scenariocontrolserver" -> {return ModuleName.ScenarioControlServer;}
            default -> throw new IllegalArgumentException("No such module exists or string is not mapped: " + moduleName);
        }
    }

    public enum ModuleName {
        CORE,
        DMI,
        Graph,
        smartTMS,
        smartLogic,
        ScenarioControlServer
    }

}

