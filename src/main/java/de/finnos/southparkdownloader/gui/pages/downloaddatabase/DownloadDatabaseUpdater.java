package de.finnos.southparkdownloader.gui.pages.downloaddatabase;

import de.finnos.southparkdownloader.DownloadDatabaseHelper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.data.DownloadDatabase;
import de.finnos.southparkdownloader.data.Setup;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressDialog;
import de.finnos.southparkdownloader.gui.components.progressdialog.ProgressItemPanel;
import de.finnos.southparkdownloader.gui.components.progressdialog.events.ProgressDialogEvent;
import de.finnos.southparkdownloader.processes.ThreadProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DownloadDatabaseUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadDatabaseUpdater.class);

    public static void update(final Runnable callback) {
        Setup.init();

        final var dialog = new ProgressDialog();
        ThreadProcess thread = new ThreadProcess(dialog) {
            @Override
            public void execute() throws Exception {
                ArrayList<Season> seasons;

                getProgressEvent().updateProgress("Load seasons...");
                seasons = DownloadDatabaseHelper.findSeasons();
                getProgressEvent().updateProgress(String.format("%d seasons found!", seasons.size()));

                for (Season season : seasons) {
                    cancelProcessIfInterruptionIsInProgress();
                    loadEpisodes(season, this);
                    getProgressEvent().updateProgress("");
                }

                getProgressEvent().updateProgress("Done!");

                List<Season> emptySeasons = seasons.stream()
                    .filter(season -> season.getEpisodes().isEmpty())
                    .collect(Collectors.toList());
                seasons.removeAll(emptySeasons);

                DownloadDatabase.update(seasons);

                callback.run();
            }
        };

        executeThread(dialog, thread);
    }

    private static void loadEpisodes(final Season season, final ThreadProcess threadProcess) throws DownloadDatabaseHelper.DownloadHelperException {
        threadProcess.getProgressEvent().updateProgress("Load Episodes for season " + season.getNumber() + "...");
        season.getEpisodes().addAll(DownloadDatabaseHelper.findEpisodes(season));
        threadProcess.getProgressEvent().updateProgress(String.format("%d episodes found!", season.getEpisodes().size()));

        threadProcess.getProgressEvent().initProgress(season.getEpisodes().size());
        for (Episode episode : season.getEpisodes()) {
            threadProcess.cancelProcessIfInterruptionIsInProgress();
            loadEpisodeParts(season, episode, threadProcess);
        }
    }

    private static void loadEpisodeParts(final Season season,
                                         final Episode episode,
                                         final ThreadProcess threadProcess) throws DownloadDatabaseHelper.DownloadHelperException {
        LOG.trace("DownloadDatabaseUpdater handle episode S%s E%s".formatted(season.getNumber(), episode.getNumber()));
        threadProcess.getProgressEvent().updateProgress("Load part download urls for episode " + episode.getNumber(), episode.getNumber());
        episode.setPartsGeneratorUrls(DownloadDatabaseHelper.findEpisodePartUrls(episode));
    }

    public static void findNewEpisodes(final Runnable callback) {
        Setup.init();

        final var dialog = new ProgressDialog();
        ThreadProcess thread = new ThreadProcess(dialog) {
            @Override
            public void execute() throws Exception {
                final var currentSeasons = DownloadDatabase.get().getSeasons();

                boolean foundNewEpisodes = false;
                getProgressEvent().updateProgress("Find new episodes...");
                for (final Season season : DownloadDatabaseHelper.findSeasons()) {
                    cancelProcessIfInterruptionIsInProgress();
                    final var optCurrentSeason = currentSeasons.stream()
                        .filter(season1 -> season1.getNumber().equals(season.getNumber()))
                        .findFirst();
                    if (optCurrentSeason.isEmpty()) {
                        foundNewEpisodes = true;
                        getProgressEvent().updateProgress("Found new season " + season.getNumber());
                        loadEpisodes(season, this);
                        getProgressEvent().updateProgress("");
                        currentSeasons.add(season);
                    } else {
                        final var currentSeason = optCurrentSeason.get();
                        final var episodes = DownloadDatabaseHelper.findEpisodes(currentSeason);
                        for (final Episode episode : episodes) {
                            cancelProcessIfInterruptionIsInProgress();

                            final var optCurrentEpisode = currentSeason.getEpisodes()
                                .stream().filter(episode1 -> episode1.getNumber().equals(episode.getNumber()))
                                .findFirst();
                            if (optCurrentEpisode.isEmpty()) {
                                foundNewEpisodes = true;
                                getProgressEvent().updateProgress("Found new episode %s in season %s".formatted(episode.getNumber(), season.getNumber()));
                                loadEpisodeParts(season, episode, this);
                                getProgressEvent().updateProgress("");
                                currentSeason.getEpisodes().add(episode);
                            }
                        }
                    }
                }
                if (!foundNewEpisodes) {
                    getProgressEvent().updateProgress("No new episodes");
                }
                getProgressEvent().updateProgress("Done !");

                DownloadDatabase.update(currentSeasons);

                callback.run();
            }
        };

        executeThread(dialog, thread);
    }

    private static void executeThread(final ProgressDialog dialog, final ThreadProcess thread) {
        final var progressItemPanel = new ProgressItemPanel();
        dialog.addProgressItem(thread, I18N.i18n("download_database.update"),
            progressItemPanel, new ProgressDialogEvent(progressItemPanel), false, false);
        dialog.setTitle(I18N.i18n("download_database.update"));
        dialog.setSize(600, 600);
    }
}
