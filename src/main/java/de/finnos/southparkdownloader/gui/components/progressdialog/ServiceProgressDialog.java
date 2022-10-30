package de.finnos.southparkdownloader.gui.components.progressdialog;

import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.services.download.DownloadService;

public abstract class ServiceProgressDialog extends ProgressDialog {

    private final DownloadService downloadService;

    public ServiceProgressDialog(DialogEvent event) {
        super(event);

        downloadService = new DownloadService(this);
    }

    public DownloadService getDownloadService() {
        return downloadService;
    }

    public abstract void update ();
}
