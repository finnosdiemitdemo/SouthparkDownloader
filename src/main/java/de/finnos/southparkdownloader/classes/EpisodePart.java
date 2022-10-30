package de.finnos.southparkdownloader.classes;

import de.finnos.southparkdownloader.ffmpeg.ffprobe.FfprobeHelper;
import de.finnos.southparkdownloader.services.download.SouthparkDownloadHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class EpisodePart {
    private final Episode episode;
    private final int index;
    private String outputPath;
    private String inputPath;

    private final String partGeneratorUrl;
    private String partStreamsUrl;

    private ArrayList<EpisodePartStream> streams;

    public EpisodePart(final String partGeneratorUrl, final Episode episode, final int index) {
        this.episode = episode;
        this.index = index;
        this.partGeneratorUrl = partGeneratorUrl;
    }

    public void loadMedia () {
        if (partStreamsUrl == null) {
            this.partStreamsUrl = SouthparkDownloadHelper.getPartUrl(partGeneratorUrl);

            streams = new ArrayList<>();
            final Map<String, String> mapResolutionToUrl = SouthparkDownloadHelper.getPartStreams(partStreamsUrl);
            mapResolutionToUrl.forEach((resolution, url) -> streams.add(new EpisodePartStream(this, resolution, url)));
        }
    }

    public void setPath () {
        outputPath = Path.of(episode.getPath(), String.format("part_%d.mp4", index)).toString();
        inputPath = Path.of(episode.getPath(), String.format("part_%d.m3u8", index)).toString();
    }

    public ArrayList<EpisodePartStream> getStreams() {
        return streams;
    }

    public Episode getEpisode() {
        return episode;
    }

    public int getIndex() {
        return index;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getPartStreamsUrl() {
        return partStreamsUrl;
    }

    public boolean downloaded () {
        return new File(outputPath).exists() && !FfprobeHelper.isVideoCorrupt(outputPath);
    }

    public boolean delete () {
        final File input = new File(inputPath);
        final File output = new File(outputPath);

        boolean success = true;
        success &= input.delete();
        success &= output.delete();

        return success;
    }
}
