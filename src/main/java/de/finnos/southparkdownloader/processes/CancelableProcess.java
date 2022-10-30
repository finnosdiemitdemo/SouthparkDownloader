package de.finnos.southparkdownloader.processes;

import java.util.function.Consumer;

public interface CancelableProcess {
    ThreadProcess getThread();

    void cancel();

    void addFinishedListener(final Runnable runnable);

    void addInterruptedListener(final Consumer<Throwable> runnable);
}
