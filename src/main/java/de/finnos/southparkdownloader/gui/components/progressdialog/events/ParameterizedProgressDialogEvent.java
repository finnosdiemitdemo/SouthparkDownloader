package de.finnos.southparkdownloader.gui.components.progressdialog.events;

import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;

public class ParameterizedProgressDialogEvent<DATA> extends AbstractProgressDialogEvent{
    public ParameterizedProgressDialogEvent(final ProgressItemPanel progressDialog) {
        super(progressDialog);
    }

    public void finished(final DATA data) {

    }

    public void interrupted (final DATA data, final Throwable throwable) {

    }
}
