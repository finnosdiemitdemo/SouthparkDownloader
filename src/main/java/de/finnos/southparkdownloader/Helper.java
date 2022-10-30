package de.finnos.southparkdownloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Helper {
    private static final Logger LOG = LoggerFactory.getLogger(Helper.class);

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isFile()) {
                    if (!file.delete()) {
                        return false;
                    }
                } else if (file.isDirectory()) {
                    deleteDirectory(file);
                }
            }
        }

        return directoryToBeDeleted.delete();
    }

    public static void defaultDialog(final JFrame frame) {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setIconImage(ImageLoader.IMAGE_ICON);
    }

    public static String removeNotAllowedCharactersFromFileName(String filename) {
        filename = filename.replaceAll("\\\\", "");
        filename = filename.replaceAll("/", "");
        filename = filename.replaceAll(":", "");
        filename = filename.replaceAll("\\*", "");
        filename = filename.replaceAll("\\?", "");
        filename = filename.replaceAll("\"", "");
        filename = filename.replaceAll("<", "");
        filename = filename.replaceAll(">", "");
        filename = filename.replaceAll("\\|", "");

        return filename;
    }

    public static void openFileWithSystemDefaultProgram(final String path) {
        final File file = new File(path);
        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                showErrorMessage(null, ex.getMessage());
            }
        }
    }

    public static int showErrorMessage(final Component target, final String message) {
       return showMessage(target, I18N.i18n("error"), message, JOptionPane.ERROR_MESSAGE);
    }

    public static int showInfoMessage(final Component target, final String title, final String message) {
        return showMessage(target, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static int showConfirmMessage(final Component target, final String title, final String message) {
        String[] options = new String[2];
        options[0] = I18N.i18n("yes");
        options[1] = I18N.i18n("no");
        return JOptionPane.showOptionDialog(target, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    }

    public static int showMessage(final Component target, final String title, final String message, final int messageType) {
        String[] options = new String[1];
        options[0] = I18N.i18n("ok");
        return JOptionPane.showOptionDialog(target, message, title, JOptionPane.DEFAULT_OPTION, messageType, null, options, null);
    }

    public static void setEnabledWithTooltip(final JComponent component, final boolean enabled, final String tooltip) {
        component.setEnabled(enabled);
        component.setToolTipText(enabled ? null : tooltip);
    }
}