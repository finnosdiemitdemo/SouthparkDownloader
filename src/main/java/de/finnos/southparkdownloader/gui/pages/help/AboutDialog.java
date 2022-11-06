package de.finnos.southparkdownloader.gui.pages.help;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.data.Setup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Properties;

public class AboutDialog extends JFrame {
    private static final String VERSION;
    private static final LocalDate DATE;

    static {
        final var properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("info.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VERSION = properties.getProperty("version");
        DATE = LocalDate.parse(properties.getProperty("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public AboutDialog() {
        setLayout(new MigLayout("insets 0 20 20 20"));

        final var versionString = "SouthparkDownloader v" + VERSION;
        final var lVersion = new JLabel("<html><h1 style='margin:0;'>%s</h1></html>".formatted(versionString));

        final var formattedBuildDate = DATE.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        final var buildString = I18N.i18n("build.date") + ": " + formattedBuildDate;
        final var lDate = new JLabel("<html><h4 style='margin:0;'>%s</h4></html>".formatted(buildString));

        final var buttonOpenDatabaseFolder = new JButton(I18N.i18n("open"));
        buttonOpenDatabaseFolder.addActionListener(e -> Helper.openFileWithSystemDefaultProgram(Setup.getDatabaseFolderPath()));

        final var buttonOpenEpisodesFolder = new JButton(I18N.i18n("open"));
        buttonOpenEpisodesFolder.addActionListener(e -> Helper.openFileWithSystemDefaultProgram(Setup.getEpisodesFolderPath()));

        add(lVersion, "wrap, span");
        add(lDate, "wrap, span");
        add(new JSeparator(), "wrap");
        add(new JLabel(I18N.i18n("info.database_path") + ": "));
        add(new JLabel(Setup.getDatabaseFolderPath()));
        add(buttonOpenDatabaseFolder, "wrap");
        add(new JLabel(I18N.i18n("info.episodes_path") + ": "));
        add(new JLabel(Setup.getEpisodesFolderPath()));
        add(buttonOpenEpisodesFolder, "wrap");

        setTitle(I18N.i18n("menu.help.about"));
        pack();
    }
}
