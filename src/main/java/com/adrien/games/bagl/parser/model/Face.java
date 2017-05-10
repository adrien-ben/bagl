package com.adrien.games.bagl.parser.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Face {

    private final int positionIndex;
    private final int normalIndex;
    private final int coordsIndex;

    public Face(int positionIndex, int normalIndex, int coordsIndex) {
        this.positionIndex = positionIndex;
        this.normalIndex = normalIndex;
        this.coordsIndex = coordsIndex;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.positionIndex)
                .append(this.normalIndex)
                .append(this.coordsIndex)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Face)) {
            return false;
        }
        Face other = (Face) obj;
        return new EqualsBuilder()
                .append(this.positionIndex, other.positionIndex)
                .append(this.normalIndex, other.normalIndex)
                .append(this.coordsIndex, other.coordsIndex)
                .isEquals();
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public int getNormalIndex() {
        return normalIndex;
    }

    public int getCoordsIndex() {
        return coordsIndex;
    }

}
