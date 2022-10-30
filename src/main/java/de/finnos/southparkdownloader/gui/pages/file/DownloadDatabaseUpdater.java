package de.finnos.southparkdownloader.gui.pages.file;

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

public class DownloadDatabaseUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadDatabaseUpdater.class);

    public static void update(final Runnable callback) {
        Setup.init();

        final var dialog = new ProgressDialog();
        ThreadProcess thread = new ThreadProcess(dialog) {
            @Override
            public void execute() {
                ArrayList<Season> seasons;

                try {
                    getProgressEvent().updateProgress("Load seasons...");
                    seasons = DownloadDatabaseHelper.findSeasons();
                    getProgressEvent().updateProgress(String.format("%d seasons found!", seasons.size()));

                    for (Season season : seasons) {
                        cancelProcessIfInterruptionIsInProgress();
                        getProgressEvent().updateProgress("Load Episodes for season " + season.getNumber() + "...");
                        DownloadDatabaseHelper.findEpisodes(season);
                        getProgressEvent().updateProgress(String.format("%d episodes found!", season.getEpisodes().size()));

                        getProgressEvent().initProgress(season.getEpisodes().size());

                        int episodeCount = 1;
                        for (Episode episode : season.getEpisodes()) {
                            cancelProcessIfInterruptionIsInProgress();
                            LOG.trace("DownloadDatabaseUpdater handle episode S%s E%s".formatted(season.getNumber(), episode.getNumber()));
                            getProgressEvent().updateProgress("Load part download urls for episode " + episode.getNumber(), episodeCount);
                            DownloadDatabaseHelper.findEpisodePartUrls(episode);

                            episodeCount++;
                        }

                        getProgressEvent().updateProgress("");
                    }

                    getProgressEvent().updateProgress("Done !");
                } catch (DownloadDatabaseHelper.DownloadHelperException e) {
                    getProgressEvent().initProgress(0);
                    getProgressEvent().updateProgress("Error: " + e.getMessage());
                    throw new RuntimeException(e);
                }

                DownloadDatabase.update(seasons);

                callback.run();
            }
        };

        final var progressItemPanel = new ProgressItemPanel();
        dialog.addProgressItem(thread, I18N.i18n("download_database.update"),
            progressItemPanel, new ProgressDialogEvent(progressItemPanel), false, false);
        dialog.setTitle(I18N.i18n("download_database.update"));
        dialog.setSize(500, 600);
        thread.start();
    }
}
