package ebd.etcsLauncher.backend.model.processManager;

import ebd.etcsLauncher.backend.api.webSocket.WebSocketService;
import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Manages the {@link Process} of a running {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}.
 *
 * @author Lukas Geyer
 */
public class ETCSModuleProcessWorker implements Runnable {

    private final Logger            logger;
    private final CountDownLatch    initializedModuleSignal;
    private final CountDownLatch    startSignal;
    private final CountDownLatch    doneSignal;
    private final CountDownLatch    moduleTerminatedSignal;
    private final ProcessBuilder    moduleProcessBuilder;
    private final ETCSModule        correspondingModule;
    private final WebSocketService  webSocketService;
    private final String            webSocketEndpoint;
    private       Process           moduleProcess;
    private       ModuleInputThread moduleInputThread;
    private       Thread            moduleOutputThread;
    private       Thread            moduleErrorOutputThread;

    public ETCSModuleProcessWorker(WebSocketService webSocketService,
                                   CountDownLatch startSignal,
                                   CountDownLatch doneSignal,
                                   ProcessBuilder moduleProcessBuilder,
                                   ETCSModule correspondingModule) {
        this.webSocketService = webSocketService;
        this.webSocketEndpoint = "/processManager/running/" + correspondingModule.getModuleName().toString();
        this.initializedModuleSignal = new CountDownLatch(1);
        this.moduleTerminatedSignal = new CountDownLatch(1);
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
        this.moduleProcessBuilder = moduleProcessBuilder;
        this.correspondingModule = correspondingModule;
        logger = LoggerFactory.getLogger(correspondingModule.getModuleName().name());
    }

    public ETCSModule getCorrespondingModule() {
        return correspondingModule;
    }

    /**
     * Checks if the {@link Process} exists and is alive.
     *
     * @return true if {@link #moduleProcess} exists and is alive, false otherwise
     */
    public boolean isRunning() {
        return moduleProcess != null && moduleProcess.isAlive();
    }

    /**
     * Waits for the {@link #startSignal} to go, then starts the {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule} {@link Process}.
     * Creates {@link Thread}s that forward output of the {@link Process} to a websocket endpoint as well as
     * forwarding input to the {@link Process}.
     * Waits for termination of the {@link Process} and all created {@link Thread}s and then counts down the {@link #doneSignal}.
     *
     * @throws RuntimeException
     *         if an {@link Exception} has occurred while running
     */
    public void run() {
        try {
            startSignal.await();
            correspondingModule.getModificationLock().readLock().lock();

            moduleProcessBuilder.redirectErrorStream(true);
            moduleProcess = moduleProcessBuilder.start();

            StringBuilder command = new StringBuilder();
            moduleProcessBuilder.command().forEach(cmd -> command.append(" ").append(cmd));

            logger.info("Process has been started. Command: {}", command);

            webSocketService.sendMessageToAddress(webSocketEndpoint, "STARTED");

            initializedModuleSignal.countDown();

            moduleOutputThread = new Thread(() -> readStream(moduleProcess.getInputStream()));
            moduleErrorOutputThread = new Thread(() -> readStream(moduleProcess.getErrorStream()));
            moduleInputThread = new ModuleInputThread(moduleProcess, logger);

            moduleOutputThread.start();
            moduleErrorOutputThread.start();
            moduleInputThread.start();

            moduleProcess.waitFor();

            moduleTerminatedSignal.countDown();

            moduleOutputThread.interrupt();
            moduleErrorOutputThread.interrupt();
            moduleInputThread.stopThread();

            moduleOutputThread.join();
            moduleErrorOutputThread.join();
            moduleInputThread.join();

            correspondingModule.getModificationLock().readLock().unlock();

            logger.info("Process and all corresponding Threads have terminated gracefully.");

            webSocketService.sendMessageToAddress(webSocketEndpoint, "STOPPED");

            doneSignal.countDown();
        } catch(Exception e) {
            logger.error("Exception has occurred while trying to start/run Process: {}", e.getMessage());
            logger.error("Trying to interrupt corresponding Threads and destroy process before throwing exception...");
            if(moduleOutputThread != null && moduleOutputThread.isAlive()) {
                moduleOutputThread.interrupt();
            }

            if(moduleErrorOutputThread != null && moduleErrorOutputThread.isAlive()) {
                moduleErrorOutputThread.interrupt();
            }

            if(moduleInputThread != null && moduleInputThread.isAlive()) {
                moduleInputThread.interrupt();
            }

            destroyProcess();
            correspondingModule.getModificationLock().readLock().unlock();

            webSocketService.sendMessageToAddress(webSocketEndpoint, "STOPPED");
            doneSignal.countDown();
            throw new RuntimeException();
        }
    }

    /**
     * Creates an output thread for the {@link Process} of this {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}.
     * Sends output to a websocket endpoint which clients can subscribe to.
     *
     * @param inputStream
     *         the stream from the process of this {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     *         to be printed
     */
    private void readStream(InputStream inputStream) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            long   sequenceNumber = 0;
            while((line = reader.readLine()) != null && webSocketService != null) {
                webSocketService.sendMessageToAddress(webSocketEndpoint, sequenceNumber + ":" + line);
                sequenceNumber++;
            }
        } catch(IOException e) {
            try {
                if(!moduleTerminatedSignal.await(1, TimeUnit.SECONDS)) {
                    logger.error("Read stream has thrown exception:{}", e.getMessage());
                    logger.error("Trying to destroy process...");
                    destroyProcess();
                }
            } catch(InterruptedException interruptedException) {
                logger.error("Read stream was interrupted while waiting for a signal to go down:{}", interruptedException.getMessage());
                logger.error("Trying to destroy process...");
                destroyProcess();
            }

        }
    }

    /**
     * Receives input and sends it to the {@link Process} of this {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}.
     *
     * @param input
     *         the input to be sent to the {@link Process} of this {@link ebd.etcsLauncher.backend.model.etcsModule.ETCSModule}
     */
    public boolean receiveInput(String input) {
        return moduleInputThread.sendInput(input);
    }

    /**
     * Destroys the underlying {@link Process}. Waits 1 second, if after 1 second the {@link Process} is still alive,
     * tries to gracefully destroy it. Waits 3 seconds, if after 3 seconds the {@link Process} is still alive,
     * forcibly destroys it.
     *
     * @throws RuntimeException
     *         if waiting for the {@link Process} has been interrupted
     */
    public void destroyProcess() {
        if(isRunning()) {
            moduleProcess.destroy();
            try {
                if(!moduleProcess.waitFor(3, TimeUnit.SECONDS)) {
                    logger.info("Could not destroy process gracefully. Destroying it forcibly...");
                    moduleProcess = moduleProcess.destroyForcibly();
                }
            } catch(InterruptedException e) {
                logger.error("Interrupted while trying to destroy process! Trying to destroy the process forcibly before throwing Exception.");
                moduleProcess = moduleProcess.destroyForcibly();
                throw new RuntimeException();
            }
        }
        else {
            logger.warn("Trying to destroy process even though its not alive. Ignoring request...");
        }
    }

}
