package ebd.etcsLauncher.backend.utils.fileSystemUtils;

import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getDirectoryLogic;

/**
 * Provides useful functions for interacting with the file system.
 *
 * @author Lukas Geyer
 */
public class FileSystemUtils {

    /**
     * Scans the internal file system and looks for .jar files to run
     *
     * @return a {@link HashMap} that stores the available versions and the name of the .jar
     * for each {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     */
    public static @NotNull Map<ETCSModuleNames.ModuleName, Map<ETCSModuleVersion, String>> getAvailableVersionsAndJarNamesFromFileSystem() {
        Map<ETCSModuleNames.ModuleName, Map<ETCSModuleVersion, String>> availableVersions = new HashMap<>();

        for(ETCSModuleNames.ModuleName moduleName : ETCSModuleNames.ModuleName.values()) {
            availableVersions.put(moduleName, getVersionsAndJarFilesInModuleDirectory(getDirectoryLogic(moduleName)));
        }

        return availableVersions;
    }

    /**
     * Scans the module directory and looks for .jar files in each version folder
     *
     * @param pathToModuleDirectory
     *         the path to the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule} directory
     *
     * @return a {@link List} of available versions within this {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule} directory
     */
    public static Map<ETCSModuleVersion, String> getVersionsAndJarFilesInModuleDirectory(@NotNull Path pathToModuleDirectory) {
        if(!pathToModuleDirectory.toFile().isDirectory() || pathToModuleDirectory.toFile().listFiles() == null) {
            return new HashMap<>();
        }

        Map<ETCSModuleVersion, String> versionsAndJarFiles = new HashMap<>();

        List<File> subdirectories = Arrays.stream(Objects.requireNonNull(pathToModuleDirectory.toFile().listFiles()))
                                          .filter(File::isDirectory)
                                          .toList();

        for(File subdirectory : subdirectories) {
            try {
                ETCSModuleVersion etcsModuleVersion = new ETCSModuleVersion(subdirectory.getName());
                File[]            jarFiles          = subdirectory.listFiles((dir, name) -> name.endsWith(".jar"));

                if(jarFiles != null && jarFiles.length == 1) {
                    versionsAndJarFiles.put(etcsModuleVersion,
                                            Arrays.stream(jarFiles).findFirst().orElseThrow().toPath().getFileName().toString());
                }
            } catch(IllegalArgumentException ignored) {
            }
        }

        return versionsAndJarFiles;
    }

    public static String getJarNameFromPath(Path pathToJar) {
        String fileName = pathToJar.getFileName().toString();
        if(!fileName.endsWith(".jar")) {
            throw new IllegalArgumentException("Path does not point to a .jar file!");
        }

        return fileName;
    }

    public static File[] getSubdirectories(Path pathToDirectory) throws IllegalArgumentException {
        if(pathToDirectory.toFile().isDirectory()) {
            return pathToDirectory.toFile().listFiles(File::isDirectory);
        }
        else {
            throw new IllegalArgumentException(pathToDirectory + " is no directory or does not exist.");
        }
    }

    public static List<File> getFilesInDirectory(Path pathToDirectory) throws IOException {
        if(pathToDirectory.toFile().isDirectory()) {
            try(Stream<Path> paths = Files.walk(pathToDirectory, 1)) {
                return paths.filter(Files::isRegularFile).map(Path::toFile).toList();
            } catch(IOException e) {
                throw new IOException(e);
            }
        }
        else {
            throw new IllegalArgumentException("No such directory exists: " + pathToDirectory);
        }
    }

    public static void deleteFolder(@NotNull Path pathToFolder) throws IOException {
        if(pathToFolder.toFile().isDirectory()) {
            try(Stream<Path> pathStream = Files.walk(pathToFolder)) {
                pathStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                    try {
                        boolean deletedFile = file.delete();
                        if(!deletedFile) {
                            throw new IOException("Could not delete file " + file);
                        }
                    } catch(IOException ioException) {
                        try {
                            throw new IOException("Could not delete file " + file);
                        } catch(IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
        else {
            throw new IllegalArgumentException("Tried to delete '" + pathToFolder + "' which is not a folder.");
        }
    }

    /**
     * Copies a .jar from the file system of the user to the internal file system.
     *
     * @param jarPathInSystem
     *         the path to the .jar within the file system of the user
     * @param moduleName
     *         the name of the module that corresponds to the .jar
     * @param version
     *         the version of the module that corresponds to the .jar
     *
     * @throws IOException
     *         if the folder for this version could not be created and does not already exist
     */
    public static void copyJarFromFileSystemToInternalPath(Path jarPathInSystem,
                                                           @NotNull ETCSModuleNames.ModuleName moduleName,
                                                           String version)
            throws IOException {
        Path   jarDirectory = getDirectoryLogic(moduleName).resolve(version);
        String jarName      = getJarNameFromPath(jarPathInSystem);
        Path   jarPath      = jarDirectory.resolve(jarName);
        if(!jarDirectory.toFile().mkdirs()) {
            if(!jarDirectory.toFile().isDirectory()) {
                throw new IOException("Could not create directory " + jarDirectory);
            }
        }
        try {
            Files.copy(jarPathInSystem, jarPath);
        } catch(IOException e) {
            throw new IOException("Could not copy external .jar file for version " + version + " of module " +
                                  moduleName + "!");
        }
    }

}
