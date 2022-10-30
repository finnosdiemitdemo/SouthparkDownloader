package de.finnos.southparkdownloader.classes;

import de.finnos.southparkdownloader.DownloadDatabaseHelper;
import de.finnos.southparkdownloader.DownloadDatabaseHelperTest;
import de.finnos.southparkdownloader.DownloadHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EpisodePartTest {

    @Test
    public void loadPartMediaTest () throws DownloadDatabaseHelper.DownloadHelperException {
        final var episode = DownloadDatabaseHelperTest.findFirstEpisode();
        Assertions.assertNotNull(episode);
        Assertions.assertEquals(episode.getParts().size(), 4);
        episode.loadMedia();
        for (final EpisodePart part : episode.getParts()) {
            Assertions.assertFalse(part.getStreams().isEmpty());
        }
    }
}
