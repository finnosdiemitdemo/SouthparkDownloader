package de.finnos.southparkdownloader.classes;

import com.google.gson.annotations.Expose;
import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.data.Setup;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class Season {
    @Expose
    private final String name;

    @Expose
    private final Integer number;

    @Expose
    private final String url;

    @Expose
    private ArrayList<Episode> episodes = new ArrayList<>();

    private String path;

    public Season(String name, Integer number, String url) {
        this.name = name;
        this.number = number;
        this.url = url;

        setPath();
    }

    public String getName() {
        return name;
    }

    public Integer getNumber() {
        return number;
    }

    public String getUrl() {
        return url;
    }

    public ArrayList<Episode> getEpisodes() {
        return episodes;
    }

    public String getPath() {
        return path;
    }

    public boolean downloaded() {
        return getEpisodes().stream().filter(Episode::downloaded).count() == getEpisodes().size();
    }

    public boolean delete() {
        if (downloaded()) {
            return Helper.deleteDirectory(new File(getPath()));
        }

        return false;
    }

    public void openFolder() {
        Helper.openFileWithSystemDefaultProgram(getPath());
    }

    public boolean canOpenFolder() {
        return new File(getPath()).exists();
    }

    public void setPath() {
        final var downloadLanguage = Config.DownloadLanguage.valueOfCode(Config.get().getDownloadLang());
        final var episodesFolderPath = Setup.EPISODES_FOLDER_PATH + downloadLanguage.getName();
        final var folderName = downloadLanguage.getFolderNameSeason() + " " + number;
        this.path = Path.of(episodesFolderPath, folderName).toString();

        episodes.forEach(Episode::setPath);
    }
}
