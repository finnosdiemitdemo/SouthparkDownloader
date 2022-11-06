package de.finnos.southparkdownloader.gui.downloadepisode.single;

import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.EpisodePart;
import de.finnos.southparkdownloader.classes.EpisodePartStream;
import de.finnos.southparkdownloader.services.ProgressItem;

import java.io.File;

public class DownloadItem implements ProgressItem {
    private final Episode episode;
    private final EpisodePart episodePart;

    private EpisodePartStream stream;

    private final String outputDirSeason;
    private final String outputDirEpisode;

    public DownloadItem(final Episode episode, final EpisodePart episodePart, final EpisodePartStream stream) {
        this.episode = episode;
        this.episodePart = episodePart;
        this.stream = stream;

        outputDirSeason = episode.getSeason().getPath();
        outputDirEpisode = episode.getPath();
    }

    public DownloadItem(final Episode episode, final EpisodePart episodePart) {
        this(episode, episodePart, null);
    }

    public boolean prepareForDownload() {
        boolean success = episode.getSeason() != null;

        // Create Folder for the season
        final File destSeasonFile = new File(outputDirSeason);
        if (!destSeasonFile.exists()) {
            success = destSeasonFile.mkdirs();
            if (!success) {
                Helper.showErrorMessage(null, I18N.i18n("unable_to_create_folder").formatted(outputDirSeason));
            }
        }

        // Create a folder for the episode
        if (success) {
            final File destEpisodeFile = new File(outputDirEpisode);
            if (!destEpisodeFile.exists()) {
                success = destEpisodeFile.mkdirs();
                if (!success) {
                    Helper.showErrorMessage(null, I18N.i18n("unable_to_create_folder").formatted(outputDirEpisode));
                }
            }
        }

        return success;
    }

    public EpisodePartStream getStream() {
        return stream;
    }

    public void setStream(EpisodePartStream stream) {
        this.stream = stream;
    }

    public String getOutputDirEpisode() {
        return outputDirEpisode;
    }

    public EpisodePart getEpisodePart() {
        return episodePart;
    }

    @Override
    public String getName() {
        return "%s%s %s%s %s%s".formatted(
            I18N.i18n("season.short"),
            getEpisodePart().getEpisode().getSeason().getNumber(),
            I18N.i18n("episode.short"),
            getEpisodePart().getEpisode().getNumber(),
            I18N.i18n("part.short"),
            getEpisodePart().getIndex());
    }
}
