package de.finnos.southparkdownloader.data;

import java.io.File;

public class Setup {

    public static final String DATA_FOLDER_PATH = "data" + File.separatorChar;
    public static final String EPISODES_FOLDER_PATH = DATA_FOLDER_PATH + "episodes" + File.separatorChar;
    public static final String DATABASE_FOLDER_PATH = DATA_FOLDER_PATH + "database" + File.separatorChar;

    public static boolean init() {
        boolean success = true;

        // data
        final File dataFolderFile = new File(DATA_FOLDER_PATH);
        if (!dataFolderFile.exists()) {
            success &= dataFolderFile.mkdir();
        }

        // data/episodes
        final File episodeFolder = new File(EPISODES_FOLDER_PATH);
        if (!episodeFolder.exists()) {
            success &= episodeFolder.mkdir();
        }

        // data/database
        final File databaseFolder = new File(DATABASE_FOLDER_PATH);
        if (!databaseFolder.exists()) {
            success &= databaseFolder.mkdir();
        }

        return success;
    }

}
