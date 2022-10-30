package de.finnos.southparkdownloader.classes;

public record Resolution(String resolutionString, Long resolution) implements Comparable<Resolution> {

    @Override
    public int compareTo(final Resolution o) {
        return o.resolution.compareTo(this.resolution);
    }
}
