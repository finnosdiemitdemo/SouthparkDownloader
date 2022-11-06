package de.finnos.southparkdownloader.processes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FfmpegProcess implements CancelableProcess {
    private final Process process;
    private final ThreadProcess thread;

    private final List<Runnable> finishedListeners = new ArrayList<>();
    private final List<Consumer<Throwable>> interruptedListeners = new ArrayList<>();

    public FfmpegProcess(Process process, ThreadProcess thread) {
        this.process = process;
        this.thread = thread;

        process.onExit().whenCompleteAsync((process1, throwable) -> {
            if (throwable != null) {
                interruptedListeners.forEach(throwableConsumer -> throwableConsumer.accept(throwable));
            } else {
                finishedListeners.forEach(Runnable::run);
            }
        });

        ProcessesHolder.addProcess(this);
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public ThreadProcess getThread() {
        return thread;
    }

    @Override
    public void cancel() {
        if (process.isAlive()) {
            process.destroyForcibly();
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
}
