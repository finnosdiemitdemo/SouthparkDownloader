package de.finnos.southparkdownloader.gui.components.progressdialog.events;

import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;

public abstract class AbstractProgressDialogEvent implements ProgressEvent {

    private final ProgressItemPanel progressItemPanel;

    public AbstractProgressDialogEvent(ProgressItemPanel progressDialog) {
        this.progressItemPanel = progressDialog;
    }

    @Override
    public void initProgress(int steps) {
        progressItemPanel.initProgressBar(steps);
    }

    @Override
    public void updateProgress(String message, int value) {
        progressItemPanel.updateProgressBar(value);
        updateProgress(message);
    }

    @Override
    public void updateProgress(String message) {
        progressItemPanel.appendText(message);
    }

    @Override
    public void updateProgress(int value, boolean increase) {
        progressItemPanel.updateProgressBar((increase ? progressItemPanel.getProgressBar().getValue() : 0) + value);
    }
}
