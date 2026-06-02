package ebd.etcsLauncher.backend.model.etcsModule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleLogics;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion;
import ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleLogics.getConfigLogic;
import static ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleLogics.requiresCLIParamForConfigFile;
import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getDirectoryLogic;
import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemUtils.deleteFolder;

/**
 * This class represents a wrapper around a .jar file from one of the supported ETCS-Modules (see {@link ETCSModuleNames}
 * for supported modules). It stores information about the name of the module, its version, its config values and command line arguments.
 * The config file for the module will be extracted from the provided .jar file and its entries are stored in a {@link TreeMap}.
 *
 * @author Lukas Geyer
 */
public class ETCSModule implements Comparable<ETCSModule> {

    @JsonIgnore
    private final Logger                     logger;
    @JsonIgnore
    private final String                     jdkPath;
    private final String                     jarName;
    private final ETCSModuleNames.ModuleName moduleName;
    private final ETCSModuleVersion          version;
    @JsonView
    private final Path                       configDirectory;
    private final TreeMap<String, String>    configValues;
    private final ArrayList<String>          commandLineArguments;
    @JsonIgnore
    private final ReadWriteLock              modificationLock;

    /**
     * Creates a new {@link ETCSModule}. Expects the .jar for this module to lie in the correct directory.
     * See {@link FileSystemLogic} for information about the logic of the internal file system.
     *
     * @param moduleName
     *         {@link ETCSModuleNames.ModuleName} of the module
     * @param version
     *         version of the module in the X.Y.Z format
     * @param jarName
     *         name of the .jar file associated with this {@link ETCSModule}
     */
    public ETCSModule(String jdkPath, ETCSModuleNames.@NotNull ModuleName moduleName, ETCSModuleVersion version, String jarName) {
        this.logger = LoggerFactory.getLogger(moduleName + "_" + version.get());
        this.jdkPath = jdkPath;
        this.moduleName = moduleName;
        this.version = version;
        this.jarName = jarName;
        this.configDirectory = FileSystemLogic.getDirectoryLogic(this.moduleName)
                                              .resolve(version.get())
                                              .resolve(getConfigLogic(this.moduleName))
                                              .toAbsolutePath();
        this.configValues = new TreeMap<>();
        this.commandLineArguments = new ArrayList<>();
        modificationLock = new ReentrantReadWriteLock();
    }


