package ebd.etcsLauncher.backend.model.processManager;

import ebd.etcsLauncher.backend.api.webSocket.WebSocketService;
import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import ebd.etcsLauncher.backend.model.moduleManager.ModuleManager;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleLogics;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleSet;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.SortedETCSModuleArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static ebd.etcsLauncher.backend.utils.fileSystemUtils.FileSystemLogic.getDirectoryLogic;

/**
 * Manages the running {@link Process}es of the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}s.
 *
 * @author Lukas Geyer
 */
@Component
public class ProcessManager {

    private final String                                         jdkPath;
    private final ModuleManager                                  moduleManager;
    private final Logger                                         logger;
    private final WebSocketService                               webSocketService;
    private final ConcurrentLinkedQueue<ETCSModuleProcessWorker> moduleProcessWorkers;
    private       long                                           delayBetweenModuleStarting;
    private       ScheduledExecutorService                       moduleExecutor;
    private       CountDownLatch                                 startSignal;
    private       CountDownLatch                                 doneSignal;

    public ProcessManager(ModuleManager moduleManager, WebSocketService webSocketService,
                          @Value("${jdkPath}") String jdkPath) {
        this.jdkPath = jdkPath;
        this.moduleManager = moduleManager;
        this.logger = LoggerFactory.getLogger("ProcessManager");
        this.webSocketService = webSocketService;
        this.moduleProcessWorkers = new ConcurrentLinkedQueue<>();
        this.delayBetweenModuleStarting = 3L;
        this.startSignal = new CountDownLatch(1);
    }

    /**
     * Returns the delay between the start of each {@link Process}.
     *
     * @return the delay between the start of each {@link Process} in seconds
     */
    public long getDelayBetweenModuleStarting() {
        return delayBetweenModuleStarting;
    }

    /**
     * Sets the delay between the start of each {@link Process}, if a delay is needed.
     *
     * @param delayBetweenModuleStarting
     *         the delay between the start of each {@link Process} in seconds
     */
    public void setDelayBetweenModuleStarting(long delayBetweenModuleStarting) {
        this.delayBetweenModuleStarting = delayBetweenModuleStarting;
    }

    /**
     * Counts down the {@link CountDownLatch} responsible for triggering the start of all {@link ETCSModule} {@link Process}es.
     * If there needs to be a delay (when the order of execution is relevant) then a delay is introduced between starting the
     * different {@link ETCSModuleProcessWorker}s.
     * Asynchronously calls the {@link #reset()} method such that the API call that is mapped to this method does not
     * block until all {@link Process}es have terminated.
     *
     * @param modulesToRun
     *         an {@link ETCSModuleSet} of {@link ETCSModule}s to run
     *
     * @return true if {@link ETCSModule}s have successfully been started, false otherwise
     */
    public boolean runModules(ETCSModuleSet modulesToRun) {
        if(modulesToRun.isEmpty()) {
            return false;
        }
        if(!moduleManager.getAvailableModules().containsAll(modulesToRun)) {
            logger.warn("Not all modules are available! Ignoring request...");
            return false;
        }
        if((startSignal != null && startSignal.getCount() == 0) || (doneSignal != null && doneSignal.getCount() > 0)) {
            logger.warn("Tried to start processes before running processes have finished. Ignoring request...");
            return false;
        }
        try {
            prepareModules(modulesToRun);
            moduleExecutor = new ScheduledThreadPoolExecutor(modulesToRun.size());
            long delayBetweenProcesses = modulesToRun.size() > 1 && (modulesToRun.contains(ETCSModuleNames.ModuleName.smartTMS)
                                                                     || modulesToRun.contains(ETCSModuleNames.ModuleName.ScenarioControlServer)
                                                                     || modulesToRun.contains(ETCSModuleNames.ModuleName.smartLogic))
                                         ? delayBetweenModuleStarting
                                         : 0L;
            ListIterator<ETCSModuleProcessWorker> etcsModuleProcessWorkerListIterator = moduleProcessWorkers.stream().toList().listIterator();
            while(etcsModuleProcessWorkerListIterator.hasNext()) {
                moduleExecutor.schedule(etcsModuleProcessWorkerListIterator.next(),
                                        delayBetweenProcesses * etcsModuleProcessWorkerListIterator.nextIndex(),
                                        TimeUnit.SECONDS);
            }

            logger.info("Starting module/s: {}. Delay: {}s.", ETCSModuleLogics.getRunOrder(modulesToRun), delayBetweenModuleStarting);
            startSignal.countDown();
        } catch(Exception e) {
            logger.error("Exception has occurred while running module/s: ", e);
            return false;
        } finally {
            CompletableFuture.runAsync(this::reset);
        }

        return true;
    }

