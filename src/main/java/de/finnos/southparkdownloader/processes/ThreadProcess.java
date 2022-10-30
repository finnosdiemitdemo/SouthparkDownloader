package de.finnos.southparkdownloader.processes;

import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressEvent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ThreadProcess extends Thread implements CancelableProcess {
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
        } catch (InterruptThreadProcessException ignored) {

        }
    }

    @Override
    public void interrupt() {
        // Request interruption
        super.interrupt();
        interruptedListeners.forEach(throwableConsumer -> throwableConsumer.accept(null));
    }

    @Override
    public ThreadProcess getThread() {
        return this;
    }

    @Override
    public void cancel() {
        interrupt();
    }

    @Override
    public void addFinishedListener(final Runnable runnable) {
        finishedListeners.add(runnable);
    }

    @Override
    public void addInterruptedListener(final Consumer<Throwable> runnable) {
        interruptedListeners.add(runnable);
    }

    public abstract void execute();

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

    private static class InterruptThreadProcessException extends RuntimeException {
    }
}
