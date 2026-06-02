//file:noinspection SpellCheckingInspection
package ebd.etcsLauncher.backend.model.etcsModule

import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleLogics
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion
import ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

class ETCSModuleSpecification extends Specification {
    ArrayList<ETCSModule> moduleList = new ArrayList<>()
    def jdkPath = "java"

    def copyJarFilesFromResources() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(FileSystemLogic.getJarFolder())
        Path tempTestDirectory = Path.of(FileSystemLogic.getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())
    }

    def createModuleList() {
        def core1_3_4 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.4"), "ETCS-Core-Programm-1.3.4.jar")
        def core1_3_5 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.5"), "ETCS-Core-Programm-1.3.5.jar")
        def core1_6_3 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.6.3"), "ETCS-Simulation-1.6.3.jar")
        def dmi1_4_0 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.DMI, new ETCSModuleVersion("1.4.0"), "etcs_DMI-1.4.0.jar")
        def graph1_6_3 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.Graph, new ETCSModuleVersion("1.6.3"), "ETCS-Graph-1.6.3-SNAPSHOT.jar")
        def scs1_0_5 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.ScenarioControlServer, new ETCSModuleVersion("1.0.5"), "scenarioControlServer-1.0.5-SNAPSHOT.jar")
        def smartLogic1_3_52 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.smartLogic, new ETCSModuleVersion("1.3.52"), "logic_core-1.3.52.jar")
        def tms1_3_26 = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.smartTMS, new ETCSModuleVersion("1.3.26"), "tms_core-1.3.26-SNAPSHOT.jar")

        moduleList.add(core1_3_4)
        moduleList.add(core1_3_5)
        moduleList.add(core1_6_3)
        moduleList.add(dmi1_4_0)
        moduleList.add(graph1_6_3)
        moduleList.add(scs1_0_5)
        moduleList.add(smartLogic1_3_52)
        moduleList.add(tms1_3_26)
    }

    def "specify correct behaviour of getModuleName() method"() {
        given:
        createModuleList()

        when:
        def moduleName = moduleList.get(i).getModuleName()

        then:
        moduleName == expectedModuleName

        where:
        i | expectedModuleName
        0 | ETCSModuleNames.ModuleName.CORE
        1 | ETCSModuleNames.ModuleName.CORE
        2 | ETCSModuleNames.ModuleName.CORE
        3 | ETCSModuleNames.ModuleName.DMI
        4 | ETCSModuleNames.ModuleName.Graph
        5 | ETCSModuleNames.ModuleName.ScenarioControlServer
        6 | ETCSModuleNames.ModuleName.smartLogic
        7 | ETCSModuleNames.ModuleName.smartTMS
    }

    def "specify correct behaviour of createConfigFile - .jar available"() {
        given:
        copyJarFilesFromResources()
        createModuleList()

        when:
        moduleList.forEach { it.createConfigFile() }

        then:
        def configPath = FileSystemLogic.getDirectoryLogic(moduleName).resolve(moduleVersion).resolve(ETCSModuleLogics.getConfigLogic(moduleName))
        Files.isRegularFile(configPath.resolve(ETCSModuleLogics.getNameOfConfigFile(moduleName)))

        where:
        moduleName                                       | moduleVersion
        ETCSModuleNames.ModuleName.CORE                  | "1.3.4"
        ETCSModuleNames.ModuleName.CORE                  | "1.3.5"
        ETCSModuleNames.ModuleName.CORE                  | "1.6.3"
        ETCSModuleNames.ModuleName.DMI                   | "1.4.0"
        ETCSModuleNames.ModuleName.Graph                 | "1.6.3"
        ETCSModuleNames.ModuleName.ScenarioControlServer | "1.0.5"
        ETCSModuleNames.ModuleName.smartLogic            | "1.3.52"
        ETCSModuleNames.ModuleName.smartTMS              | "1.3.26"
    }

    def "specify correct behaviour of createConfigFile -  no.jar available"() {
        when:
        new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.4"), "core_.jar").createConfigFile()

        then:
        thrown(NoSuchFileException)
    }

    def "specfify correct behaviour of indexConfigFile - file available"() {
        given:
        copyJarFilesFromResources()
        createModuleList()
        def configPath = FileSystemLogic.getDirectoryLogic(moduleList.get(i).getModuleName()).resolve(moduleList.get(i).getVersion().get()).resolve(ETCSModuleLogics.getConfigLogic(moduleList.get(i).getModuleName()))

        when: "creating and indexing config file"
        moduleList.forEach { it.createConfigFile() }
        moduleList.forEach { it.indexConfigFile() }

        then: "the variables of the file should be stored in the map and the index file should be accessible and deletable"
        !moduleList.get(i).getConfigValues().isEmpty()
        Files.isRegularFile(configPath.resolve(ETCSModuleLogics.getNameOfConfigFile(moduleList.get(i).getModuleName())))
        configPath.resolve(ETCSModuleLogics.getNameOfConfigFile(moduleList.get(i).getModuleName())).toFile().delete()

        where:
        //noinspection GroovyAssignabilityCheck
        i << (0..7)
    }

    def "specify correct behaviour of indexConfigFile - no file available"() {
        given:
        copyJarFilesFromResources()
        createModuleList()

        when:
        moduleList.get(0).indexConfigFile()

        then:
        thrown(FileNotFoundException)
    }

    def "specify correct behavior of setCommandLineArguments() method"() {
        given:
        createModuleList()
        def commandLineArguments = new ArrayList()
        commandLineArguments.add("headless")
        commandLineArguments.add("trainID=1")

        when:
        moduleList.get(i).setCommandLineArguments(commandLineArguments)
        def newArguments = (ArrayList) moduleList.get(i).getCommandLineArguments().clone()
        moduleList.get(i).setCommandLineArguments(new ArrayList<String>())
        def noArguments = moduleList.get(i).getCommandLineArguments()

        then:
        newArguments.contains("headless")
        newArguments.contains("trainID=1")
        noArguments.isEmpty()

        where:
        //noinspection GroovyAssignabilityCheck
        i << (0..7)
    }

    def "specify correct behaviour of changeConfigVariable() method - config file available"() {
        given:
        copyJarFilesFromResources()
        createModuleList()
        moduleList.forEach { it.createConfigFile() }
        moduleList.forEach { it.indexConfigFile() }

        when:
        def changedVariable = moduleList.get(i).changeConfigVariable(variable, newValue)

        then:
        changedVariable == changeExpected


        where:
        i | variable                              | newValue      | changeExpected
        0 | "train.feedbackLoop.FBLLogMultiplier" | "3"           | true
        0 | "noChange"                            | "3"           | false
        1 | "train.feedbackLoop.FBLLogMultiplier" | "3"           | true
        1 | "noChange"                            | "3"           | false
        2 | "train.feedbackLoop.FBLLogMultiplier" | "3"           | true
        2 | "noChange"                            | "3"           | false
        3 | "dmiServerPort"                       | "192.168.0.1" | true
        3 | "noChange"                            | "192.168.0.1" | false
        4 | "etcsgraph.headless"                  | "true"        | true
        4 | "noChange"                            | "true"        | false
        5 | "timeAccFactor"                       | "10"          | true
        5 | "noChange"                            | "10"          | false
        6 | "config.handler.portOfGUIServer4TMS"  | "8080"        | true
        6 | "noChange"                            | "8080"        | false
        7 | "tms.config.sendMotisFiles"           | "true"        | true
        7 | "noChange"                            | "true"        | false
    }

    def "specify correct behaviour of changeConfigVariable() method - no config file available"() {
        given:
        copyJarFilesFromResources()
        createModuleList()

        when:
        def changedVariable = moduleList.get(0).changeConfigVariable("train.feedbackLoop.FBLLogMultiplier", "3")

        then:
        !changedVariable
    }

    def "specify correct behaviour of changeConfigVariable() method - config file deleted after indexing"() {
        given:
        copyJarFilesFromResources()
        createModuleList()
        def moduleName = moduleList.get(0).moduleName
        def moduleVersion = moduleList.get(0).getVersion()
        moduleList.get(0).createConfigFile()
        moduleList.get(0).indexConfigFile()
        def configPath = FileSystemLogic.getDirectoryLogic(moduleName).resolve(moduleVersion.get()).resolve(ETCSModuleLogics.getConfigLogic(moduleName))
        configPath.resolve(ETCSModuleLogics.getNameOfConfigFile(moduleName)).toFile().delete()

        when:
        def changedVariable = moduleList.get(0).changeConfigVariable("train.feedbackLoop.FBLLogMultiplier", "3")

        then:
        !changedVariable
    }


    def "specify correct behaviour of equals()"() {
        given:
        createModuleList()

        when:
        def equals = moduleList.get(i) == moduleList.get(j)

        then:
        equals == expectedComparison

        where:
        i | j | expectedComparison
        0 | 0 | true
        0 | 1 | false
        0 | 2 | false
        0 | 3 | false
        0 | 4 | false
        0 | 5 | false
        0 | 6 | false
        0 | 7 | false
    }

    def "specify correct behaviour of equals() - different Object but is equal"() {
        when:
        def equals = new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.4"), "core.jar") == new ETCSModule(jdkPath, ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.4"), "core.jar")

        then:
        equals
    }

    def "specify correct behaviour of compareTo()"() {
        given:
        createModuleList()

        when:
        def comparison = moduleList.get(i) <=> moduleList.get(j)

        then:
        comparison == expectedComparison

        where:
        i | j | expectedComparison
        0 | 0 | 0
        0 | 1 | -1
        0 | 2 | -1
        0 | 3 | -1
        0 | 4 | -1
        0 | 5 | -1
        0 | 6 | -1
        0 | 7 | -1
    }

    def "specify correct behaviour of deleteModuleFiles() method"() {
        given:
        copyJarFilesFromResources()
        createModuleList()

        when:
        def module = moduleList.get(i)
        def deletedModuleFiles = module.deleteModuleFiles()
        def moduleFilesExist = Files.isDirectory(FileSystemLogic.getDirectoryLogic(module.getModuleName()).resolve(module.getVersion().get()))

        then:
        deletedModuleFiles
        !moduleFilesExist

        where:
        //noinspection GroovyAssignabilityCheck
        i << (0..7)
    }

    def cleanup() {
        Path.of(FileSystemLogic.getJarFolder()).toFile().deleteDir()
    }
}
