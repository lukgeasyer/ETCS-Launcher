package ebd.etcsLauncher.backend.utils.fileSystemUtils;

import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Keeps track of the logic of the internal file system. In particular the name of the folder that contains the
 * .jar files of all available {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s
 * and the logic behind the subdirectories.
 */
public class FileSystemLogic {

    private final static String jarFolder = "jarFiles";

    /**
     * Defines the logic of the subdirectories for each {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @param moduleName
     *         the name of the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *
     * @return the path of the directory where the versions of this {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     * are stored
     */
    public static @NotNull Path getDirectoryLogic(ETCSModuleNames.@NotNull ModuleName moduleName) {
        Path jarDirectory = Paths.get(jarFolder);
        switch(moduleName) {
            case CORE, Graph, DMI -> {return jarDirectory.resolve(moduleName.name());}
            case smartTMS, smartLogic, ScenarioControlServer -> {return jarDirectory.resolve("TMS").resolve(moduleName.name());}
            default -> throw new IllegalArgumentException("Unknown module name: " + moduleName);
        }
    }

    public static String getJarFolder() {
        return jarFolder;
    }

}
