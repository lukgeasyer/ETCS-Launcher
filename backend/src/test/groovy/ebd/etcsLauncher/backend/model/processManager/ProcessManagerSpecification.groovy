package ebd.etcsLauncher.backend.model.processManager

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule
import ebd.etcsLauncher.backend.model.moduleManager.ModuleManager
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleSet
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.nio.file.Path

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getJarFolder

@SpringBootTest
class ProcessManagerSpecification extends Specification {
    @Autowired
    ModuleManager moduleManager
    @Autowired
    ProcessManager processManager

    def setup() {
        Path resourceDirectory = Path.of("src").resolve("test").resolve("resources").resolve(getJarFolder())
        Path tempTestDirectory = Path.of(getJarFolder())
        FileUtils.copyDirectory(resourceDirectory.toFile(), tempTestDirectory.toFile())

        moduleManager.fillWithAvailableModules()
    }

    def createModulesToRun(Map<ETCSModuleNames.ModuleName, String> moduleNameStringMap) {
        ETCSModuleSet modulesToRun = new ETCSModuleSet()
        for (Map.Entry<ETCSModuleNames.ModuleName, String> entry : moduleNameStringMap.entrySet()) {
            ETCSModuleNames.ModuleName moduleName = entry.getKey()
            String version = entry.getValue()
            modulesToRun.add(moduleManager.getAvailableModule(moduleName, version))
        }

        return modulesToRun
    }

    def getMapOfAllModules() {
        moduleManager.getAvailableModule(ETCSModuleNames.ModuleName.DMI, "1.4.0").setCommandLineArguments(["-headless"].toList())
        moduleManager.getAvailableModule(ETCSModuleNames.ModuleName.Graph, "1.6.3").setCommandLineArguments(["-headless"].toList())
        def modulesToRunMap = new HashMap<ETCSModuleNames.ModuleName, String>()
        modulesToRunMap.put(ETCSModuleNames.ModuleName.CORE, "1.6.3")
        modulesToRunMap.put(ETCSModuleNames.ModuleName.DMI, "1.4.0")
        modulesToRunMap.put(ETCSModuleNames.ModuleName.Graph, "1.6.3")
        modulesToRunMap.put(ETCSModuleNames.ModuleName.smartTMS, "1.3.26")
        modulesToRunMap.put(ETCSModuleNames.ModuleName.smartLogic, "1.3.52")
        modulesToRunMap.put(ETCSModuleNames.ModuleName.ScenarioControlServer, "1.0.5")

        return modulesToRunMap
    }

    def ensureModulesHaveTerminated() {
        def startingTime = System.currentTimeMillis()
        def modulesHaveTerminated = processManager.getRunningProcesses().size() == 0
        while (!modulesHaveTerminated) {
            modulesHaveTerminated = processManager.getRunningProcesses().size() == 0
            if (System.currentTimeMillis() - startingTime > 1000) {
                processManager.destroyAllModuleProcesses()
            }
        }
    }

