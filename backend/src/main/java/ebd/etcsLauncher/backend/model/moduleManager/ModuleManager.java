package ebd.etcsLauncher.backend.model.moduleManager;


import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.SortedETCSModuleArrayList;
import ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic;
import ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemUtils.copyJarFromFileSystemToInternalPath;
import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemUtils.getAvailableVersionsAndJarNamesFromFileSystem;

/**
 * This class manages the {@link ETCSModule}s that are available.
 *
 * @author Lukas Geyer
 */
@Repository
public class ModuleManager {

    private final Logger                    logger;
    private final String                    jdkPath;
    private final SortedETCSModuleArrayList availableModules;
    private final ReadWriteLock             availableModulesLock;

    public ModuleManager(@Value("${jdkPath}") String jdkPath) {
        this.jdkPath = jdkPath;
        this.logger = LoggerFactory.getLogger("ModuleManager");
        this.availableModules = new SortedETCSModuleArrayList();
        this.availableModulesLock = new ReentrantReadWriteLock();
    }

    /**
     * Scans the internal file system and looks for available {@link ETCSModule}s.
     */
    public void fillWithAvailableModules() {
        availableModules.clear();
        Map<ETCSModuleNames.ModuleName, Map<ETCSModuleVersion, String>> availableModuleVersions = getAvailableVersionsAndJarNamesFromFileSystem();

        for(Map.Entry<ETCSModuleNames.ModuleName, Map<ETCSModuleVersion, String>> entry : availableModuleVersions.entrySet()) {
            ETCSModuleNames.ModuleName     moduleName          = entry.getKey();
            Map<ETCSModuleVersion, String> versionsAndJarNames = entry.getValue();
            for(Map.Entry<ETCSModuleVersion, String> versionAndJarName : versionsAndJarNames.entrySet()) {
                ETCSModuleVersion version   = versionAndJarName.getKey();
                String            jarName   = versionAndJarName.getValue();
                ETCSModule        newModule = new ETCSModule(jdkPath, moduleName, version, jarName);

                newModule.getModificationLock().writeLock().lock();
                try {
                    newModule.createConfigFile();
                    newModule.indexConfigFile();
                } catch(Exception e) {
                    logger.warn("Could not create or index config file for module {}: {}. Ignoring module...", newModule, e.getMessage());
                    continue;
                } finally {
                    newModule.getModificationLock().writeLock().unlock();
                }

                availableModulesLock.writeLock().lock();
                availableModules.insertSorted(newModule);
                availableModulesLock.writeLock().unlock();
            }
        }
    }

