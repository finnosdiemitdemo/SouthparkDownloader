package de.finnos.southparkdownloader.services;

public abstract class BaseServiceEventAdapter implements BaseServiceEvent {
    @Override
    public void done(final boolean wasInterrupted, final Throwable throwable) {

    }

    @Override
    public void started() {

    }
}
