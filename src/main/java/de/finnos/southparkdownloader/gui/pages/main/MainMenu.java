package de.finnos.southparkdownloader.gui.pages.main;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.pages.help.AboutDialog;
import de.finnos.southparkdownloader.gui.pages.settings.SettingsPanel;
import de.finnos.southparkdownloader.gui.pages.tools.RepairParts;

import javax.swing.*;

public class MainMenu extends JMenuBar {
    public void init (final Runnable updateDownloadDatabase, final Runnable findNewEpisodes) {
        final var menuDownloadDatabase = new JMenu(I18N.i18n("download_database"));

        final JMenuItem menuItemFileUpdateDownloadDatabase = new JMenuItem(I18N.i18n("menu.file.update_download_database"));
        menuItemFileUpdateDownloadDatabase.addActionListener(e -> updateDownloadDatabase.run());

        final JMenuItem menuItemFileFindNewEpisodes = new JMenuItem(I18N.i18n("menu.file.find_new_episodes"));
        menuItemFileFindNewEpisodes.addActionListener(e -> findNewEpisodes.run());

        menuDownloadDatabase.add(menuItemFileUpdateDownloadDatabase);
        menuDownloadDatabase.add(menuItemFileFindNewEpisodes);

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
        final JMenuItem menuItemHelpInfo = new JMenuItem(I18N.i18n("menu.help.about"));
        menuItemHelpInfo.addActionListener(e -> {
            Helper.defaultDialog(new AboutDialog());
        });

        menuHelp.add(menuItemHelpInfo);

        final JMenu menuSettings = new JMenu(I18N.i18n("menu.settings"));
        final JMenuItem menuItemSettings = new JMenuItem(I18N.i18n("menu.settings.settings"));
        menuItemSettings.addActionListener(e -> {
            new SettingsPanel();
        });
        menuSettings.add(menuItemSettings);

        add(menuDownloadDatabase);
        add(menuTools);
        add(menuSettings);
        add(menuHelp);
    }
}