    def specificModulesHaveStarted(ETCSModuleSet specificModules, long waitingTime) {
        def startingTime = System.currentTimeMillis()
        def startedModules = processManager.getRunningProcesses()
        def allModulesHaveStarted = startedModules.containsAll(specificModules) && specificModules.containsAll(startedModules)
        while (!allModulesHaveStarted) {
            startedModules = processManager.getRunningProcesses()
            allModulesHaveStarted = startedModules.containsAll(specificModules) && specificModules.containsAll(startedModules)
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
        def dmiHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.DMI)
        def graphHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.Graph)
        def scsHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)
        def smartLogicHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartLogic)
        def smartTMSHasStarted = runningModules.contains(ETCSModuleNames.ModuleName.smartTMS)

        while (!coreHasStarted || !dmiHasStarted || !graphHasStarted || !scsHasStarted || !smartLogicHasStarted || !smartTMSHasStarted) {
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
        def modulesHaveTerminated = processManager.getRunningProcesses().size() == 0
        while (!modulesHaveTerminated) {
            modulesHaveTerminated = processManager.getRunningProcesses().size() == 0
            if (System.currentTimeMillis() - startingTime > waitingTime) {
                return false
            }
        }
        return true
    }


    def "specify correct behavior of startProcesses() method - no delay between starting"() {
        given:
        def modulesToRun = createModulesToRun(getMapOfAllModules())
        processManager.setDelayBetweenModuleStarting(0L)

        when:
        processManager.runModules(modulesToRun)

        then:
        allModulesHaveStarted(5000)
    }

    def "specify correct behavior of startProcesses() method - 3 seconds delay"() {
        given:
        def modulesToRun = createModulesToRun(getMapOfAllModules())
        processManager.setDelayBetweenModuleStarting(3L)

        when:
        def startingTime = System.currentTimeMillis()
        processManager.runModules(modulesToRun)

        then:
        allModulesHaveStarted(25000)
        System.currentTimeMillis() - startingTime > 15000
    }

    def "specify correct behaviour of setDelayBetweenModuleStarting() method"() {
        given:
        def modulesToRun = createModulesToRun(getMapOfAllModules())
        def delay = 5L
        processManager.setDelayBetweenModuleStarting(delay)
        def startingTime = System.currentTimeMillis()

        when:
        processManager.runModules(modulesToRun)
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

            assert System.currentTimeMillis() - startingTime < ((delay * 6) + 5) * 1000
        }

        then:
        processManager.getDelayBetweenModuleStarting() == delay

        scsTime - dmiTime > (delay * 1000) - 200
        graphTime - scsTime > (delay * 1000) - 200
        coreTime - graphTime > (delay * 1000) - 200
        smartTMSTime - coreTime > (delay * 1000) - 200
        smartLogicTime - smartTMSTime > (delay * 1000) - 200
    }

    def "specify correct order of startedModules"() {
        given:
        def modulesToRun = createModulesToRun(getMapOfAllModules())
        def delay = 3L
        processManager.setDelayBetweenModuleStarting(delay)
        def startingTime = System.currentTimeMillis()

        when:
        processManager.runModules(modulesToRun)
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

            assert System.currentTimeMillis() - startingTime < ((delay * 5) + 5) * 1000
        }

        then:
        scsTime - dmiTime > delay - 500L
        graphTime - scsTime > delay - 500L
        coreTime - graphTime > delay - 500L
        smartTMSTime - coreTime > delay - 500L
        smartLogicTime - smartTMSTime > delay - 500L
    }

    def "specify correct behavior of destroyAllModuleProcesses() method"() {
        given:
        def modulesToRun = createModulesToRun(getMapOfAllModules())
        processManager.setDelayBetweenModuleStarting(0L)

        when:
        processManager.runModules(modulesToRun)
        allModulesHaveStarted(5000)
        processManager.destroyAllModuleProcesses()

        then:
        allModulesHaveTerminated(5000)
    }

    def "specify correct behaviour when trying to run module which is not available"() {
        given:
        ETCSModuleSet modulesToRun = new ETCSModuleSet()
        modulesToRun.add(new ETCSModule("java", ETCSModuleNames.ModuleName.ScenarioControlServer, new ETCSModuleVersion("1.0.5"), "scenarioControlServer-1.0.5-SNAPSHOT_invalid.jar"))

        when:
        def ranNotAvailableModule = processManager.runModules(modulesToRun)
        sleep(3000)
        def runningModules = processManager.getRunningProcesses().size()

        then:
        !ranNotAvailableModule
        runningModules == 0
    }

    def "specify correct behaviour of multiple startProcesses() calls"() {
        given:
        processManager.setDelayBetweenModuleStarting(0L)
        def modulesToRun = createModulesToRun(getMapOfAllModules())

        when:
        def initialStart = processManager.runModules(modulesToRun)
        def secondStart = processManager.runModules(modulesToRun)

        then:
        allModulesHaveStarted(3000)
        initialStart
        !secondStart
    }

    def "specify correct behaviour of forwardInputToRunningProcess() method"() {
        given:
        def modulesToRun = new ETCSModuleSet()
        modulesToRun.add(moduleManager.getAvailableModule(ETCSModuleNames.ModuleName.CORE, "1.6.3"))
        processManager.setDelayBetweenModuleStarting(0L)
        processManager.runModules(modulesToRun)

        when:
        specificModulesHaveStarted(modulesToRun, 5000)
        Thread.sleep(5000)
        processManager.forwardInputToRunningProcess(ETCSModuleNames.ModuleName.CORE, "end")

        then:
        allModulesHaveTerminated(20000)
    }

    def cleanup() {
        processManager.destroyAllModuleProcesses()
        ensureModulesHaveTerminated()
        Path.of(getJarFolder()).toFile().deleteDir()
    }
}
