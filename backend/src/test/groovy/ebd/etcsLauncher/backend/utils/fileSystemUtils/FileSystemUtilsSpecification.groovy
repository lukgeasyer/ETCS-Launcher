package ebd.etcsLauncher.backend.utils.fileSystemUtils

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

class FileSystemUtilsSpecification extends Specification {
    def setup() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())
    }

    def "specify behaviour of getJarNameFromPath() function - valid path"() {
        when:
        def jarName = FileSystemUtils.getJarNameFromPath(jarPath)

        then:
        jarName == expectedJarName

        where:
        jarPath                                                                      | expectedJarName
        Path.of("user").resolve("CORE_1.3.4.jar")                                    | "CORE_1.3.4.jar"
        Path.of("user").resolve("desktop").resolve("etcs").resolve("CORE_1.3.4.jar") | "CORE_1.3.4.jar"
        Path.of("user").resolve("CORE.jar")                                          | "CORE.jar"
    }

    def "specify behaviour of getJarNameFromPath() function - invalid path"() {
        when:
        FileSystemUtils.getJarNameFromPath(jarPath)

        then:
        thrown(IllegalArgumentException)

        where:
        jarPath << [Path.of("user").resolve("desktop"), Path.of("user").resolve("CORE_1.3.4.jar").resolve("CORE.exe")]
    }

    def "specify correct behaviour of getSubdirectories() function"() {
        given:
        new ETCSModule("java", ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.3.4"), "ETCS-Core-Programm-1.3.4.jar").createConfigFile()

        when:
        def isSubDirectory = FileSystemUtils.getSubdirectories(path).contains(aSubdirectory)

        then:
        isSubDirectory == isActuallyASubdirectory

        where:
        path                                                     | aSubdirectory                                                                                             | isActuallyASubdirectory
        Path.of(getJarFolder())                                  | Path.of(getJarFolder()).resolve("CORE").toFile()                                                          | true
        Path.of(getJarFolder())                                  | Path.of(getJarFolder()).resolve("TMS").toFile()                                                           | true
        Path.of(getJarFolder())                                  | Path.of(getJarFolder()).resolve("DMI").toFile()                                                           | true
        Path.of(getJarFolder())                                  | Path.of(getJarFolder()).resolve("smartLogic").toFile()                                                    | false
        Path.of(getJarFolder()).resolve("CORE")                  | Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4").toFile()                                         | true
        Path.of(getJarFolder()).resolve("CORE")                  | Path.of(getJarFolder()).resolve("CORE").resolve("1.3.5").toFile()                                         | true
        Path.of(getJarFolder()).resolve("CORE")                  | Path.of(getJarFolder()).resolve("CORE").resolve("1.3.6").toFile()                                         | false
        Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4") | Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4").resolve("config").toFile()                       | true
        Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4") | Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4").resolve("ETCS-Core-Programm-1.3.4.jar").toFile() | false
    }

    def "specify correct behaviour of getFilesInDirectory() function - valid directory"() {
        given:
        new ETCSModule("java", ETCSModuleNames.ModuleName.CORE, new ETCSModuleVersion("1.6.3"), "ETCS-Simulation-1.6.3.jar").createConfigFile()

        when:
        def isFileInDirectory = FileSystemUtils.getFilesInDirectory(path).contains(aFile)

        then:
        isFileInDirectory == isActuallyFileInDirectory

        where:
        path                                                     | aFile                                                                                                  | isActuallyFileInDirectory
        Path.of(getJarFolder()).resolve("CORE").resolve("1.6.3") | Path.of(getJarFolder()).resolve("CORE").resolve("1.6.3").resolve("ETCS-Simulation-1.6.3.jar").toFile() | true
        Path.of(getJarFolder()).resolve("CORE").resolve("1.6.3") | Path.of(getJarFolder()).resolve("CORE").resolve("1.6.3").resolve("config").toFile()                    | false
    }

    def "specify correct behaviour of getFilesInDirectory() function - invalid directory"() {
        when:
        FileSystemUtils.getFilesInDirectory(invalidPath)

        then:
        thrown(IllegalArgumentException)

        where:
        invalidPath << [Path.of(getJarFolder()).resolve("smartTMS"), Path.of("user")]
    }

    def "specify correct behaviour of deleteFolder() function - deletion of actual folder"() {
        when:
        def folderExistedBefore = Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4"))
        FileSystemUtils.deleteFolder(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4"))
        def folderDeleted = !Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4"))

        then:
        folderExistedBefore
        folderDeleted
    }

    def "specify correct behaviour of deleteFolder() function - deletion of non existing folder"() {
        when:
        FileSystemUtils.deleteFolder(Path.of("notAFolder"))

        then:
        thrown(IllegalArgumentException)
    }

    def "specify correct behaviour of copyJarFromFileSystemToInternalPath() function - jar exists"() {
        when:
        new File("jarFiles/CORE/1.6.3/ETCS-Simulation-1.6.3.jar").delete()
        Path path = Paths.get("src/test/resources/jarFiles/CORE/1.6.3/ETCS-Simulation-1.6.3.jar")
        FileSystemUtils.copyJarFromFileSystemToInternalPath(path, "CORE" as ETCSModuleNames.ModuleName, "1.6.3")

        then:
        Files.isRegularFile(Path.of(getJarFolder()).resolve("CORE").resolve("1.6.3").resolve("ETCS-Simulation-1.6.3.jar"))
    }

    def "specify correct behaviour of copyJarFromFileSystemToInternalPath() function - jar does not exist"() {
        given:
        Path path = Paths.get("src/test/resources/jarFiles/DMI/1.4.0/etcs_DMI-1.4.0-RELEASE.jar")

        when:
        FileSystemUtils.copyJarFromFileSystemToInternalPath(path, "DMI" as ETCSModuleNames.ModuleName, "1.4.0")

        then:
        thrown(IOException)
    }

    def "specify correct behaviour of copyJarFromFileSystemToInternalPath() function - invalid path"() {
        given:
        Path invalidPath = Paths.get("src/main/invalid")

        when:
        FileSystemUtils.copyJarFromFileSystemToInternalPath(invalidPath, "DMI" as ETCSModuleNames.ModuleName, "1.4.0")

        then:
        thrown(IllegalArgumentException)
    }

    def cleanup() {
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}
