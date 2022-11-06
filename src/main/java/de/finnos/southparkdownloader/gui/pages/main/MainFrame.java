package de.finnos.southparkdownloader.gui.pages.main;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.SouthparkDownloader;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.gui.pages.downloaddatabase.DownloadDatabaseUpdater;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Arrays;

public class MainFrame extends JFrame {
    private MainMenu menuBar;
    private final JPanel panelContent = new JPanel(new MigLayout());
    ;
    private final JPanel panelSeasonsTree = new JPanel(new MigLayout("insets 0, al center center"));

    private final JTextArea labelEmptyDatabase = new JTextArea(I18N.i18n("download_database.empty_message"));
    private final JButton buttonCreateDatabase = new JButton(I18N.i18n("download_database.create"));

    private final SeasonsTree tree = new SeasonsTree();

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
        labelEmptyDatabase.setMargin(null);
        labelEmptyDatabase.setBackground(new Color(0, 0, 0, 0));
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
            updateSeasonsTree();
            repaint();
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

        final var tfSearchEpisodes = new JTextField();
        tfSearchEpisodes.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                search(tfSearchEpisodes.getText());
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                search(tfSearchEpisodes.getText());
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                search(tfSearchEpisodes.getText());
            }
        });

        final var panelSearchPanel = new JPanel(new MigLayout("insets 0, al right center"));
        panelSearchPanel.add(new JLabel(I18N.i18n("search_for_episodes")));
        panelSearchPanel.add(tfSearchEpisodes, "w 150px");

        final var captionWidth = "120px";

        panelContent.add(new JLabel(I18N.i18n("download_domain")), "w %s".formatted(captionWidth));
        panelContent.add(new JLabel(Config.DOMAIN.getUrl()), "w 100px, wrap");

        panelContent.add(new JLabel(I18N.i18n("download_language")), "w %s".formatted(captionWidth));
        panelContent.add(downloadLangComboBox, "w 100px, wrap");

        panelContent.add(new JLabel(I18N.i18n("gui_language")), "w %s".formatted(captionWidth));
        panelContent.add(guiLangComboBox, "w 100px");
        panelContent.add(panelSearchPanel, "w 100%, wrap");

        panelContent.add(panelSeasonsTree, "span, h 100%, w 100%");
    }

    private void search(final String searchString) {
        tree.search(searchString);
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

    private void findNewEpisodes() {
        DownloadDatabaseUpdater.findNewEpisodes(this::updateSeasonsTree);
    }

    private void initMenuBar() {
        menuBar = new MainMenu();
        menuBar.init(this::updateDownloadDatabase, this::findNewEpisodes);
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

    private void reloadTree() {
        tree.update();
    }
}