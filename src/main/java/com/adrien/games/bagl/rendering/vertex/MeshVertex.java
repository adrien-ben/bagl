package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;

public class MeshVertex implements Vertex {

    public static final VertexDescription DESCRIPTION = createVertexDescription();

    private final Vector3 position;
    private final Vector3 normal;
    private final Vector2 coords;
    private Vector3 tangent;

    public MeshVertex() {
        this.position = new Vector3();
        this.normal = new Vector3();
        this.coords = new Vector2();
    }

    public MeshVertex(Vector3 position, Vector3 normal, Vector2 coords, Vector3 tangent) {
        this.position = position;
        this.normal = normal;
        this.coords = coords;
        this.tangent = tangent;
    }

    @Override
    public float[] getData() {
        return new float[]{
                this.position.getX(), this.position.getY(), this.position.getZ(),
                this.normal.getX(), this.normal.getY(), this.normal.getZ(),
                this.coords.getX(), this.coords.getY(),
                this.tangent.getX(), this.tangent.getY(), this.tangent.getZ()};
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

    public Vector3 getTangent() {
        return tangent;
    }

    private static VertexDescription createVertexDescription() {
        return new VertexDescription(
                new VertexElement[]
                        {new VertexElement(0, 3, 0), new VertexElement(1, 3, 3), new VertexElement(2, 2, 6),
                                new VertexElement(3, 3, 8)});
    }

}
