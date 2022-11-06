package de.finnos.southparkdownloader.classes;

import de.finnos.southparkdownloader.RegexHelper;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class EpisodePartStream implements Serializable {
    private final EpisodePart episodePart;

    private final Resolution resolution;
    private final String url;

    public EpisodePartStream(final EpisodePart episodePart, final String resolutionString, final String url) {
        this.episodePart = episodePart;
        this.url = url;

        final AtomicLong resolution = new AtomicLong(-1);
        RegexHelper.match("(\\d+)[xX](\\d+)", resolutionString, matcher -> {
            resolution.set(Long.parseLong(matcher.group(1)) * Long.parseLong(matcher.group(2)));
        });

        this.resolution = new Resolution(resolutionString, resolution.get());
    }

    public Resolution getResolution() {
        return resolution;
    }

    public String getUrl() {
        return url;
    }

    public EpisodePart getEpisodePart() {
        return episodePart;
    }
}
