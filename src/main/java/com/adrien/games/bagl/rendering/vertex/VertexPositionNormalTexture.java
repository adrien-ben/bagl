package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;

public class VertexPositionNormalTexture implements Vertex {

    public static final VertexDescription DESCRIPTION = createVertexDescription();

    private final Vector3 position;
    private final Vector3 normal;
    private final Vector2 coords;

    public VertexPositionNormalTexture() {
        this.position = new Vector3();
        this.normal = new Vector3();
        this.coords = new Vector2();
    }

    public VertexPositionNormalTexture(Vector3 position, Vector3 normal, Vector2 coords) {
        this.position = position;
        this.normal = normal;
        this.coords = coords;
    }

    @Override
    public float[] getData() {
        return new float[]{
                position.getX(), position.getY(), position.getZ(),
                normal.getX(), normal.getY(), normal.getZ(),
                coords.getX(), coords.getY()};
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getNormal() {
        return normal;
    }

    public Vector2 getCoords() {
        return coords;
    }

    private static VertexDescription createVertexDescription() {
        return new VertexDescription(
                new VertexElement[]
                        {new VertexElement(0, 3, 0), new VertexElement(1, 3, 3), new VertexElement(2, 2, 6)});
    }
}
