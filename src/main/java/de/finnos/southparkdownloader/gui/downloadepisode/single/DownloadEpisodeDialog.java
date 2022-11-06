package de.finnos.southparkdownloader.gui.downloadepisode.single;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.ImageLoader;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.EpisodePart;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.services.BaseServiceEventAdapter;
import de.finnos.southparkdownloader.services.combine.CombineService;
import de.finnos.southparkdownloader.services.download.DownloadService;
import de.finnos.southparkdownloader.services.download.DownloadServiceEventAdapter;
import de.finnos.southparkdownloader.services.metadata.MetaDataService;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DownloadEpisodeDialog extends ProgressDialog {
    enum DownloadAction {
        DOWNLOAD_ALL,
        DOWNLOAD_PART,
        NONE;
    }

    private final Episode episode;

    private final Map<DownloadItem, EpisodePartPanel> mapAllDownloadItems;
    private final DownloadService downloadService;
    private final CombineService combineService;
    private final MetaDataService metaDataService;

    private JButton buttonDownloadAllParts;
    private JButton buttonOpen;
    private JButton buttonDelete;
    private JButton buttonCombineParts;

    private JTextField textOutputPath;
    private JButton buttonPlay;

    private DownloadAction downloadAction = DownloadAction.NONE;

    public DownloadEpisodeDialog(final Episode episode, final DialogEvent event) {
        super(event);

        this.episode = episode;
        mapAllDownloadItems = new HashMap<>();
        downloadService = new DownloadService(this);
        combineService = new CombineService(this);
        metaDataService = new MetaDataService(this);

        init();
    }

    private void init() {
        episode.loadMedia();

        final JPanel panelBasicInfo = new JPanel(new MigLayout());
        panelBasicInfo.setBorder(BorderFactory.createTitledBorder(I18N.i18n("episode.info")));

        final JPanel panelButtonsBasicInfo = new JPanel(new MigLayout("insets 0"));

        buttonDelete = new JButton(I18N.i18n("episode.delete"), ImageLoader.IMAGE_DELETE);
        buttonDelete.addActionListener(e -> {
            if (episode.downloaded()) {
                episode.delete();
                update();
            }
        });

        buttonOpen = new JButton(I18N.i18n("open_folder"), ImageLoader.IMAGE_OPEN_FOLDER);
        buttonOpen.addActionListener(e -> {
            if (episode.canOpenFolder()) {
                episode.openFolder();
            }
        });

        buttonPlay = new JButton(I18N.i18n("play"), ImageLoader.IMAGE_PLAY);
        buttonPlay.addActionListener(e -> {
            if (episode.canPlay()) {
                episode.play();
            }
        });

        panelButtonsBasicInfo.add(buttonDelete);
        panelButtonsBasicInfo.add(buttonOpen);
        panelButtonsBasicInfo.add(buttonPlay);

        panelBasicInfo.add(new JLabel(I18N.i18n("episode.episode_plus_season")));
        panelBasicInfo.add(new JLabel(String.format("S%02d / E%02d", episode.getSeason().getNumber(), episode.getNumber())), "wrap");

        panelBasicInfo.add(new JLabel(I18N.i18n("episode.name")));
        panelBasicInfo.add(new JLabel(episode.getName()), "wrap");

        panelBasicInfo.add(new JLabel(I18N.i18n("episode.duration")));
        panelBasicInfo.add(new JLabel(formatDuration(episode.getDuration())), "wrap");

        panelBasicInfo.add(new JLabel(I18N.i18n("episode.description")), "top");
        panelBasicInfo.add(new JLabel(String.format("<html><div style='width: " + getSize().getWidth() + "px;'>%s</div></html>", episode.getDescription())),
            "wrap");

        panelBasicInfo.add(new JLabel(I18N.i18n("episode.publish_date")));
        panelBasicInfo.add(new JLabel(episode.getPublishDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))), "wrap");

        panelBasicInfo.add(panelButtonsBasicInfo, "span");

        final JPanel panelDownloadInfo = new JPanel(new MigLayout());
        panelDownloadInfo.setBorder(BorderFactory.createTitledBorder(I18N.i18n("episode.download_info")));

        textOutputPath = new JTextField(episode.getFinalName());
        textOutputPath.setEnabled(false);

        EpisodePartsPanel partsPanel = new EpisodePartsPanel(episode, this);

        buttonDownloadAllParts = new JButton(I18N.i18n("episode.download_all_parts"), ImageLoader.IMAGE_DOWNLOAD);
        buttonDownloadAllParts.addActionListener(e -> {
            if (!episode.downloaded()) {
                downloadAll();
            }
        });

        buttonCombineParts = new JButton(I18N.i18n("episode.combine_parts"), ImageLoader.IMAGE_CONNECT);
        buttonCombineParts.addActionListener(e -> {
            combine();
        });

        JPanel panelButtonsDownload = new JPanel(new MigLayout("insets 0"));
        panelButtonsDownload.add(buttonDownloadAllParts);
        panelButtonsDownload.add(buttonCombineParts);

        panelDownloadInfo.add(new JLabel(I18N.i18n("episode.output_name")));
        panelDownloadInfo.add(textOutputPath, "wrap,w 300px,gapbottom 10px");

        panelDownloadInfo.add(new JLabel(I18N.i18n("episode.parts")), "top");
        panelDownloadInfo.add(partsPanel, "wrap,w 300px");

        panelDownloadInfo.add(panelButtonsDownload, "span");

        getContentPanel().add(panelBasicInfo, "w 100%,wrap");
        getContentPanel().add(panelDownloadInfo, "w 100%,");

        setSize(new Dimension(1000, 800));
        setTitle(I18N.i18n("download") + " " + episode.getName());

        for (EpisodePartPanel panel : partsPanel.getPanels()) {
            mapAllDownloadItems.put(new DownloadItem(episode, panel.getEpisodePart()), panel);
        }

        update();
    }

    public String formatDuration(final Duration duration) {
        return "%d:%s".formatted(duration.toMinutesPart(), duration.toSecondsPart());
    }

    private void downloadAll() {
        downloadAction = DownloadAction.DOWNLOAD_ALL;
        update();

        ArrayList<DownloadItem> downloadItemQueue = new ArrayList<>();

        for (Map.Entry<DownloadItem, EpisodePartPanel> entry : mapAllDownloadItems.entrySet()) {
            if (!entry.getKey().getEpisodePart().downloaded()) {
                final DownloadItem item = entry.getKey();
                item.setStream(entry.getValue().getStream());
                downloadItemQueue.add(item);
            }
        }

        downloadService.start(downloadItemQueue, new DownloadServiceEventAdapter() {
            @Override
            public void done(final boolean wasInterrupted, final Throwable throwable) {
                downloadAction = DownloadAction.NONE;
                update();
                if (!wasInterrupted) {
                    combine();
                }
            }

            @Override
            public void doneItem(final DownloadItem item) {
                update();
            }
        });
    }

    public void downloadPart(final EpisodePart part, final EpisodePartPanel panel) {
        downloadAction = DownloadAction.DOWNLOAD_PART;

        final DownloadItem item = findDownloadItem(part, panel);
        if (item != null) {
            downloadService.start(List.of(item), new DownloadServiceEventAdapter() {
                @Override
                public void doneItem(final DownloadItem item) {
                    update();
                }

                @Override
                public void done(final boolean wasInterrupted, final Throwable throwable) {
                    downloadAction = DownloadAction.NONE;
                    update();

                    if (!wasInterrupted && mapAllDownloadItems.values().stream()
                        .allMatch(episodePartPanel -> episodePartPanel.getEpisodePart().downloaded())) {
                        combine();
                    }
                }
            });
        }
        update();
    }

    public void deletePart(final EpisodePart part, final EpisodePartPanel panel) {
        if (findDownloadItem(part, panel) != null) {
            part.delete();
            update();
        }
    }

    private DownloadItem findDownloadItem(final EpisodePart part, final EpisodePartPanel panel) {
        final Optional<DownloadItem> requestedItem = mapAllDownloadItems.keySet().stream().filter(item -> item.getEpisodePart() == part).findFirst();
        if (requestedItem.isPresent() && mapAllDownloadItems.get(requestedItem.get()) == panel) {
            requestedItem.get().setStream(panel.getStream());
            return requestedItem.get();
        }

        return null;
    }

    private void combine() {
        if (episode.downloaded()) {
            final ArrayList<DownloadItem> items = new ArrayList<>(mapAllDownloadItems.keySet());
            items.sort(Comparator.comparing(item -> item.getEpisodePart().getIndex()));

            final Optional<DownloadItem> firstItem = items.stream().findFirst();
            if (firstItem.isPresent()) {
                final ArrayList<String> paths =
                    (ArrayList<String>) items.stream().map(item -> item.getEpisodePart().getOutputPath()).collect(Collectors.toList());
                combineService.start(List.of(new CombineService.CombineItem(paths, episode.getFinalPathDir(), getFilename())),
                    new BaseServiceEventAdapter() {
                        @Override
                        public void done(final boolean wasInterrupted, final Throwable throwable) {
                            if (!wasInterrupted) {
                                metMetaInfos();
                            }
                        }
                    });
            }
        }
    }

    private void metMetaInfos() {
        metaDataService.start(List.of(new MetaDataService.MetaProgressItem(episode)), new BaseServiceEventAdapter() {
            @Override
            public void done(final boolean wasInterrupted, final Throwable throwable) {
                update();
            }
        });
    }

    public void update() {
        if (downloadAction == DownloadAction.NONE) {
            final var downloaded = episode.downloaded();
            final var folderExists = episode.folderExists();
            Helper.setEnabledWithTooltip(buttonDelete, folderExists, I18N.i18n("download_episode.tooltip.unable_to_delete_folder"));
            Helper.setEnabledWithTooltip(buttonOpen, folderExists, I18N.i18n("download_episode.tooltip.unable_to_open_folder"));
            Helper.setEnabledWithTooltip(buttonPlay, episode.canPlay(), I18N.i18n("download_episode.tooltip.unable_to_play"));
            Helper.setEnabledWithTooltip(buttonCombineParts, downloaded, I18N.i18n("download_episode.tooltip.unable_to_combine"));
            buttonDownloadAllParts.setEnabled(true);
            mapAllDownloadItems.values().forEach(episodePartPanel -> episodePartPanel.update(downloadAction));
        } else {
            final var message = I18N.i18n("download_episode.tooltip.unable_to_execute_during_download");
            Stream.of(buttonDelete, buttonOpen, buttonPlay, buttonDownloadAllParts, buttonCombineParts)
                .forEach(jButton -> Helper.setEnabledWithTooltip(jButton, false, message));

            if (downloadAction == DownloadAction.DOWNLOAD_PART) {
                for (final Map.Entry<DownloadItem, EpisodePartPanel> entry : mapAllDownloadItems.entrySet()) {
                    boolean isDownloading = false;
                    if (downloadService.getDownloadingItems().containsKey(entry.getKey())) {
                        isDownloading = downloadService.getDownloadingItems().keySet().stream()
                            .anyMatch(downloadItem -> downloadItem.equals(entry.getKey()));
                    }

                    entry.getValue().update(isDownloading ? downloadAction : DownloadAction.NONE);
                }
            } else if (downloadAction == DownloadAction.DOWNLOAD_ALL) {
                mapAllDownloadItems.values().forEach(episodePartPanel -> episodePartPanel.update(downloadAction));
            }
        }
    }

    private String getFilename() {
        return Helper.removeNotAllowedCharactersFromFileName(textOutputPath.getText());
    }
}
