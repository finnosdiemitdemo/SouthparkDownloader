package de.finnos.southparkdownloader.gui.pages.tools;

import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.ImageLoader;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.EpisodePart;
import de.finnos.southparkdownloader.classes.EpisodePartStream;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.ffmpeg.ffprobe.FfprobeHelper;
import de.finnos.southparkdownloader.ffmpeg.ffprobe.VideoInfo;
import de.finnos.southparkdownloader.gui.DialogEvent;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;
import de.finnos.southparkdownloader.gui.components.progressdialog.ServiceProgressDialog;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressDialogEvent;
import de.finnos.southparkdownloader.gui.downloadepisode.single.DownloadItem;
import de.finnos.southparkdownloader.processes.ThreadProcess;
import de.finnos.southparkdownloader.services.download.DownloadService;
import de.finnos.southparkdownloader.services.download.DownloadServiceEventAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class RepairParts extends ServiceProgressDialog {

    private final DownloadService downloadService;

    private Map<EpisodePart, VideoInfo> mapBrokenParts;

    private JButton buttonFind;
    private JButton buttonRepair;

    public RepairParts(DialogEvent event) {
        super(event);

        downloadService = new DownloadService(this);

        mapBrokenParts = new HashMap<>();

        init();
    }

    @Override
    public void update() {

    }

    private void init() {
        buttonFind = new JButton(I18N.i18n("repair.find_broken_parts"), ImageLoader.IMAGE_FIND);
        buttonFind.addActionListener(e -> findBrokenParts());

        buttonRepair = new JButton(I18N.i18n("repair"), ImageLoader.IMAGE_REPAIR);
        buttonRepair.addActionListener(e -> repair());

        getContentPanel().add(buttonFind);
        getContentPanel().add(buttonRepair);

        setSize(new Dimension(700, 700));
        setTitle(I18N.i18n("repair.repair_parts"));
    }

    private void findBrokenParts() {
        ThreadProcess thread = new ThreadProcess() {
            @Override
            public void execute() {
                mapBrokenParts.clear();

                final List<Season> seasons = DownloadDatabase.get().getSeasons();
                final Map<EpisodePart, File> parts = seasons.stream()
                    .flatMap(season -> season.getEpisodes().stream())
                    .flatMap(episode -> episode.getParts().stream())
                    .filter(EpisodePart::downloaded)
                    .collect(Collectors.toMap(episodePart -> episodePart, episodePart -> new File(episodePart.getOutputPath())));

                getProgressEvent().updateProgress(String.format("Check %d parts...", parts.size()));
                getProgressEvent().initProgress(parts.size());

                int index = 0;
                for (final EpisodePart part : parts.keySet()) {
                    getProgressEvent().updateProgress(String.format("Check part %d...", index));

                    final File file = parts.get(part);
                    final VideoInfo info = FfprobeHelper.getInfo(file.getAbsolutePath());

                    if (info != null) {
                        final boolean broken = Math.abs(info.getExpectedDurationInSeconds() - info.getActualDurationInSeconds()) > 1;
                        final String status = !broken ? "ok" : "broken";

                        if (broken) {
                            mapBrokenParts.put(part, info);
                        }

                        getProgressEvent().updateProgress(String.format("File %s is %s", file.getAbsolutePath(), status));
                        if (broken) {
                            getProgressEvent().updateProgress(String.format("Expected duration=%s actual duration=%s",
                                formatDurationSeconds(info.getExpectedDurationInSeconds()),
                                formatDurationSeconds(info.getActualDurationInSeconds())
                            ));
                        }
                        getProgressEvent().updateProgress(index, false);
                    } else {
                        getProgressEvent().updateProgress(String.format("File %s is broken", file.getAbsolutePath()));
                        getProgressEvent().updateProgress(index, false);

                        mapBrokenParts.put(part, null);
                    }

                    index++;
                }

                getProgressEvent().updateProgress(String.format("%d parts are broken", mapBrokenParts.size()));
            }
        };

        removeFinishedTabs();
        final var progressItemPanel = new ProgressItemPanel();
        addProgressItem(thread, I18N.i18n("repair.repair_parts"), progressItemPanel, new ProgressDialogEvent(progressItemPanel), false, true);
        thread.start();
    }

    private String formatDurationSeconds(final int seconds) {
        final var minutes = seconds / 60;

        final var minutesInt = minutes;
        final var secondsInt = seconds - (minutesInt * 60);

        return "%d min %d sec".formatted(minutesInt, secondsInt);
    }

    private void repair() {
        ThreadProcess thread = new ThreadProcess() {
            @Override
            public void execute() {
                ArrayList<DownloadItem> downloadItems = new ArrayList<>();

                getProgressEvent().updateProgress(String.format("Prepare %d parts...", mapBrokenParts.size()));
                getProgressEvent().initProgress(mapBrokenParts.size());

                for (final Map.Entry<EpisodePart, VideoInfo> entry : mapBrokenParts.entrySet()) {
                    final EpisodePart part = entry.getKey();
                    final Episode episode = part.getEpisode();

                    getProgressEvent().updateProgress(
                        String.format("Prepare part in episode %d of season %d...", part.getEpisode().getNumber(), episode.getSeason().getNumber()));
                    part.loadMedia();

                    // Find the stream
                    final Optional<EpisodePartStream> opStream;
                    if (entry.getValue() != null) {
                        final int resolution = entry.getValue().getHeight() * entry.getValue().getWidth();
                        opStream = part.getStreams().stream().filter(s -> s.getResolution().resolution() == resolution).findFirst();
                    } else {
                        opStream = part.getStreams().stream().max(Comparator.comparing(EpisodePartStream::getResolution));
                    }

                    opStream.ifPresent(stream -> {
                        downloadItems.add(new DownloadItem(episode, part, stream));
                    });

                    getProgressEvent().updateProgress(1, true);
                }

                getProgressEvent().updateProgress(String.format("Repair %d parts...", downloadItems.size()));

                downloadService.start(downloadItems, new DownloadServiceEventAdapter() {
                    @Override
                    public void startedItem(DownloadItem item) {
                        getProgressEvent().updateProgress(String.format("Repair part in episode %d of season %d...", item.getEpisodePart().getEpisode().getNumber(),
                            item.getEpisodePart().getEpisode().getSeason().getNumber()));
                    }
                });
            }
        };

        removeFinishedTabs();
        final var progressItemPanel = new ProgressItemPanel();
        addProgressItem(thread, I18N.i18n("repair.redownload_broken_parts"), progressItemPanel, new ProgressDialogEvent(progressItemPanel), false, true);
        thread.start();
    }
}
