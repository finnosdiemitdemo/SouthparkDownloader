package de.finnos.southparkdownloader.gui.components.progressdialog.events;

import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;

public class SimpleProgressDialogEvent extends AbstractProgressDialogEvent {
    public SimpleProgressDialogEvent(final ProgressItemPanel progressDialog) {
        super(progressDialog);
    }

    public void finished() {

    }

    public void interrupted (final Throwable throwable) {

    }
}
