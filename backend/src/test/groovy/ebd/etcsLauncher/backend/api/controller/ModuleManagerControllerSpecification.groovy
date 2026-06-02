package ebd.etcsLauncher.backend.api.controller

import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModuleManagerControllerSpecification extends Specification {
    @LocalServerPort
    private int port
    private TestRestTemplate restTemplate = new TestRestTemplate()
    @Autowired
    ModuleManagerController moduleManagerController

    def fillWithAvailable() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())
        restTemplate.postForEntity("http://localhost:$port/moduleManager/availableModules/scanFileSystem", null, String)
    }

    def "specify correct behaviour of scanForAvailableModules() method - modules available"() {
        given:
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())

        when:
        def scanAvailableModulesResponse = restTemplate
                .postForEntity("http://localhost:$port/moduleManager/availableModules/scanFileSystem", null, String)

        then:
        scanAvailableModulesResponse.statusCode == HttpStatus.OK
        scanAvailableModulesResponse.hasBody()
        scanAvailableModulesResponse.body.contains(availableModuleVersion)

        where:
        availableModuleVersion << ["1.3.4", "1.3.5", "1.6.3", "1.6.3", "1.4.0", "1.0.5", "1.3.52", "1.3.26"]
    }

    def "specify correct behaviour of scanForAvailableModules() method - no modules available"() {
        when:
        def scanAvailableModulesResponse = restTemplate
                .postForEntity("http://localhost:$port/moduleManager/availableModules/scanFileSystem", null, String)

        then:
        scanAvailableModulesResponse.statusCode == HttpStatus.OK
        scanAvailableModulesResponse.hasBody()
        scanAvailableModulesResponse.body == "[]"
    }

    def "specify correct behaviour of getAvailableModules() method - modules available"() {
        given:
        fillWithAvailable()

        when:
        def getAvailableModulesResponse = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        getAvailableModulesResponse.statusCode == HttpStatus.OK
        getAvailableModulesResponse.hasBody()
        getAvailableModulesResponse.body.contains(availableModuleVersion)

        where:
        availableModuleVersion << ["1.3.4", "1.3.5", "1.6.3", "1.6.3", "1.4.0", "1.0.5", "1.3.52", "1.3.26"]
    }

    def "specify correct behaviour of getAvailableModules() method - no modules available"() {
        when:
        def getAvailableModulesResponse = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        getAvailableModulesResponse.statusCode == HttpStatus.OK
        getAvailableModulesResponse.hasBody()
        getAvailableModulesResponse.body == "[]"
    }

    def "specify correct behaviour of deleteModule() method"() {
        given:
        fillWithAvailable()

        when:
        def deleteNonExistingModuleResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/CORE/1.3.6", HttpMethod.DELETE, null, String)
        def deleteModuleResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/CORE/1.3.4", HttpMethod.DELETE, null, String)
        def getAvailableModulesAfterDeletionResponse = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        deleteNonExistingModuleResponse.statusCode == HttpStatus.BAD_REQUEST
        deleteModuleResponse.statusCode == HttpStatus.NO_CONTENT
        getAvailableModulesAfterDeletionResponse.statusCode == HttpStatus.OK
        !getAvailableModulesAfterDeletionResponse.body.contains("1.3.4")
        getAvailableModulesAfterDeletionResponse.body.contains("1.3.5")
        !Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4"))
        Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.5"))
    }

    def "specify correct behavior of addAvailableModule() method - valid request with CORE 1.6.3"() {
        given:
        def getAvailableModulesBeforeCreation = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)
        def jarPath = "src/test/resources/" + getJarFolder() + "/CORE/1.6.3/ETCS-Simulation-1.6.3.jar"
        def addAvailableModuleRequest = "http://localhost:$port/moduleManager/availableModules/CORE/1.6.3?pathToJar=" + jarPath

        when:
        def addAvailableModuleResponse = restTemplate.exchange(addAvailableModuleRequest, HttpMethod.PUT, null, String)
        def getAvailableModulesAfterCreation = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        !getAvailableModulesBeforeCreation.body.contains("1.6.3")
        addAvailableModuleResponse.statusCode == HttpStatus.CREATED
        getAvailableModulesAfterCreation.body.contains("1.6.3")
    }

    def "specify correct behavior of addAvailableModule() method - valid request with Graph 1.6.3"() {
        given:
        def getAvailableModulesBeforeCreation = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)
        def jarPath = "src/test/resources/" + getJarFolder() + "/Graph/1.6.3/ETCS-Graph-1.6.3-SNAPSHOT.jar"
        def addAvailableModuleRequest = "http://localhost:$port/moduleManager/availableModules/Graph/1.6.3?pathToJar=" + jarPath

        when:
        def addAvailableModuleResponse = restTemplate.exchange(addAvailableModuleRequest, HttpMethod.PUT, null, String)
        def getAvailableModulesAfterCreation = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        !getAvailableModulesBeforeCreation.body.contains("1.6.3")
        addAvailableModuleResponse.statusCode == HttpStatus.CREATED
        getAvailableModulesAfterCreation.body.contains("1.6.3")
    }

    def "specify correct behaviour of deleteModule() method - multiple calls in short time"() {
        given:
        fillWithAvailable()

        when:
        def deleteCORE1Response = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/CORE/1.3.4", HttpMethod.DELETE, null, String)
        def deleteCORE2Response = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/CORE/1.3.5", HttpMethod.DELETE, null, String)
        def deleteCORE3Response = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/CORE/1.6.3", HttpMethod.DELETE, null, String)
        def deleteDMIResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/DMI/1.4.0", HttpMethod.DELETE, null, String)
        def deleteGraphResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/Graph/1.6.3", HttpMethod.DELETE, null, String)
        def deletesmartLogicResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/smartLogic/1.3.52", HttpMethod.DELETE, null, String)
        def deletesmartTMSResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/smartTMS/1.3.26", HttpMethod.DELETE, null, String)
        def deleteSCSResponse = restTemplate.exchange("http://localhost:$port/moduleManager/availableModules/scs/1.0.5", HttpMethod.DELETE, null, String)
        def getAvailableModulesAfterDeletionResponse = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        deleteCORE1Response.statusCode == HttpStatus.NO_CONTENT
        deleteCORE2Response.statusCode == HttpStatus.NO_CONTENT
        deleteCORE3Response.statusCode == HttpStatus.NO_CONTENT
        deleteDMIResponse.statusCode == HttpStatus.NO_CONTENT
        deleteGraphResponse.statusCode == HttpStatus.NO_CONTENT
        deletesmartLogicResponse.statusCode == HttpStatus.NO_CONTENT
        deletesmartTMSResponse.statusCode == HttpStatus.NO_CONTENT
        deleteSCSResponse.statusCode == HttpStatus.NO_CONTENT

        getAvailableModulesAfterDeletionResponse.statusCode == HttpStatus.OK
        getAvailableModulesAfterDeletionResponse.body == "[]"
        !Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.4"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.3.5"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("CORE").resolve("1.6.3"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("DMI").resolve("1.4.0"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("Graph").resolve("1.6.3"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("TMS").resolve("smartLogic").resolve("1.3.52"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("TMS").resolve("smartTMS").resolve("1.3.26"))
        !Files.isDirectory(Path.of(getJarFolder()).resolve("TMS").resolve("ScenarioControlServer").resolve("1.0.5"))
    }

    def "specify correct behavior of addAvailableModule() method - invalid request"() {
        given:
        def addCoreRequest = "http://localhost:$port/moduleManager/availableModules/CORE/1.3.4?pathToJar=src/test/resources/" + getJarFolder() + "/CORE/1.3.4/ETCS-Core-Programm-1.3.4.jar"
        restTemplate.exchange(addCoreRequest, HttpMethod.PUT, null, String)
        def addAvailableModuleRequest = "http://localhost:$port/moduleManager/availableModules/" + moduleName + "/" + moduleVersion + "?pathToJar=" + jarPath

        when:
        def addAvailableModuleResponse = restTemplate.exchange(addAvailableModuleRequest, HttpMethod.PUT, null, String)

        then:
        addAvailableModuleResponse.statusCode == HttpStatus.BAD_REQUEST

        where:
        moduleName                      | moduleVersion | jarPath
        ETCSModuleNames.ModuleName.CORE | "1.3.4"       | "src/test/resources/" + getJarFolder() + "/CORE/1.3.4/ETCS-Core-Programm-1.3.4.jar"
        ETCSModuleNames.ModuleName.CORE | "1.3.4"       | "invalid"
        ETCSModuleNames.ModuleName.CORE | "invalid"     | "src/test/resources/" + getJarFolder() + "/CORE/1.3.5/ETCS-Core-Programm-1.3.5.jar"
        ETCSModuleNames.ModuleName.DMI  | "1.4.0"       | "src/test/resources/logback-test.xml"
    }

    def "specify correct behaviour of getConfigValues() method - no search key"() {
        given:
        fillWithAvailable()
        def getConfigValuesRequest = "http://localhost:$port/moduleManager/availableModules/" + moduleName + "/" + moduleVersion + "/configValues"

        when:
        def getConfigValuesResponse = restTemplate.getForEntity(getConfigValuesRequest, String)

        then:
        getConfigValuesResponse.statusCode == HttpStatus.OK
        getConfigValuesResponse.hasBody()
        getConfigValuesResponse.body.toString().contains(matchingVariable)

        where:
        moduleName   | moduleVersion | matchingVariable
        "CORE"       | "1.3.4"       | "train.feedbackLoop.FBLLogMultiplier"
        "CORE"       | "1.3.5"       | "train.feedbackLoop.FBLLogMultiplier"
        "CORE"       | "1.6.3"       | "train.feedbackLoop.FBLLogMultiplier"
        "dmi"        | "1.4.0"       | "dmiServerPort"
        "Graph"      | "1.6.3"       | "etcsgraph.headless"
        "scs"        | "1.0.5"       | "timeAccFactor"
        "smarttms"   | "1.3.26"      | "tms.config.sendMotisFiles"
        "smartLogic" | "1.3.52"      | "config.handler.portOfGUIServer4TMS"
    }

    def "specify correct behaviour of changeConfigValue() method"() {
        given:
        fillWithAvailable()
        def changeConfigValueRequest = "http://localhost:$port/moduleManager/availableModules/" + moduleName + "/" + moduleVersion + "/configValues?variableName=" +
                variableName + "&newValue=" + newValue

        when:
        def configValueBeforeChange = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules/" + moduleName + "/" + moduleVersion + "/configValues?searchKey=" + variableName, String)
        def changeConfigValueResponse = restTemplate.exchange(changeConfigValueRequest, HttpMethod.PUT, null, String)
        def configValueAfterChange = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules/" + moduleName + "/" + moduleVersion + "/configValues?searchKey=" + variableName, String)

        then:
        configValueBeforeChange.body.toString().contains('"' + variableName + '"' + ":" + '"' + oldValue + '"')
        changeConfigValueResponse.statusCode == HttpStatus.NO_CONTENT
        configValueAfterChange.body.toString().contains('"' + variableName + '"' + ":" + '"' + newValue + '"')

        where:
        moduleName   | moduleVersion | variableName                          | newValue | oldValue
        "CORE"       | "1.3.4"       | "train.feedbackLoop.FBLLogMultiplier" | "1"      | "3"
        "CORE"       | "1.3.5"       | "train.feedbackLoop.FBLLogMultiplier" | "1"      | "3"
        "CORE"       | "1.6.3"       | "train.feedbackLoop.FBLLogMultiplier" | "1"      | "3"
        "DMI"        | "1.4.0"       | "dmiServerPort"                       | "22222"  | "22225"
        "Graph"      | "1.6.3"       | "etcsgraph.headless"                  | "true"   | "false"
        "scs"        | "1.0.5"       | "debug"                               | "true"   | "false"
        "smartLogic" | "1.3.52"      | "config.handler.portOfGUIServer4TMS"  | "11111"  | "11114"
        "smarttms"   | "1.3.26"      | "tms.config.sendMotisFiles"           | "false"  | "true"
    }

    def "specify correct behaviour of setCommandLineArguments() and deleteCommandLineArguments() method - valid request"() {
        given:
        fillWithAvailable()
        def changeCommandLineArgumentRequest = "http://localhost:$port/moduleManager/availableModules/DMI/1.4.0/commandLineArguments"
        def requestBody = [firstArgument, secondArgument] as List
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(requestBody, headers)
        restTemplate.put(changeCommandLineArgumentRequest, requestEntity)

        when:
        def getAvailableResponse = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)
        restTemplate.delete("http://localhost:$port/moduleManager/availableModules/DMI/1.4.0/commandLineArguments")
        def getAvailableAfterDeletingResponse = restTemplate.getForEntity("http://localhost:$port/moduleManager/availableModules", String)

        then:
        getAvailableResponse.body.contains(firstArgument)
        getAvailableResponse.body.contains(secondArgument)
        !getAvailableAfterDeletingResponse.body.contains(secondArgument)

        where:
        firstArgument | secondArgument
        "headless"    | "trainID=1"
    }

    def cleanup() {
        moduleManagerController.getModuleManager().removeAllAvailableModules()
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}
