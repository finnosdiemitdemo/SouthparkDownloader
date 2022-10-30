package de.finnos.southparkdownloader.gui.downloadepisode.multiple;

import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.ImageLoader;
import de.finnos.southparkdownloader.classes.*;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;
import de.finnos.southparkdownloader.gui.components.progressdialog.ServiceProgressDialog;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressDialogEvent;
import de.finnos.southparkdownloader.gui.downloadepisode.single.DownloadItem;
import de.finnos.southparkdownloader.processes.ThreadProcess;
import de.finnos.southparkdownloader.services.BaseServiceEventAdapter;
import de.finnos.southparkdownloader.services.combine.CombineService;
import de.finnos.southparkdownloader.services.download.DownloadServiceEventAdapter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleSeasonsDownloadDialog extends ServiceProgressDialog {
    private final CombineService combineService;

    private JPanel buttonsPanel = new JPanel(new MigLayout("insets 0"));
    private JComboBox<Resolution> comboBoxResolution;
    private JButton buttonPrepareDownload;
    private JButton buttonStartDownload;
    private JButton buttonCombineParts;
    private JButton buttonCancelDownload;

    private final List<Season> seasons;

    public MultipleSeasonsDownloadDialog(DialogEvent event, final List<Season> seasons) {
        super(event);
        this.seasons = seasons;
        this.combineService = new CombineService(this);

        init();
    }

    private void init() {
        comboBoxResolution = new JComboBox<>();
        comboBoxResolution.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (value == null) {
                return new JLabel("");
            }
            return new JLabel(value.resolutionString());
        });

        buttonPrepareDownload = new JButton(I18N.i18n("prepare"), ImageLoader.IMAGE_DOWNLOAD);
        buttonPrepareDownload.addActionListener((e) -> prepareDownload());

        buttonStartDownload = new JButton(I18N.i18n("download"), ImageLoader.IMAGE_DOWNLOAD);
        buttonStartDownload.addActionListener(e -> download());

        buttonCombineParts = new JButton(I18N.i18n("episode.combine_parts"), ImageLoader.IMAGE_CONNECT);
        buttonCombineParts.addActionListener(e -> combine());

        buttonCancelDownload = new JButton(I18N.i18n("cancel"), ImageLoader.IMAGE_CANCEL);
        buttonCancelDownload.addActionListener(e -> {
            getDownloadService().stop();
        });

        getContentPanel().add(buttonsPanel);
        buttonsPanel.add(buttonPrepareDownload);

        setSize(new Dimension(900, 500));
    }

    private void prepareDownload() {
        buttonPrepareDownload.setEnabled(false);

        final ThreadProcess thread = new ThreadProcess() {
            @Override
            public void execute() {
                getProgressEvent().updateProgress("Load media for %d seasons".formatted(seasons.size()));
                for (final Season season : seasons) {
                    cancelProcessIfInterruptionIsInProgress();
                    getProgressEvent().initProgress(season.getEpisodes().size());
                    getProgressEvent().updateProgress(String.format("Load media for %d episodes...", season.getEpisodes().size()));

                    for (final Episode episode : season.getEpisodes()) {
                        cancelProcessIfInterruptionIsInProgress();
                        if (!episode.downloaded()) {
                            getProgressEvent().updateProgress(String.format("Load media for episode %d...", episode.getNumber()), episode.getNumber());

                            episode.loadMedia();
                        }
                    }
                }
            }
        };

        final var progressItemPanel = new ProgressItemPanel();
        addProgressItem(thread, I18N.i18n("prepare"), progressItemPanel, new ProgressDialogEvent(progressItemPanel));
        thread.addFinishedListener(() -> {
            final var allResolutionsSorted = seasons.stream()
                .flatMap(season -> season.getEpisodes().stream())
                .flatMap(episode -> episode.getParts().stream())
                .flatMap(episodePart -> episodePart.getStreams().stream())
                .map(EpisodePartStream::getResolution)
                .distinct()
                .sorted(Resolution::compareTo)
                .toList();
            allResolutionsSorted.forEach(resolution -> comboBoxResolution.addItem(resolution));
            comboBoxResolution.setSelectedItem(allResolutionsSorted.stream().findFirst().orElse(null));

            buttonsPanel.removeAll();
            buttonsPanel.add(new JLabel(I18N.i18n("episode.part.resolution")));
            buttonsPanel.add(comboBoxResolution);
            buttonsPanel.add(buttonStartDownload);
            buttonsPanel.add(buttonCombineParts);
            pack();
        });
        thread.addInterruptedListener(throwable -> buttonPrepareDownload.setEnabled(true));
        thread.start();
    }

    private void download() {
        comboBoxResolution.setEnabled(false);
        buttonStartDownload.setEnabled(false);
        buttonCombineParts.setEnabled(false);

        ArrayList<DownloadItem> downloadItemQueue = new ArrayList<>();

        for (final Season season : seasons) {
            if (season.downloaded()) {
                continue;
            }
            for (final Episode episode : season.getEpisodes()) {
                if (episode.downloaded()) {
                    continue;
                }

                for (final EpisodePart part : episode.getParts()) {
                    if (part.downloaded()) {
                        continue;
                    }
                    final var stream = part.getStreams().stream()
                        .filter(episodePartStream -> episodePartStream.getResolution().equals(((Resolution) comboBoxResolution.getSelectedItem())))
                        .findFirst()
                        .orElseGet(() -> part.getStreams().stream()
                            .max(Comparator.comparing(EpisodePartStream::getResolution))
                            .stream().findFirst()
                            .orElse(null));
                    if (stream != null) {
                        downloadItemQueue.add(new DownloadItem(episode, part, stream));
                    }
                }
            }
        }

        getDownloadService().start(downloadItemQueue, new DownloadServiceEventAdapter() {
            @Override
            public void done(final boolean wasInterrupted, final Throwable throwable) {
                if (wasInterrupted) {
                    resetToDownload();
                } else {
                    combine();
                }
            }
        });
    }

    private void combine() {
        for (final Season season : seasons) {
            for (final Episode episode : season.getEpisodes()) {
                combine(episode);
            }
        }
    }

    private void combine(final Episode episode) {
        if (episode.combined()) {
            return;
        }

        final ArrayList<String> parts = (ArrayList<String>) episode.getParts().stream()
            .filter(EpisodePart::downloaded)
            .map(EpisodePart::getOutputPath)
            .collect(Collectors.toList());

        combineService.start(List.of(new CombineService.CombineItem(parts, episode.getFinalPathDir(), episode.getFinalName())), new BaseServiceEventAdapter() {
            @Override
            public void done(final boolean wasInterrupted, final Throwable throwable) {
                resetToDownload();
            }
        });
    }

    private void resetToDownload() {
        comboBoxResolution.setEnabled(true);
        buttonStartDownload.setEnabled(true);
        buttonCombineParts.setEnabled(true);
    }

    @Override
    public void update() {

    }
}
