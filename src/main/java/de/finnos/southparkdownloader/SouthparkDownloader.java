package de.finnos.southparkdownloader;

import com.formdev.flatlaf.FlatDarkLaf;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.data.Setup;
import de.finnos.southparkdownloader.ffmpeg.FfmpegExecutionHelper;
import de.finnos.southparkdownloader.gui.pages.main.MainFrame;
import de.finnos.southparkdownloader.gui.pages.settings.SettingsPanel;
import de.finnos.southparkdownloader.processes.ProcessesHolder;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;

public class SouthparkDownloader {
    private static final Logger LOG = LoggerFactory.getLogger(SouthparkDownloader.class);

    private static MainFrame MAIN_FRAME;

    public static Color getBorderColor() {
        return UIManager.getColor("Separator.foreground");
    }

    public static void main(String[] args) {
        startApplication();
    }

    public static void startApplication() {
        FlatDarkLaf.setup();
        IconFontSwing.register(FontAwesome.getIconFont());

        try {
            ProcessesHolder.init();
            Setup.init();
            DownloadDatabase.init();
            Config.init();

            openMainFrame();
        } catch (final StartupException startupException) {
            Helper.showErrorMessage(null, startupException.getMessage());
            LOG.error("Unable to start the app", startupException);
            System.exit(0);
        }
    }

    public static boolean ffmpegExecutablePathsValid() {
        return FfmpegExecutionHelper.isFFmpegFileExecutable(Config.get().getFfmpegFilePath())
            && FfmpegExecutionHelper.isFFmpegFileExecutable(Config.get().getFfprobeFilePath());
    }

    public static void openMainFrame() {
        if (ffmpegExecutablePathsValid()) {
            MAIN_FRAME = new MainFrame();
        } else {
            // check if there is a bin folder with ffmpeg binaries
            if (Files.exists(Path.of("bin", "ffmpeg.exe"))
                && Files.exists(Path.of("bin", "ffprobe.exe"))
            ) {
                Config.get().setFfmpegFilePath("bin/ffmpeg.exe");
                Config.get().setFfprobeFilePath("bin/ffprobe.exe");

                if (ffmpegExecutablePathsValid()) {
                    MAIN_FRAME = new MainFrame();
                    return;
                }
            }

            // Open settings dialog
            if (Helper.showConfirmMessage(null, I18N.i18n("startup.error.invalid_ffmpeg_paths.title"),
                I18N.i18n("startup.error.invalid_ffmpeg_paths.message")) == JOptionPane.OK_OPTION) {
                final var settingsPanel = new SettingsPanel();
                settingsPanel.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(final WindowEvent e) {
                        super.windowClosing(e);
                        settingsPanel.dispose();
                    }

                    @Override
                    public void windowClosed(final WindowEvent e) {
                        super.windowClosed(e);

                        openMainFrame();
                    }
                });
            }
        }
    }

    public static void restartApplication() {
        MAIN_FRAME.dispose();
        MAIN_FRAME = new MainFrame();
    }

    public static class StartupException extends RuntimeException {
        public StartupException(final String message) {
            super(message);
        }

        public StartupException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public StartupException(final Throwable cause) {
            super(cause);
        }
    }
}
