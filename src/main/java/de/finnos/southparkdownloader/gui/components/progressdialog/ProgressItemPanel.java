package de.finnos.southparkdownloader.gui.components.progressdialog;

import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressEvent;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ProgressItemPanel extends JPanel implements ProgressEvent {
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea textOutput = new JTextArea();

    public ProgressItemPanel() {
        initLayout();
    }

    private void initLayout () {
        setLayout(new MigLayout("insets 0"));
        textOutput.setEditable(false);
        textOutput.setBorder(null);

        final var scrollPane = new JScrollPane(textOutput);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> e.getAdjustable().setValue(e.getAdjustable().getMaximum()));
        scrollPane.setBorder(null);

        add(scrollPane, "w 100%, h 100%, wrap");
        add(progressBar, "growx");
    }

    public void updateProgressBar(int value) {
        progressBar.setValue(value);
    }

    public void initProgressBar(int steps) {
        progressBar.setMaximum(steps);
        updateProgressBar(0);
    }

    public void resetProgressBar() {
        initProgressBar(0);
    }

    public void appendText(final String message) {
        textOutput.append(message + "\n");
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void initProgress(int steps) {
        initProgressBar(steps);
    }

    @Override
    public void updateProgress(String message, int value) {
        updateProgressBar(value);
        updateProgress(message);
    }

    @Override
    public void updateProgress(String message) {
        appendText(message);
    }

    @Override
    public void updateProgress(int value, boolean increase) {
        updateProgressBar((increase ? getProgressBar().getValue() : 0) + value);
    }
}
