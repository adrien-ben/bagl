package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.math.Vector3;

public class VertexPositionColor implements Vertex {

    public static VertexDescription DESCRIPTION = createVertexDescription();

    private final Vector3 position;
    private final Color color;

    public VertexPositionColor(Vector3 position, Color color) {
        this.position = position;
        this.color = color;
    }

    @Override
    public float[] getData() {
        return new float[]{position.getX(), position.getY(), position.getZ(), color.getRed(),
                color.getGreen(), color.getBlue()};
    }

    public Vector3 getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    private static VertexDescription createVertexDescription() {
        return new VertexDescription(new VertexElement[]{new VertexElement(0, 3, 0), new VertexElement(1, 3, 3)});
    }

}
