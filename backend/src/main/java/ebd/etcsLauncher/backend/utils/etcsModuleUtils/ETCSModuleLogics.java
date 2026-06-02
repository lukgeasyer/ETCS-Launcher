package ebd.etcsLauncher.backend.utils.etcsModuleUtils;

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Stores the logic behind the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s.
 * In particular the logic behind their config files and the order in which
 * {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s have to be started to work together.
 */
public class ETCSModuleLogics {

    public static boolean requiresCLIParamForConfigFile(ETCSModule etcsModule) {
        switch(etcsModule.getModuleName()) {
            case CORE -> {
                return etcsModule.getVersion().compareTo(new ETCSModuleVersion("1.6.2")) > 0;
            }
            case Graph -> {
                return etcsModule.getVersion().compareTo(new ETCSModuleVersion("1.6.1")) > 0;
            }
            case DMI, ScenarioControlServer, smartTMS, smartLogic -> {return false;}
            default -> throw new IllegalArgumentException("Unknown module name: " + etcsModule.getModuleName());
        }
    }

    /**
     * Keeps track of the location each .jar expects its config file to be in.
     *
     * @param moduleName
     *         the name of the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @return the location within the version folder where the .jar expects the config file to be
     */
    public static @NotNull Path getConfigLogic(ETCSModuleNames.@NotNull ModuleName moduleName) {
        switch(moduleName) {
            case CORE, Graph, ScenarioControlServer -> {return Path.of("config");}
            case smartTMS, smartLogic -> {return Path.of("");}
            case DMI -> {return Path.of("configuration");}
            default -> throw new IllegalArgumentException("Unknown module name: " + moduleName);
        }
    }

    /**
     * Keeps track of the name of the config file for each {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @param moduleName
     *         the name of the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @return the name of the config file
     */
    public static @NotNull String getNameOfConfigFile(ETCSModuleNames.@NotNull ModuleName moduleName) {
        switch(moduleName) {
            case CORE, Graph, smartTMS, smartLogic -> {return "application.properties";}
            case ScenarioControlServer -> {return "configSCS.txt";}
            case DMI -> {return "config.txt";}
            default -> throw new IllegalArgumentException("Unknown module name: " + moduleName);
        }
    }

    /**
     * Keeps track of the name of the internal (default) config file within the .jar for each
     * {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @param moduleName
     *         the name of the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @return the name of the internal (default) config file within the .jar
     */
    public static @NotNull String getNameOfInternalConfigFile(ETCSModuleNames.@NotNull ModuleName moduleName) {
        switch(moduleName) {
            case CORE, smartTMS, smartLogic -> {return "application.properties";}
            case DMI, ScenarioControlServer -> {return "config-default";}
            default -> throw new IllegalArgumentException("Unknown module name: " + moduleName);
        }
    }

    /**
     * Keeps track of the correct run order for the executing of multiple {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s
     * together
     *
     * @param modulesToRun
     *         all {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s to run together
     *
     * @return an {@link SortedETCSModuleArrayList} of the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s to run in the order they need to be
     * started
     */
    public static SortedETCSModuleArrayList getRunOrder(ETCSModuleSet modulesToRun) {
        SortedETCSModuleArrayList runOrder = new SortedETCSModuleArrayList();
        if(modulesToRun.contains(ETCSModuleNames.ModuleName.DMI)) {
            runOrder.add(modulesToRun.get(ETCSModuleNames.ModuleName.DMI));
        }
        if(modulesToRun.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)) {
            runOrder.add(modulesToRun.get(ETCSModuleNames.ModuleName.ScenarioControlServer));
        }
        if(modulesToRun.contains(ETCSModuleNames.ModuleName.Graph)) {
            runOrder.add(modulesToRun.get(ETCSModuleNames.ModuleName.Graph));
        }
        if(modulesToRun.contains(ETCSModuleNames.ModuleName.CORE)) {
            runOrder.add(modulesToRun.get(ETCSModuleNames.ModuleName.CORE));
        }
        if(modulesToRun.contains(ETCSModuleNames.ModuleName.smartTMS)) {
            runOrder.add(modulesToRun.get(ETCSModuleNames.ModuleName.smartTMS));
        }
        if(modulesToRun.contains(ETCSModuleNames.ModuleName.smartLogic)) {
            runOrder.add(modulesToRun.get(ETCSModuleNames.ModuleName.smartLogic));
        }
        return runOrder;
    }

}
