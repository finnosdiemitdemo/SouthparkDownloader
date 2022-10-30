package de.finnos.southparkdownloader.services;

public interface BaseServiceEvent {
    void done(final boolean wasInterrupted, final Throwable throwable);

    void started();
}
