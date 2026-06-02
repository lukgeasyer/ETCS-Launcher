package ebd.etcsLauncher.backend.api.controller

import ebd.etcsLauncher.backend.model.processManager.ProcessManager
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import spock.lang.Specification

import java.nio.file.Path

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationControllerSpecification extends Specification {
    @LocalServerPort
    private int port
    private TestRestTemplate restTemplate = new TestRestTemplate()
    @Autowired
    ProcessManager processManager
    @Autowired
    ProcessManagerController processManagerController

    def fillWithAvailable() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())
        restTemplate.postForEntity("http://localhost:$port/moduleManager/availableModules/scanFileSystem", null, String)
    }

    def specificModulesHaveStarted(List<String> moduleNames, long waitingTime) {
        def startingTime = System.currentTimeMillis()
        def startedModules = restTemplate.getForEntity("http://localhost:$port/processManager/running", String)
        def allModulesHaveStarted = moduleNames.findAll { startedModules.body.contains(it) }.size() == moduleNames.size()
        while (!allModulesHaveStarted) {
            startedModules = restTemplate.getForEntity("http://localhost:$port/processManager/running", String)
            allModulesHaveStarted = moduleNames.findAll { startedModules.body.contains(it) }.size() == moduleNames.size()
            if (System.currentTimeMillis() - startingTime > waitingTime) {
                return false
            }
        }
        return true
    }

    def ensureModulesHaveTerminated() {
        def startingTime = System.currentTimeMillis()
        def modulesHaveTerminated = allModulesHaveTerminated(1000)
        while (!modulesHaveTerminated) {
            modulesHaveTerminated = allModulesHaveTerminated(1000)
            if (System.currentTimeMillis() - startingTime > 1000) {
                restTemplate.postForEntity("http://localhost:$port/processManager/stop", null, String)
            }
        }
    }

    def allModulesHaveTerminated(long waitingTime) {
        def startingTime = System.currentTimeMillis()
        def modulesHaveTerminated = restTemplate.getForEntity("http://localhost:$port/processManager/running", String).body == "[]"
        while (!modulesHaveTerminated) {
            modulesHaveTerminated = restTemplate.getForEntity("http://localhost:$port/processManager/running", String).body == "[]"
            if (System.currentTimeMillis() - startingTime > waitingTime) {
                return false
            }
        }
        return true
    }

    def "specify correct behaviour of shutdownBackend() method - invalid request, modules running"() {
        given:
        fillWithAvailable()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestBody = [:]
        requestBody[ETCSModuleNames.ModuleName.CORE.name()] = "1.3.4"
        def requestEntity = new HttpEntity<>(requestBody, headers)

        when:
        restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        specificModulesHaveStarted(["CORE"].toList(), 5000)
        Thread.sleep(5000)
        def shutDownBackendResponse = restTemplate.postForEntity("http://localhost:$port/shutdown", null, String)

        then:
        shutDownBackendResponse.statusCode == HttpStatus.BAD_REQUEST

        cleanup:
        restTemplate.postForEntity("http://localhost:$port/processManager/stop", null, String)
        ensureModulesHaveTerminated()
    }

    def cleanup() {
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}
