package de.finnos.southparkdownloader.gui.pages.main;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.SouthparkDownloader;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.gui.pages.file.DownloadDatabaseUpdater;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;

public class MainFrame extends JFrame {
    private MainMenu menuBar;
    private JPanel panelContent;
    private JPanel panelSeasonsTree;

    private JTextArea labelEmptyDatabase;
    private JButton buttonCreateDatabase;

    private SeasonsTree tree;

    public MainFrame() {
        init();
        updateSeasonsTree();
    }

    private void init() {
        initMenuBar();
        initContent();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(menuBar, BorderLayout.NORTH);
        panel.add(panelContent, BorderLayout.CENTER);

        setSize(new Dimension(600, 600));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel);
        setTitle("Southpark Downloader");

        Helper.defaultDialog(this);
    }

    private void initContent() {
        panelContent = new JPanel(new MigLayout());
        panelSeasonsTree = new JPanel(new MigLayout("insets 0, al center center"));

        tree = new SeasonsTree();
        tree.init();

        labelEmptyDatabase = new JTextArea(I18N.i18n("download_database.empty_message"));
        labelEmptyDatabase.setEnabled(false);
        labelEmptyDatabase.setMargin(null);

        buttonCreateDatabase = new JButton(I18N.i18n("download_database.create"));
        buttonCreateDatabase.addActionListener(e -> updateDownloadDatabase());

        JComboBox<Config.DownloadLanguage> downloadLangComboBox = new JComboBox<>();
        for (final Config.DownloadLanguage value : Config.getAvailableDownloadLanguages()) {
            downloadLangComboBox.addItem(value);
        }
        downloadLangComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
        downloadLangComboBox.setSelectedItem(Config.getAvailableDownloadLanguages().stream()
            .filter(language -> language.getCode().equals(Config.get().getDownloadLang()))
            .findFirst()
            .orElse(Config.DownloadLanguage.DE));
        downloadLangComboBox.addActionListener(e -> {
            Config.get().setDownloadLang(((Config.DownloadLanguage) downloadLangComboBox.getSelectedItem()).getCode());
            Config.save();
            reloadTree();
        });

        JComboBox<Config.GuiLanguage> guiLangComboBox = new JComboBox<>();
        for (final Config.GuiLanguage value : Config.GuiLanguage.values()) {
            guiLangComboBox.addItem(value);
        }
        guiLangComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> new JLabel(value.getName()));
        guiLangComboBox.setSelectedItem(Arrays.stream(Config.GuiLanguage.values())
            .filter(language -> language.getCode().equals(Config.get().getGuiLang()))
            .findFirst()
            .orElse(Config.GuiLanguage.DE));
        guiLangComboBox.addActionListener(e -> {
            Config.get().setGuiLang(((Config.GuiLanguage) guiLangComboBox.getSelectedItem()).getCode());
            Config.save();
            SouthparkDownloader.restartApplication();
        });

        final var captionWidth = "120px";

        panelContent.add(new JLabel(I18N.i18n("download_domain")), "w %s".formatted(captionWidth));
        panelContent.add(new JLabel(Config.DOMAIN.getUrl()), "w 100px, wrap");

        panelContent.add(new JLabel(I18N.i18n("download_language")), "w %s".formatted(captionWidth));
        panelContent.add(downloadLangComboBox, "w 100px, wrap");

        panelContent.add(new JLabel(I18N.i18n("gui_language")), "w %s".formatted(captionWidth));
        panelContent.add(guiLangComboBox, "w 100px, wrap");

        panelContent.add(panelSeasonsTree, "span, h 100%, w 100%");
    }

    private void updateDownloadDatabase() {
        if (DownloadDatabase.isEmpty() ||
            Helper.showConfirmMessage(this,
                I18N.i18n("download_database.update"),
                I18N.i18n("download_database.update_question")) == JOptionPane.OK_OPTION
        ) {
            DownloadDatabaseUpdater.update(this::updateSeasonsTree);
        }
    }

    private void initMenuBar() {
        menuBar = new MainMenu();
        menuBar.init(this::updateDownloadDatabase);
    }

    private void updateSeasonsTree() {
        final var isDatabaseEmpty = DownloadDatabase.isEmpty();

        panelSeasonsTree.removeAll();
        if (isDatabaseEmpty) {
            panelSeasonsTree.add(labelEmptyDatabase, "wrap");
            panelSeasonsTree.add(buttonCreateDatabase, "growx");
        } else {
            final var scroller = new JScrollPane(tree);
            scroller.setSize(panelSeasonsTree.getSize());
            panelSeasonsTree.add(scroller, "w 100%, h 100%");
            reloadTree();
        }
    }

    private void reloadTree()  {
        tree.update();
    }
}