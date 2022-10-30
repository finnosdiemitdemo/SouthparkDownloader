package de.finnos.southparkdownloader.gui.pages.main;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.pages.settings.SettingsPanel;
import de.finnos.southparkdownloader.gui.pages.tools.RepairParts;

import javax.swing.*;
import java.io.File;

public class MainMenu extends JMenuBar {
    public void init (final Runnable updateDownloadDatabase) {
        final JMenuItem menuItemFileUpdateDownloadDatabase = new JMenuItem(I18N.i18n("menu.file.update_download_database"));
        menuItemFileUpdateDownloadDatabase.addActionListener(e -> updateDownloadDatabase.run());

        final JMenuItem menuItemFileExit = new JMenuItem(I18N.i18n("exit"));
        menuItemFileExit.addActionListener(e -> System.exit(0));

        final JMenu menuFile = new JMenu(I18N.i18n("file"));
        menuFile.add(menuItemFileUpdateDownloadDatabase);
        menuFile.add(new JSeparator());
        menuFile.add(menuItemFileExit);

        final JMenu menuTools = new JMenu(I18N.i18n("menu.tools"));
        final JMenuItem menuItemToolsRepairParts = new JMenuItem(I18N.i18n("menu.tools.repair_parts"));
        menuItemToolsRepairParts.addActionListener(e -> {
            new RepairParts(new DialogEvent() {
                @Override
                public void closed() {

                }

                @Override
                public void done() {

                }
            });
        });

        menuTools.add(menuItemToolsRepairParts);

        final JMenu menuHelp = new JMenu(I18N.i18n("menu.help"));
        final JMenuItem menuItemHelpInfo = new JMenuItem(I18N.i18n("menu.help.info"));
        menuItemHelpInfo.addActionListener(e -> {
            String message = "";
            message += String.format("%s: %s\r\n", I18N.i18n("info.download_database_path"), new File(DownloadDatabase.DOWNLOAD_DATABASE_PATH).getAbsolutePath());
            message += String.format("%s: %s\r\n", I18N.i18n("info.config_path"), new File(Config.CONFIG_PATH).getAbsolutePath());
            Helper.showInfoMessage(this, message, I18N.i18n("menu.help.info"));
        });

        menuHelp.add(menuItemHelpInfo);

        final JMenu menuSettings = new JMenu(I18N.i18n("menu.settings"));
        final JMenuItem menuItemSettings = new JMenuItem(I18N.i18n("menu.settings.settings"));
        menuItemSettings.addActionListener(e -> {
            new SettingsPanel();
        });
        menuSettings.add(menuItemSettings);

        add(menuFile);
        add(menuTools);
        add(menuSettings);
        add(menuHelp);
    }
}
