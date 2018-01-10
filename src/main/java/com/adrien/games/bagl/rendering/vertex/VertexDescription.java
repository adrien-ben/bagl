package com.adrien.games.bagl.rendering.vertex;

import java.util.ArrayList;

public final class VertexDescription {

    private final ArrayList<VertexElement> vertexElements;
    private int vertexElementCount;
    private int stride;

    public VertexDescription() {
        this.vertexElements = new ArrayList<>();
        this.vertexElementCount = 0;
        this.stride = 0;
    }

    public VertexDescription(VertexElement[] vertexElements) {
        this.vertexElements = new ArrayList<>();
        this.vertexElementCount = 0;
        this.stride = 0;

        for (VertexElement element : vertexElements) {
            this.vertexElements.add(element);
            this.vertexElementCount++;
            this.stride += element.getSize();
        }
    }

    public void addVertexElement(VertexElement vertexElement) {
        vertexElements.add(vertexElement);
        vertexElementCount++;
        stride += vertexElement.getSize();
    }

    public ArrayList<VertexElement> getVertexElements() {
        return vertexElements;
    }

    public int getVertexElementCount() {
        return vertexElementCount;
    }

    public int getStride() {
        return stride;
    }


}
