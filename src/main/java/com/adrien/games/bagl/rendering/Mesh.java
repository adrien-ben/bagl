package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.rendering.vertex.IndexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexArray;
import com.adrien.games.bagl.rendering.vertex.VertexBuffer;

import java.util.Objects;

/**
 * Mesh data
 *
 * @author adrien
 */
public class Mesh {

    public static final int ELEMENTS_PER_VERTEX = 11;
    public static final int POSITION_INDEX = 0;
    public static final int ELEMENTS_PER_POSITION = 3;
    public static final int NORMAL_INDEX = 1;
    public static final int ELEMENTS_PER_NORMAL = 3;
    public static final int COORDINATES_INDEX = 2;
    public static final int ELEMENTS_PER_COORDINATES = 2;
    public static final int TANGENT_INDEX = 3;
    public static final int ELEMENTS_PER_TANGENT = 3;

    private final VertexBuffer vBuffer;
    private final VertexArray vArray;
    private final IndexBuffer iBuffer;
    private final Material material;

    /**
     * Construct a mesh
     *
     * @param vBuffer  The vertex buffer
     * @param vArray   The vertex array
     * @param iBuffer  The index buffer
     * @param material The material of the mesh
     */
    public Mesh(final VertexBuffer vBuffer, final VertexArray vArray, final IndexBuffer iBuffer, final Material material) {
        this.vBuffer = vBuffer;
        this.vArray = vArray;
        this.iBuffer = iBuffer;
        this.material = material;
    }

    /**
     * Release resources
     */
    public void destroy() {
        if (Objects.nonNull(this.vBuffer)) {
            this.vBuffer.destroy();
        }
        if (Objects.nonNull(this.vArray)) {
            this.vArray.destroy();
        }
        if (Objects.nonNull(this.iBuffer)) {
            this.iBuffer.destroy();
        }
        if (Objects.nonNull(material)) {
            this.material.destroy();
        }
    }

    public VertexArray getVertexArray() {
        return this.vArray;
    }

    public IndexBuffer getIndexBuffer() {
        return this.iBuffer;
    }

    public Material getMaterial() {
        return this.material;
    }
}
