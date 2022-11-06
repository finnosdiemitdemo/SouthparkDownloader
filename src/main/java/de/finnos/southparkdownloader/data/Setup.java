package de.finnos.southparkdownloader.data;

import de.finnos.southparkdownloader.Helper;

import java.io.File;
import java.nio.file.Path;

public class Setup {
    private static final String EPISODES_FOLDER_PATH;
    protected static final String DATABASE_FOLDER_PATH;

    static {
        final var homeFolder = System.getProperty("user.home");
        EPISODES_FOLDER_PATH = Path.of(homeFolder, "Videos", "SouthparkDownloader").toAbsolutePath().toString() + File.separatorChar;
        DATABASE_FOLDER_PATH = Path.of(homeFolder, "SouthparkDownloader", "database").toAbsolutePath().toString() + File.separatorChar;
    }

    public static boolean init() {
        boolean success = true;

        // data/episodes
        final File episodeFolder = new File(EPISODES_FOLDER_PATH);
        if (!episodeFolder.exists()) {
            success &= episodeFolder.mkdirs();
        }

        // data/database
        final File databaseFolder = new File(DATABASE_FOLDER_PATH);
        if (!databaseFolder.exists()) {
            success &= databaseFolder.mkdirs();
        }

        return success;
    }

    public static String getEpisodesFolderPath() {
        return Setup.EPISODES_FOLDER_PATH;
    }

    public static String getDatabaseFolderPath() {
        return Setup.DATABASE_FOLDER_PATH;
    }

    public static String getDownloadFolder() {
        final var downloadLanguage = Config.DownloadLanguage.valueOfCode(Config.get().getDownloadLang());
        return Setup.EPISODES_FOLDER_PATH + downloadLanguage.getName();
    }

    public static void deleteDownloadFolder() {
        Helper.deleteDirectory(new File(getDownloadFolder()));
    }

    public static boolean downloadFolderExists () {
        return new File(getDownloadFolder()).exists();
    }
}
