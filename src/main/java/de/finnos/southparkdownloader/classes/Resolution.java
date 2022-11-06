package de.finnos.southparkdownloader.classes;

import java.io.Serializable;

public record Resolution(String resolutionString, Long resolution) implements Comparable<Resolution>, Serializable {

    @Override
    public int compareTo(final Resolution o) {
        return o.resolution.compareTo(this.resolution);
    }
}
