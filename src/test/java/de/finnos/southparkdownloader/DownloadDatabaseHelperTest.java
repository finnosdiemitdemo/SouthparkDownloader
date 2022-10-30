package de.finnos.southparkdownloader;

import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.data.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DownloadDatabaseHelperTest {

    private static Episode FIRST_EPISODE = null;
    static {
        Config.init();
    }

    @Test
    void findSeasonsUrls() throws DownloadDatabaseHelper.DownloadHelperException {
        ArrayList<Season> seasonsList = DownloadDatabaseHelper.findSeasons();
        Assertions.assertFalse(seasonsList.isEmpty());
        findEpisodeUrls(seasonsList);
    }

    void findEpisodeUrls(final List<Season> seasonsList) throws DownloadDatabaseHelper.DownloadHelperException {
        if (!seasonsList.isEmpty()) {
            ArrayList<Episode> episodes = DownloadDatabaseHelper.findEpisodes(seasonsList.get(0));
            Assertions.assertFalse(episodes.isEmpty());
            findEpisodeInfoUrl(episodes.get(0));
        }
    }

    void findEpisodeInfoUrl(final Episode episode) throws DownloadDatabaseHelper.DownloadHelperException {
        final ArrayList<String> episodeInfoUrls = DownloadDatabaseHelper.findEpisodePartUrls(episode);
        Assertions.assertTrue(episodeInfoUrls.size() > 0);
        final String episodeInfoJsonContent = DownloadHelper.downloadFile(episodeInfoUrls.get(0));
        Assertions.assertFalse(episodeInfoJsonContent.isEmpty());
    }

    public static Episode findFirstEpisode () throws DownloadDatabaseHelper.DownloadHelperException {
        if (FIRST_EPISODE != null) {
            return FIRST_EPISODE;
        }
        FIRST_EPISODE = DownloadDatabaseHelper.findEpisodes(DownloadDatabaseHelper.findSeasons().get(0)).get(0);
        DownloadDatabaseHelper.findEpisodePartUrls(FIRST_EPISODE);
        return FIRST_EPISODE;
    }
}
