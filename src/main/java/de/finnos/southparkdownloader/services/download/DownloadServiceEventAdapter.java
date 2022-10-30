package de.finnos.southparkdownloader.services.download;

import de.finnos.southparkdownloader.gui.downloadepisode.single.DownloadItem;

public abstract class DownloadServiceEventAdapter implements DownloadServiceEvent {
    @Override
    public void startedItem(final DownloadItem item) { }

    @Override
    public void doneItem(DownloadItem item) { }

    @Override
    public void done(final boolean wasInterrupted, final Throwable throwable) {

    }

    @Override
    public void started() {

    }
}
