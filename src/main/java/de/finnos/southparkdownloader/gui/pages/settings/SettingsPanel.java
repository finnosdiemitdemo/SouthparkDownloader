package de.finnos.southparkdownloader.gui.pages.settings;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.gui.components.FileChooser;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JFrame {

    public SettingsPanel() {
        init();
    }

    private void init () {
        setLayout(new MigLayout());

        FileChooser fileChooserFfmpeg = new FileChooser(s -> {
            Config.get().setFfmpegFilePath(s);
            Config.save();
        }, () -> Config.get().getFfmpegFilePath());

        FileChooser fileChooserFfprobe = new FileChooser(s -> {
            Config.get().setFfprobeFilePath(s);
            Config.save();
        }, () -> Config.get().getFfprobeFilePath());

        final var spinnerMaxFfmpegProcesses = new JSpinner();
        spinnerMaxFfmpegProcesses.addChangeListener(e -> {
            Config.get().setMaxFfmpegProcesses(((int) spinnerMaxFfmpegProcesses.getValue()));
            Config.save();
        });
        spinnerMaxFfmpegProcesses.setValue(Config.get().getMaxFfmpegProcesses());

        final var ffmpegPanel = new JPanel(new MigLayout());
        ffmpegPanel.setBorder(BorderFactory.createTitledBorder("FFmpeg"));

        final var label = new JLabel(I18N.i18n("ffmpeg_path"));
        ffmpegPanel.add(label);
        ffmpegPanel.add(fileChooserFfmpeg, "w 100%, wrap");

        ffmpegPanel.add(new JLabel(I18N.i18n("ffprobe_path")));
        ffmpegPanel.add(fileChooserFfprobe, "w 100%, wrap");

        ffmpegPanel.add(new JLabel(I18N.i18n("max_ffmpeg_processes")));
        ffmpegPanel.add(spinnerMaxFfmpegProcesses, "w 100%, wrap");

        add(ffmpegPanel, "w 100%,h 100%");
        setSize(new Dimension(500, 200));

        Helper.defaultDialog(this);
    }
}
