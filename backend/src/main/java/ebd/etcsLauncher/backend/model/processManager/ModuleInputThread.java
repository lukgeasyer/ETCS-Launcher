package ebd.etcsLauncher.backend.model.processManager;

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A running {@link Thread} that waits for input as long as a {@link Process} is alive.
 * The {@link Process} represents a running {@link ETCSModule} {@link Process}.
 *
 * @author Lukas Geyer
 */
class ModuleInputThread extends Thread {

    private final Logger                logger;
    private final Process               moduleProcess;
    private final BlockingQueue<String> inputQueue    = new LinkedBlockingQueue<>();
    private       boolean               stopRequested = false;

    public ModuleInputThread(Process moduleProcess, Logger underlyingProcessLogger) {
        this.moduleProcess = moduleProcess;
        this.logger = underlyingProcessLogger;
    }

    public boolean sendInput(String input) {
        return inputQueue.offer(input);
    }

    public void stopThread() {
        stopRequested = true;
        this.interrupt();
    }

    /**
     * Waits for items pushed to the {@link #inputQueue} and then sends the input to the underlying {@link Process}.
     */
    @Override
    public void run() {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(moduleProcess.getOutputStream()))) {
            while(!Thread.interrupted() && !stopRequested) {
                try {
                    String input = inputQueue.take();
                    writer.write(input);
                    writer.newLine();
                    writer.flush();
                    logger.info("Sent input: {}", input);
                } catch(InterruptedException e) {
                    if(!stopRequested) {
                        logger.warn("Write stream has been interrupted while waiting for input even though process is still running.");
                        Thread.currentThread().interrupt();
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch(IOException e) {
            logger.warn("Write stream has thrown exception. Interrupting it...", e);
            this.interrupt();
        }
    }

}

