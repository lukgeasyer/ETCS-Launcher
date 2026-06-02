package ebd.etcsLauncher.backend.model.moduleManager

import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion
import ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

@SpringBootTest
class ModuleManagerSpecification extends Specification {
    @Autowired
    ModuleManager moduleManager

    def fillWithAvailable() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())

        moduleManager.fillWithAvailableModules()
    }

    def "specify correct behaviour of fillWithAvailableModules() and getAvailableModules() method"() {
        given:
        fillWithAvailable()

        when:
        def numberOfAvailableModules = moduleManager.getAvailableModules().size()
        def numberOfAvailableModulesNotInModulesToRun = moduleManager.getAvailableModules().size()
        moduleManager.fillWithAvailableModules()
        def numberOfAvailableModulesAfterScanningAgain = moduleManager.getAvailableModules().size()

        then:
        numberOfAvailableModules == 8
        numberOfAvailableModulesNotInModulesToRun == 8
        numberOfAvailableModulesAfterScanningAgain == 8
    }

    def "specify correct behaviour of getAvailableModule() method - legal parameters"() {
        given:
        fillWithAvailable()

        when:
        def nameOfAvailableModule = moduleManager.getAvailableModule(moduleName, version).getModuleName().toString()
        def versionOfAvailableModule = moduleManager.getAvailableModule(moduleName, version).getVersion().get()

        then:
        nameOfAvailableModule == expectedName
        versionOfAvailableModule == expectedVersion

        where:
        moduleName                       | version | expectedName | expectedVersion
        ETCSModuleNames.ModuleName.CORE  | "1.3.4" | "CORE"       | "1.3.4"
        ETCSModuleNames.ModuleName.CORE  | "1.6.3" | "CORE"       | "1.6.3"
        ETCSModuleNames.ModuleName.DMI   | "1.4.0" | "DMI"        | "1.4.0"
        ETCSModuleNames.ModuleName.Graph | "1.6.3" | "Graph"      | "1.6.3"

    }

    def "specify correct behaviour of getAvailableModule() method - illegal parameters"() {
        given:
        fillWithAvailable()

        when:
        moduleManager.getAvailableModule(moduleName, version).getModuleName().toString()
        moduleManager.getAvailableModule(moduleName, version).getVersion()

        then:
        thrown(IllegalArgumentException)

        where:
        moduleName                      | version
        ETCSModuleNames.ModuleName.CORE | "1.3.8"
        ETCSModuleNames.ModuleName.DMI  | "1.3.1"

    }

    def "specify correct behaviour of removeAvailableModule() method"() {
        given:
        fillWithAvailable()
        def module = moduleManager.getAvailableModules().get(i)

        when:
        def fileExisted = Files.isDirectory(FileSystemLogic.getDirectoryLogic(module.getModuleName()).resolve(module.getVersion().get()))
        def removeModule = moduleManager.removeAvailableModule(module)
        def fileExistsAfterRemoving = Files.isDirectory(FileSystemLogic.getDirectoryLogic(module.getModuleName()).resolve(module.getVersion().get()))
        def moduleExistsAfterRemoving = moduleManager.getAvailableModules().contains(module.moduleName, module.version)

        then:
        fileExisted
        removeModule
        !fileExistsAfterRemoving
        !moduleExistsAfterRemoving

        where:
        //noinspection GroovyAssignabilityCheck
        i << (0..7)
    }

    def "specify correct behaviour of removeAllAvailableModules() method"() {
        given:
        fillWithAvailable()

        when:
        moduleManager.removeAllAvailableModules()

        then:
        moduleManager.getAvailableModules().isEmpty()
    }

    def "specify correct behaviour of addModuleFromUserSystem() method - valid addition"() {
        when:
        def jarPath = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder()).resolve(moduleName.name()).resolve(moduleVersion).resolve(jarName)
        moduleManager.addModuleFromUserSystem(moduleName, new ETCSModuleVersion(moduleVersion), jarPath)

        then:
        moduleManager.getAvailableModules().contains(moduleName, new ETCSModuleVersion(moduleVersion))

        where:
        moduleName                       | moduleVersion | jarName
        ETCSModuleNames.ModuleName.CORE  | "1.3.4"       | "ETCS-Core-Programm-1.3.4.jar"
        ETCSModuleNames.ModuleName.CORE  | "1.6.3"       | "ETCS-Simulation-1.6.3.jar"
        ETCSModuleNames.ModuleName.DMI   | "1.4.0"       | "etcs_DMI-1.4.0.jar"
        ETCSModuleNames.ModuleName.Graph | "1.6.3"       | "ETCS-Graph-1.6.3-SNAPSHOT.jar"
    }

    def "specify correct behaviour of addModuleFromUserSystem() method - invalid addition"() {
        given:
        moduleManager.addModuleFromUserSystem(ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.4"), Path
                .of("src")
                .resolve("test")
                .resolve("resources")
                .resolve(getJarFolder())
                .resolve("CORE")
                .resolve("1.3.4")
                .resolve("ETCS-Core-Programm-1.3.4.jar"))

        when:
        def added = moduleManager.addModuleFromUserSystem(moduleName, new ETCSModuleVersion(moduleVersion), jarPath)

        then:
        added == null
        moduleManager.getAvailableModules().size() == 1

        where:
        moduleName                      | moduleVersion | jarPath
        ETCSModuleNames.ModuleName.CORE | "1.3.4"       | Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder()).resolve("CORE").resolve("1.3.4").resolve("ETCS-Core-Programm-1.3.4.jar")
        ETCSModuleNames.ModuleName.CORE | "1.3.4"       | Path.of("invalid")
        ETCSModuleNames.ModuleName.DMI  | "1.4.0"       | Path.of("src").resolve("test").resolve("resources").resolve("logback-test.xml")
    }

    def cleanup() {
        moduleManager.removeAllAvailableModules()
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}
