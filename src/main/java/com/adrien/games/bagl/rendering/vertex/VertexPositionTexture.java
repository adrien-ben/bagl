package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;

public class VertexPositionTexture implements Vertex {

    public static final VertexDescription DESCRIPTION = createVertexDescription();

    private final Vector3 position;
    private final Vector2 coords;

    public VertexPositionTexture() {
        this.position = new Vector3();
        this.coords = new Vector2();
    }

    public VertexPositionTexture(Vector3 position, Vector2 coords) {
        this.position = position;
        this.coords = coords;
    }

    @Override
    public float[] getData() {
        return new float[]
                { position.getX(), position.getY(), position.getZ(), coords.getX(),
                        coords.getY() };
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector2 getCoords() {
        return coords;
    }

    private static VertexDescription createVertexDescription() {
        return new VertexDescription(
                new VertexElement[]
                        { new VertexElement(0, 3, 0), new VertexElement(2, 2, 3) });
    }
}