    /**
     * Copys a .jar file from the file system of the user to the internal file system and adds the corresponding
     * {@link ETCSModule} to the {@link #availableModules}.
     *
     * @param moduleName
     *         the name of the {@link ETCSModule} to add to the {@link #availableModules}
     * @param moduleVersion
     *         the version of the {@link ETCSModule} to add to the {@link #availableModules}
     * @param jarPathInUserSystem
     *         the path of the .jar for the {@link ETCSModule}
     *
     * @return the new {@link ETCSModule} created or null if this {@link ETCSModule} could not be created.
     */
    public ETCSModule addModuleFromUserSystem(ETCSModuleNames.ModuleName moduleName, ETCSModuleVersion moduleVersion, Path jarPathInUserSystem) {
        if(!Files.isRegularFile(jarPathInUserSystem)) {
            logger.warn("File {} does not exist. Ignoring request...", jarPathInUserSystem.toAbsolutePath());
            return null;
        }
        String jarName;
        try {
            jarName = FileSystemUtils.getJarNameFromPath(jarPathInUserSystem);
        } catch(IllegalArgumentException illegalArgumentException) {
            logger.warn("File {} does not contain a .jar file. Ignoring request...", jarPathInUserSystem.toAbsolutePath());
            return null;
        }
        logger.info("Copying file {} to {}",
                    jarPathInUserSystem.toAbsolutePath(),
                    FileSystemLogic.getDirectoryLogic(moduleName).resolve(moduleVersion.get()).resolve(jarName).toAbsolutePath());

        if(FileSystemLogic.getDirectoryLogic(moduleName).resolve(moduleVersion.get()).toFile().isDirectory()) {
            if(Objects.requireNonNull(FileSystemLogic.getDirectoryLogic(moduleName)
                                                     .resolve(moduleVersion.get())
                                                     .toFile()
                                                     .listFiles((dir, name) -> name.endsWith(".jar"))).length > 0) {
                logger.warn("The directory {} already exists and contains a .jar file. Ignoring request...",
                            FileSystemLogic.getDirectoryLogic(moduleName).resolve(moduleVersion.get()).toAbsolutePath());
                return null;
            }
        }
        try {
            copyJarFromFileSystemToInternalPath(jarPathInUserSystem, moduleName, moduleVersion.get());
        } catch(IOException ioException) {
            logger.error("There was an error trying to copy file {} to {}: {}",
                         jarPathInUserSystem.toAbsolutePath(),
                         FileSystemLogic.getDirectoryLogic(moduleName).resolve(moduleVersion.get()).resolve(jarName).toAbsolutePath(),
                         ioException.getMessage());
            return null;
        }

        ETCSModule newModule = new ETCSModule(jdkPath, moduleName, moduleVersion, jarName);
        newModule.getModificationLock().writeLock().lock();
        try {
            newModule.createConfigFile();
            newModule.indexConfigFile();
        } catch(Exception e) {
            logger.warn("Could not create version '{}' of module {}: {} Deleting corresponding folder...",
                        moduleVersion.get(),
                        moduleName,
                        e.getMessage());
            deleteModuleFiles(newModule);
            return null;
        } finally {
            newModule.getModificationLock().writeLock().unlock();
        }

        availableModulesLock.writeLock().lock();
        availableModules.insertSorted(newModule);
        availableModulesLock.writeLock().unlock();
        logger.info("Added version {} of module {}", newModule.getVersion().get(), newModule.getModuleName());
        return newModule;
    }

    public SortedETCSModuleArrayList getAvailableModules() {
        try {
            availableModulesLock.readLock().lock();
            return (SortedETCSModuleArrayList) availableModules.clone();
        } finally {
            availableModulesLock.readLock().unlock();
        }
    }

    public @NotNull ETCSModule getAvailableModule(ETCSModuleNames.ModuleName moduleName, String version) throws IllegalArgumentException {
        if(availableModules.contains(moduleName, new ETCSModuleVersion(version))) {
            try {
                availableModulesLock.readLock().lock();
                return availableModules.get(moduleName, new ETCSModuleVersion(version));
            } finally {
                availableModulesLock.readLock().unlock();
            }
        }

        throw new IllegalArgumentException("Version " + version + " of module " + moduleName + " is not available!");
    }

    public boolean removeAvailableModule(ETCSModule module) {
        try {
            availableModulesLock.writeLock().lock();
            module.getModificationLock().writeLock().lock();
            if(availableModules.remove(module)) {
                if(deleteModuleFiles(module)) {
                    logger.info("Deleted {}.", module);
                    return true;
                }
                else {
                    logger.error("Could not delete module files of {}.", module);
                    return false;
                }
            }
            else {
                logger.error("Could not delete {}.", module);
                return false;
            }
        } finally {
            availableModulesLock.writeLock().unlock();
            module.getModificationLock().writeLock().unlock();
        }
    }

    public void removeAllAvailableModules() {
        List<ETCSModule> availableModulesCopy = new ArrayList<>(availableModules);
        availableModulesCopy.forEach(this::removeAvailableModule);
    }

    private boolean deleteModuleFiles(@NotNull ETCSModule module) {
        try {
            module.getModificationLock().writeLock().lock();
            return module.deleteModuleFiles();
        } finally {
            module.getModificationLock().writeLock().unlock();
        }
    }

}
