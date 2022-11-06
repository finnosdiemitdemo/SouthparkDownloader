package de.finnos.southparkdownloader.classes;

import com.google.gson.annotations.Expose;
import de.finnos.southparkdownloader.Helper;
import de.finnos.southparkdownloader.data.Config;
import de.finnos.southparkdownloader.ffmpeg.ffprobe.FfprobeHelper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

public class Episode implements Serializable {
    @Expose
    private final String id;

    @Expose
    private final String name;

    @Expose
    private final Integer number;

    @Expose
    private final String url;

    @Expose
    private final String description;

    @Expose
    private final String thumbnailUrl;

    @Expose
    private final Duration duration;

    @Expose
    private final LocalDate publishDate;

    @Expose
    private ArrayList<String> partsGeneratorUrls;

    private  String path;

    private Season season;
    private ArrayList<EpisodePart> parts;

    public Episode (final Season season,
                    final String id,
                    final String name,
                    final Integer number,
                    final String url,
                    final String description,
                    final String thumbnailUrl,
                    final Duration duration,
                    final LocalDate publishDate) {
        this.season = season;
        this.id = id;
        this.name = name;
        this.number = number;
        this.url = url;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.duration = duration;
        this.publishDate = publishDate;

        partsGeneratorUrls = new ArrayList<>();
        parts = new ArrayList<>();

        setPath();
    }

    public Episode(final Episode episode) {
        this.id = episode.id;
        this.name = episode.name;
        this.number = episode.number;
        this.url = episode.url;
        this.description = episode.description;
        this.thumbnailUrl = episode.thumbnailUrl;
        this.duration = episode.duration;
        this.publishDate = episode.publishDate;

        parts = new ArrayList<>();

        setPartsGeneratorUrls(episode.partsGeneratorUrls);
    }

    public String getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public void setPartsGeneratorUrls(final ArrayList<String> urls) {
        partsGeneratorUrls = urls;
        parts.clear();

        int count = 0;
        for (String url : partsGeneratorUrls) {
            parts.add(new EpisodePart(url, this, count));
            count++;
        }
    }

    public ArrayList<EpisodePart> getParts() {
        return parts;
    }

    public Season getSeason() {
        return season;
    }

    public String getPath() {
        return path;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void loadMedia() {
        for (final EpisodePart part : parts) {
            part.loadMedia();
        }
    }

    public boolean downloaded() {
        return parts != null && !parts.isEmpty() && parts.stream().filter(EpisodePart::downloaded).count() == parts.size();
    }

    public boolean combined() {
        return new File(getFinalPath()).exists() && !FfprobeHelper.isVideoCorrupt(getFinalPath());
    }

    public boolean delete() {
        if (downloaded()) {
            return Helper.deleteDirectory(new File(getPath()));
        }

        return false;
    }

    public boolean canDelete() {
        return folderExists();
    }

    public void setPath() {
        final var fileName = Config.DownloadLanguage.valueOfCode(Config.get().getDownloadLang()).getFolderNameEpisode() + " " + number;
        path = Path.of(season.getPath(), fileName).toString();
        parts.forEach(EpisodePart::setPath);
    }

    public String getFinalName() {
        return String.format("S%02dE%02d - %s.mp4", season.getNumber(), getNumber(), Helper.removeNotAllowedCharactersFromFileName(getName()));
    }

    public String getFinalPath() {
        return Path.of(getFinalPathDir(), getFinalName()).toString();
    }

    public String getFinalPathDir() {
        return getPath();
    }

    public boolean folderExists() {
        return new File(getPath()).exists();
    }

    public boolean canPlay() {
        return combined();
    }

    public void play() {
        try {
            Desktop.getDesktop().open(new File(getFinalPath()));
        } catch (IOException ex) {
            Helper.showErrorMessage(null, ex.getMessage());
        }
    }

    public boolean canOpenFolder() {
        return new File(getPath()).exists();
    }

    public void openFolder() {
        Helper.openFileWithSystemDefaultProgram(getPath());
    }
}