    /**
     * Initializes a CountDownLatch with the number of {@link ETCSModule}s that should be run.
     * Creates corresponding {@link ETCSModuleProcessWorker}s that control the running {@link Process} of a {@link ETCSModule}.
     */
    private void prepareModules(ETCSModuleSet modulesToRun) {
        doneSignal = new CountDownLatch(modulesToRun.size());

        SortedETCSModuleArrayList modulesToRunInOrder = ETCSModuleLogics.getRunOrder(modulesToRun);

        modulesToRunInOrder.forEach(etcsModule -> {
            ArrayList<String> commandLineArguments = new ArrayList<>();
            commandLineArguments.add(jdkPath);
            commandLineArguments.add("-jar");
            commandLineArguments.add(etcsModule.getJarName());
            commandLineArguments.addAll(etcsModule.getCommandLineArguments());
            ProcessBuilder moduleProcess = new ProcessBuilder(commandLineArguments);
            Path           jarPath       = getDirectoryLogic(etcsModule.getModuleName()).resolve(Path.of(etcsModule.getVersion().get()));
            moduleProcess.directory(jarPath.toFile());
            ETCSModuleProcessWorker etcsModuleProcessWorker = new ETCSModuleProcessWorker(webSocketService,
                                                                                          startSignal,
                                                                                          doneSignal,
                                                                                          moduleProcess,
                                                                                          etcsModule);
            moduleProcessWorkers.add(etcsModuleProcessWorker);
        });
    }

    /**
     * Forwards input to a running {@link Process}.
     *
     * @param moduleName
     *         the name of the {@link ETCSModule} the input is for
     * @param input
     *         the input to forward
     */
    public boolean forwardInputToRunningProcess(ETCSModuleNames.ModuleName moduleName, String input) {
        try {
            ETCSModuleProcessWorker processWorkerToReceiveInput = moduleProcessWorkers.stream().filter(ETCSModuleProcessWorker::isRunning)
                                                                                      .toList()
                                                                                      .stream()
                                                                                      .filter(etcsModuleProcessWorker -> etcsModuleProcessWorker
                                                                                              .getCorrespondingModule().getModuleName()
                                                                                              .equals(moduleName))
                                                                                      .findFirst()
                                                                                      .orElseThrow();
            return processWorkerToReceiveInput.receiveInput(input);
        } catch(NoSuchElementException noSuchElementException) {
            logger.warn("Tried to forward input to module that is not running. Ignoring request...");
            return false;
        }
    }

    public List<ETCSModule> getRunningProcesses() {
        if(moduleProcessWorkers.isEmpty()) {
            return Collections.emptyList();
        }
        return moduleProcessWorkers.stream()
                                   .filter(ETCSModuleProcessWorker::isRunning)
                                   .map(ETCSModuleProcessWorker::getCorrespondingModule)
                                   .toList();
    }

    public boolean destroyAllModuleProcesses() {
        logger.info("Destroying running modules...");
        if(moduleProcessWorkers.isEmpty()) {
            logger.info("There were not modules running. Ignoring request...");
            return true;
        }
        else {
            try {
                moduleProcessWorkers.forEach(ETCSModuleProcessWorker::destroyProcess);
                return true;
            } catch(RuntimeException e) {
                logger.error("An error has occurred while trying to shut down running modules: {}", e.getMessage());
                return false;
            }
        }
    }


    /**
     * Resets the preparation for running {@link Process}es. That is, resetting the {@link #startSignal} and clearing {@link #moduleProcessWorkers}.
     */
    private void reset() {
        try {
            doneSignal.await();
        } catch(InterruptedException interruptedException) {
            logger.error("reset() method has been interrupted while waiting for doneSignal.", interruptedException);
            throw new RuntimeException();
        }
        if(moduleExecutor != null) {
            moduleExecutor.shutdown();
        }
        startSignal = new CountDownLatch(1);
        moduleProcessWorkers.clear();
    }

}
