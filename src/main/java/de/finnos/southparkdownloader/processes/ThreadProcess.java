package de.finnos.southparkdownloader.processes;

import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ThreadProcess extends Thread implements CancelableProcess {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadProcess.class);

    private ProgressEvent progressEvent;
    private final List<Runnable> finishedListeners = new ArrayList<>();
    private final List<Consumer<Throwable>> interruptedListeners = new ArrayList<>();

    public ThreadProcess() {
        ProcessesHolder.addProcess(this);
    }

    public ThreadProcess(final JFrame jFrame) {
        ProcessesHolder.addProcess(this, jFrame);
    }

    @Override
    final public void run() {
        super.run();

        // Wenn der Thread terminiert wird, dürfen die finishedListener nicht aufgerufen werden
        try {
            execute();
            finishedListeners.forEach(Runnable::run);
        } catch (InterruptThreadProcessException e) {
            interruptedListeners.forEach(throwableConsumer -> throwableConsumer.accept(e));
        } catch (Exception e) {
            LOG.error("UncaughtException", e);
            getProgressEvent().updateProgress("Exception occurred: " + e.getMessage());

            interruptedListeners.forEach(throwableConsumer -> throwableConsumer.accept(e));
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public ThreadProcess getThread() {
        return this;
    }

    @Override
    public void cancel() {
        if (isAlive()) {
            interrupt();
        }
    }

    @Override
    public void addFinishedListener(final Runnable runnable) {
        finishedListeners.add(runnable);
    }

    @Override
    public void addInterruptedListener(final Consumer<Throwable> runnable) {
        interruptedListeners.add(runnable);
    }

    public abstract void execute() throws Exception;

    public void setProgressEvent(ProgressEvent progressEvent) {
        this.progressEvent = progressEvent;
    }

    public ProgressEvent getProgressEvent() {
        return progressEvent;
    }

    public void cancelProcessIfInterruptionIsInProgress() {
        if (isInterrupted()) {
            throw new InterruptThreadProcessException();
        }
    }

    public static class InterruptThreadProcessException extends RuntimeException {
    }
}
