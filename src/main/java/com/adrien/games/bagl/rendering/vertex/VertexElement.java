package com.adrien.games.bagl.rendering.vertex;

public final class VertexElement {

    private final int location;
    private final int size;
    private final int offset;

    public VertexElement(int location, int size, int offset) {
        this.location = location;
        this.size = size;
        this.offset = offset;
    }

    public int getLocation() {
        return location;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

}