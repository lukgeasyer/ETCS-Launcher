package ebd.etcsLauncher.backend.api.controller

import ebd.etcsLauncher.backend.model.processManager.ProcessManager
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import spock.lang.Specification

import java.nio.file.Path

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProcessManagerControllerSpecification extends Specification {
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

    def getModulesToRun() {
        def modulesToRun = [:]
        modulesToRun[ETCSModuleNames.ModuleName.CORE.name()] = "1.6.3"
        modulesToRun[ETCSModuleNames.ModuleName.DMI.name()] = "1.4.0"
        modulesToRun[ETCSModuleNames.ModuleName.Graph.name()] = "1.6.3"
        modulesToRun[ETCSModuleNames.ModuleName.smartLogic.name()] = "1.3.52"
        modulesToRun[ETCSModuleNames.ModuleName.smartTMS.name()] = "1.3.26"
        modulesToRun[ETCSModuleNames.ModuleName.ScenarioControlServer.name()] = "1.0.5"

        return modulesToRun
    }

    def prepareAllModules() {
        def changeCommandLineArgumentRequestDMI = "http://localhost:$port/moduleManager/availableModules/DMI/1.4.0/commandLineArguments"
        def changeCommandLineArgumentRequestGraph = "http://localhost:$port/moduleManager/availableModules/Graph/1.6.3/commandLineArguments"
        def requestBody = ["-headless"] as List
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(requestBody, headers)
        restTemplate.put(changeCommandLineArgumentRequestDMI, requestEntity)
        restTemplate.put(changeCommandLineArgumentRequestGraph, requestEntity)
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

    def allModulesHaveStarted(long waitingTime) {
        def startingTime = System.currentTimeMillis()
        def runningModules = processManager.getRunningProcesses().moduleName
        def coreHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.CORE)
        def graphHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.Graph)
        def dmiHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.DMI)
        def scsHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)
        def smartLogicHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)
        def smartTMSHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)

        while (!coreHasStarted || !graphHasStarted || !dmiHasStarted || !scsHasStarted || !smartLogicHasStarted || !smartTMSHasStarted) {
            runningModules = processManager.getRunningProcesses().moduleName
            if (!coreHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.CORE)) {
                coreHasStarted = true
            }
            if (!dmiHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.DMI)) {
                dmiHasStarted = true
            }
            if (!graphHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.Graph)) {
                graphHasStarted = true
            }
            if (!scsHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)) {
                scsHasStarted = true
            }
            if (!smartLogicHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)) {
                smartLogicHasStarted = true
            }
            if (!smartTMSHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)) {
                smartTMSHasStarted = true
            }

            assert System.currentTimeMillis() - startingTime < waitingTime
        }

        return true
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

    def "specify correct behaviour of runModules() method - no delay"() {
        given:
        fillWithAvailable()
        processManager.setDelayBetweenModuleStarting(0L)
        prepareAllModules()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(getModulesToRun(), headers)

        when:
        def response = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)

        then:
        response.statusCode == HttpStatus.NO_CONTENT
        allModulesHaveStarted(5000)
    }

    def "specify correct behaviour of runModules() method - invalid modules"() {
        given:
        fillWithAvailable()
        processManager.setDelayBetweenModuleStarting(0L)
        prepareAllModules()
        def requestBody = [:]
        requestBody[ETCSModuleNames.ModuleName.CORE] = "1.6.4"
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(requestBody, headers)

        when:
        def response = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
    }

    def "specify correct behaviour of runModules() method - 3 seconds delay"() {
        given:
        fillWithAvailable()
        processManager.setDelayBetweenModuleStarting(3L)
        prepareAllModules()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(getModulesToRun(), headers)

        when:
        def startingTime = System.currentTimeMillis()
        def response = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)

        then:
        response.statusCode == HttpStatus.NO_CONTENT
        allModulesHaveStarted(25000)
        System.currentTimeMillis() - startingTime > 15000
    }

    def "specify correct behaviour of setDelayBetweenStarting() method - 5 seconds delay"() {
        given:
        fillWithAvailable()
        prepareAllModules()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(getModulesToRun(), headers)

        when:
        def setDelayResponse = restTemplate.exchange("http://localhost:$port/processManager/delay?delayBetweenStarting=5",
                HttpMethod.PUT, null, String)
        def startingTime = System.currentTimeMillis()
        def runResponse = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        def runningModules = processManager.getRunningProcesses().moduleName
        def coreHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.CORE)
        def dmiHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.DMI)
        def graphHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.Graph)
        def scsHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)
        def smartLogicHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)
        def smartTMSHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)

        def coreTime = 0L
        def dmiTime = 0L
        def graphTime = 0L
        def scsTime = 0l
        def smartLogicTime = 0L
        def smartTMSTime = 0L

        while (!coreHasStarted || !dmiHasStarted || !graphHasStarted || !scsHasStarted || !smartLogicHasStarted || !smartTMSHasStarted) {
            runningModules = processManager.getRunningProcesses().moduleName
            if (!coreHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.CORE)) {
                coreHasStarted = true
                coreTime = System.currentTimeMillis() - startingTime
            }
            if (!dmiHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.DMI)) {
                dmiHasStarted = true
                dmiTime = System.currentTimeMillis() - startingTime
            }
            if (!graphHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.Graph)) {
                graphHasStarted = true
                graphTime = System.currentTimeMillis() - startingTime
            }
            if (!scsHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)) {
                scsHasStarted = true
                scsTime = System.currentTimeMillis() - startingTime
            }
            if (!smartLogicHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)) {
                smartLogicHasStarted = true
                smartLogicTime = System.currentTimeMillis() - startingTime
            }
            if (!smartTMSHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)) {
                smartTMSTime = System.currentTimeMillis() - startingTime
                smartTMSHasStarted = true
            }

            assert System.currentTimeMillis() - startingTime < 35000
        }
        def elapsedTime = System.currentTimeMillis() - startingTime

        then:
        setDelayResponse.statusCode == HttpStatus.NO_CONTENT
        runResponse.statusCode == HttpStatus.NO_CONTENT
        elapsedTime > 25000L
        scsTime - dmiTime > 4500L
        graphTime - scsTime > 4500L
        coreTime - graphTime > 4500L
        smartTMSTime - coreTime > 4500L
        smartLogicTime - smartTMSTime > 4500L
    }

    def "specify correct order when starting - 3 seconds delay"() {
        given:
        fillWithAvailable()
        processManager.setDelayBetweenModuleStarting(3L)
        prepareAllModules()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(getModulesToRun(), headers)

        when:
        def startingTime = System.currentTimeMillis()
        def runResponse = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        def runningModules = processManager.getRunningProcesses().moduleName
        def coreHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.CORE)
        def dmiHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.DMI)
        def graphHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.Graph)
        def scsHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)
        def smartLogicHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)
        def smartTMSHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)

        def coreTime = 0L
        def dmiTime = 0L
        def graphTime = 0L
        def scsTime = 0l
        def smartLogicTime = 0L
        def smartTMSTime = 0L

        while (!coreHasStarted || !dmiHasStarted || !graphHasStarted || !scsHasStarted || !smartLogicHasStarted || !smartTMSHasStarted) {
            runningModules = processManager.getRunningProcesses().moduleName
            if (!coreHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.CORE)) {
                coreHasStarted = true
                coreTime = System.currentTimeMillis() - startingTime
            }
            if (!dmiHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.DMI)) {
                dmiHasStarted = true
                dmiTime = System.currentTimeMillis() - startingTime
            }
            if (!graphHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.Graph)) {
                graphHasStarted = true
                graphTime = System.currentTimeMillis() - startingTime
            }
            if (!scsHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)) {
                scsHasStarted = true
                scsTime = System.currentTimeMillis() - startingTime
            }
            if (!smartLogicHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)) {
                smartLogicHasStarted = true
                smartLogicTime = System.currentTimeMillis() - startingTime
            }
            if (!smartTMSHasStarted && runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)) {
                smartTMSTime = System.currentTimeMillis() - startingTime
                smartTMSHasStarted = true
            }

            assert System.currentTimeMillis() - startingTime < 25000
        }
        def elapsedTime = System.currentTimeMillis() - startingTime

        then:
        runResponse.statusCode == HttpStatus.NO_CONTENT
        elapsedTime > 15000L
        scsTime - dmiTime > 2500L
        graphTime - scsTime > 2500L
        coreTime - graphTime > 2500L
        smartTMSTime - coreTime > 2500L
        smartLogicTime - smartTMSTime > 2500L

        cleanup:
        processManager.destroyAllModuleProcesses()
        ensureModulesHaveTerminated()
    }

    def "specify correct behaviour of sendInput() method - valid request"() {
        given:
        fillWithAvailable()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestBody = [:]
        requestBody[ETCSModuleNames.ModuleName.CORE.name()] = "1.6.3"
        def requestEntity = new HttpEntity<>(requestBody, headers)

        when:
        def runResponse = restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        specificModulesHaveStarted(["CORE"].toList(), 5000)
        Thread.sleep(5000)
        def sendInputToCOREResponse = restTemplate.postForEntity("http://localhost:$port/processManager/running/CORE?input=end", null, String)

        then:
        runResponse.statusCode == HttpStatus.NO_CONTENT
        sendInputToCOREResponse.statusCode == HttpStatus.NO_CONTENT
        allModulesHaveTerminated(20000)
    }

    def "specify correct behaviour of sendInput() method - invalid request, no running modules"() {
        when:
        def sendInputToCOREResponse = restTemplate.postForEntity("http://localhost:$port/processManager/running/CORE?input=end", null, String)

        then:
        sendInputToCOREResponse.statusCode == HttpStatus.BAD_REQUEST
    }

    def "specify correct behaviour of sendInput() method - invalid request, running module has terminated"() {
        given:
        fillWithAvailable()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestBody = [:]
        requestBody[ETCSModuleNames.ModuleName.CORE.name()] = "1.6.3"
        def requestEntity = new HttpEntity<>(requestBody, headers)

        when:
        restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        specificModulesHaveStarted(["CORE"].toList(), 5000)
        restTemplate.postForEntity("http://localhost:$port/processManager/stop", null, String)
        allModulesHaveTerminated(5000)
        def sendInputToCOREResponse = restTemplate.postForEntity("http://localhost:$port/processManager/running/CORE?input=end", null, String)

        then:
        sendInputToCOREResponse.statusCode == HttpStatus.BAD_REQUEST
    }

    def "specify correct behaviour of stopRunningProcesses() method"() {
        given:
        fillWithAvailable()
        processManager.setDelayBetweenModuleStarting(3L)
        prepareAllModules()
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def requestEntity = new HttpEntity<>(getModulesToRun(), headers)

        when:
        restTemplate.exchange("http://localhost:$port/processManager/run", HttpMethod.POST, requestEntity, String)
        allModulesHaveStarted(25000)
        Thread.sleep(1000)
        restTemplate.postForEntity("http://localhost:$port/processManager/stop", null, String)

        then:
        allModulesHaveTerminated(5000)
    }

    def cleanup() {
        restTemplate.postForEntity("http://localhost:$port/processManager/stop", null, String)
        ensureModulesHaveTerminated()
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}