    /**
     * Extracts the config file for the module from the .jar and copies the config file to the location
     * the .jar expects the config file to be.
     *
     * @throws IOException
     *         if
     *         - the directory for the config file could not be created
     *         - the .jar file could not be read from
     *         - the config file could not be created
     */
    public void createConfigFile() throws IOException, InterruptedException {

        if(Files.isRegularFile(configDirectory.resolve(ETCSModuleLogics.getNameOfConfigFile(this.moduleName)))) {
            return;
        }
        if(requiresCLIParamForConfigFile(this)) {
            extractConfigFileWithCLIParam();
        }
        else {
            if(!configDirectory.toFile().mkdirs()) {
                if(!configDirectory.toFile().isDirectory()) {
                    throw new IOException("Could not create directory: " + configDirectory.toAbsolutePath());
                }
            }

            try(JarFile moduleJar = new JarFile(FileSystemLogic.getDirectoryLogic(moduleName).resolve(version.get()).resolve(jarName).toString())) {
                Stream<JarEntry> jarEntriesStream = moduleJar.stream();
                ZipEntry configFileEntry = jarEntriesStream
                        .filter(jarEntry -> jarEntry.getName().contains(ETCSModuleLogics.getNameOfInternalConfigFile(this.moduleName)))
                        .findFirst()
                        .orElseThrow();
                InputStream jarInputStream = moduleJar.getInputStream(configFileEntry);
                try(OutputStream configFileOutputStream = new FileOutputStream(configDirectory.resolve(ETCSModuleLogics.getNameOfConfigFile(this.moduleName))
                                                                                              .toFile())) {
                    byte[] buffer = new byte[1024];
                    int    bytesRead;

                    while((bytesRead = jarInputStream.read(buffer)) != -1) {
                        configFileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        logger.info("Extracted config file: {}",
                    configDirectory.resolve(ETCSModuleLogics.getNameOfConfigFile(this.moduleName)).toFile().getAbsolutePath());
    }

    private void extractConfigFileWithCLIParam() throws IOException, InterruptedException {
        ArrayList<String> commandLineArguments = new ArrayList<>();
        commandLineArguments.add(jdkPath);
        commandLineArguments.add("-jar");
        commandLineArguments.add(this.getJarName());
        commandLineArguments.add("-genPropFileOnly");
        ProcessBuilder extractionProcessBuilder = new ProcessBuilder(commandLineArguments);
        Path           jarPath                  = getDirectoryLogic(this.getModuleName()).resolve(Path.of(this.getVersion().get()));
        extractionProcessBuilder.directory(jarPath.toFile());
        Process extractionProcess = extractionProcessBuilder.start();
        extractionProcess.waitFor(10, TimeUnit.SECONDS);
    }


    /**
     * Indexes the config file with a {@link TreeMap} that stores the name of the variables and their values as a {@link String}.
     *
     * @throws FileNotFoundException
     *         if the config file does not exist in the internal file system.
     * @throws IOException
     *         if there was an error reading the config file.
     */
    public void indexConfigFile() throws FileNotFoundException, IOException {
        try(BufferedReader configFileReader = new BufferedReader(new FileReader(configDirectory.resolve(ETCSModuleLogics.getNameOfConfigFile(this.moduleName))
                                                                                               .toFile()))) {
            configFileReader.lines().forEach(line -> {
                if(!line.isEmpty() && line.charAt(0) != '#') {
                    String key = line.split("=")[0].replaceAll("\\s", "");
                    if(line.split("=").length == 1) {
                        configValues.put(key, "");
                    }
                    else {
                        String value = line.split("=")[1].replaceAll("\\s", "");
                        configValues.put(key, value);
                    }
                }
            });
        } catch(FileNotFoundException fileNotFoundException) {
            logger.error("Config file not found: {}", fileNotFoundException.getMessage(), fileNotFoundException);
            throw new FileNotFoundException(fileNotFoundException.getMessage());
        } catch(IOException ioException) {
            logger.error("Error reading the config file: {}", ioException.getMessage(), ioException);
            throw new IOException(ioException.getMessage());
        }
    }

    public ETCSModuleNames.ModuleName getModuleName() {
        return moduleName;
    }

    public ETCSModuleVersion getVersion() {
        return version;
    }

    public String getJarName() {
        return jarName;
    }

    public TreeMap<String, String> getConfigValues() {
        return configValues;
    }

    public ArrayList<String> getCommandLineArguments() {
        return commandLineArguments;
    }

    public void setCommandLineArguments(List<String> commandLineArguments) {
        this.commandLineArguments.clear();
        this.commandLineArguments.addAll(commandLineArguments);
        logger.info("Set command line arguments: {}", commandLineArguments);
    }

    public ReadWriteLock getModificationLock() {
        return modificationLock;
    }

    /**
     * Changes the value of a config variable both in {@link #configValues} and in the config file itself.
     * The config file will be changed by creating a temporary copy of the config file
     * with the new value and deleting the old config file.
     * Deletes all config file entries if there was a problem accessing the config file or changing it.
     *
     * @param key
     *         the name of the config variable to change
     * @param value
     *         the new value of the config variable
     *
     * @return true if the change was successful, false otherwise
     */
    public boolean changeConfigVariable(String key, String value) {
        String oldValue = configValues.replace(key, value);
        if(oldValue != null) {
            Path tempFilePath      = configDirectory.resolve("temp_" + ETCSModuleLogics.getNameOfConfigFile(this.moduleName));
            Path oldConfigFilePath = configDirectory.resolve(ETCSModuleLogics.getNameOfConfigFile(this.moduleName));

            try {
                replaceLineInConfigFile(key, value, tempFilePath);
            } catch(IOException ioException) {
                logger.warn("Could not find config file! Deleting saved config entries...");
                configValues.clear();
                return false;
            }

            boolean deleted     = oldConfigFilePath.toFile().delete();
            int     maxAttempts = 3;
            int     attempts    = 0;

            while(!deleted && attempts < maxAttempts) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException interruptedException) {
                    logger.warn("Waiting for another try to delete config file was interrupted!");
                }

                deleted = oldConfigFilePath.toFile().delete();
                attempts++;
            }

            if(Files.isRegularFile(oldConfigFilePath)) {
                logger.error("Could not delete {}", oldConfigFilePath);
                logger.error("Deleting saved config entries...");
                configValues.clear();
                return false;
            }
            if(!tempFilePath.toFile().renameTo(oldConfigFilePath.toFile())) {
                logger.error("Could not rename {} to {}", tempFilePath.toFile(), oldConfigFilePath.toFile());
                logger.error("Deleting saved config entries...");
                configValues.clear();
                return false;
            }
            logger.info("Changed config file entry. Variable name: '{}', new value: {}", key, value);
            return true;
        }
        return false;
    }

    /**
     * Changes a specific line of a file. This line is filtered by looking for the occurrence of a given key.
     * Ignores comments with #.
     *
     * @param key
     *         the name of the config variable to change
     * @param value
     *         the new value of the config variable
     * @param tempFilePath
     *         the {@link Path} of the temporary config file
     *
     * @throws IOException
     *         if the config file could not be read from or the temporary copy could not be written to.
     */
    private void replaceLineInConfigFile(String key, String value, Path tempFilePath) throws IOException {
        InputStream configInputStream = new FileInputStream(configDirectory.resolve(ETCSModuleLogics.getNameOfConfigFile(this.moduleName))
                                                                           .toFile());
        OutputStream   tempConfigOutputStream = new FileOutputStream(tempFilePath.toFile());
        BufferedReader reader                 = new BufferedReader(new InputStreamReader(configInputStream, StandardCharsets.UTF_8));
        BufferedWriter writer                 = new BufferedWriter(new OutputStreamWriter(tempConfigOutputStream, StandardCharsets.UTF_8));

        String line;
        while((line = reader.readLine()) != null) {
            if(!line.isEmpty() && !line.startsWith("#") && line.contains(key)) {
                line = key + "=" + value;
            }
            writer.write(line);
            writer.write(System.lineSeparator());
        }

        writer.flush();
        writer.close();
        reader.close();
    }


    /**
     * Deletes the corresponding folder to this {@link ETCSModule}.
     *
     * @return true if the deletion of the corresponding folder was successful, false otherwise
     */
    public boolean deleteModuleFiles() {
        try {
            deleteFolder(FileSystemLogic.getDirectoryLogic(this.moduleName).resolve(version.get()));
            logger.info("Deleted folder: {}", FileSystemLogic.getDirectoryLogic(this.moduleName).resolve(version.get()).toAbsolutePath());
            return true;
        } catch(Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass().equals(ETCSModule.class)) {
            return Objects.equals(((ETCSModule) o).moduleName, this.moduleName) && Objects.equals(((ETCSModule) o).getVersion(), this.version)
                   && Objects.equals(((ETCSModule) o).jarName, this.jarName);
        }
        return super.equals(o);
    }

    public int hashCode() {
        return this.moduleName.hashCode();
    }

    @Override
    public String toString() {
        return "Module: " + this.getModuleName().name() + ", Version: " + this.getVersion().get();
    }

    /**
     * Compares two {@link ETCSModule}s. The comparison first considers the {@link #moduleName} and then the {@link ETCSModuleVersion}.
     * The ordering of the {@link #moduleName} is equal to the ordering of {@link ETCSModuleNames.ModuleName}.
     *
     * @param etcsModule
     *         the {@link ETCSModule} to compare this {@link ETCSModule} to.
     *
     * @return a negative integer, zero, or a positive integer as this {@link ETCSModule} is less than, equal to, or greater
     * than the specified {@link ETCSModule}.
     */
    @Override
    public int compareTo(@NotNull ETCSModule etcsModule) {
        if(etcsModule.moduleName != this.moduleName) {
            return this.moduleName.compareTo(etcsModule.moduleName);
        }
        return this.version.compareTo(etcsModule.version);
    }

}


