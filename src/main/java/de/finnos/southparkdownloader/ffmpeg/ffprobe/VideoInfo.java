package de.finnos.southparkdownloader.ffmpeg.ffprobe;

public class VideoInfo {
    private int expectedDurationInSeconds;
    private int actualDurationInSeconds;
    private int width;
    private int height;

    public int getExpectedDurationInSeconds() {
        return expectedDurationInSeconds;
    }

    public void setExpectedDurationInSeconds(int expectedDurationInSeconds) {
        this.expectedDurationInSeconds = expectedDurationInSeconds;
    }

    public int getActualDurationInSeconds() {
        return actualDurationInSeconds;
    }

    public void setActualDurationInSeconds(int actualDurationInSeconds) {
        this.actualDurationInSeconds = actualDurationInSeconds;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
