package de.finnos.southparkdownloader.services.download;

import de.finnos.southparkdownloader.gui.downloadepisode.single.DownloadItem;
import de.finnos.southparkdownloader.services.BaseServiceEvent;

public interface DownloadServiceEvent extends BaseServiceEvent {
    void startedItem (final DownloadItem item);
    void doneItem(final DownloadItem item);
}
