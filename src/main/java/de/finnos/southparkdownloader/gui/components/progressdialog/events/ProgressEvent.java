package de.finnos.southparkdownloader.gui.components.progressdialog.events;

public interface ProgressEvent {
    void initProgress(final int steps);
    void updateProgress(final String message, final int value);
    void updateProgress(final String message);
    void updateProgress(final int value, final boolean increase);
}
