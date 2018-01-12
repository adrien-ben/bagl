package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.rendering.vertex.VertexArray;
import com.adrien.games.bagl.rendering.vertex.VertexBuffer;
import org.lwjgl.opengl.GL15;

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
    private final int iboId;
    private final int indexCount;
    private final Material material;

    /**
     * Construct a mesh
     *
     * @param vBuffer    The vertex buffer
     * @param vArray     The vertex array
     * @param iboId      The if of the index buffer
     * @param indexCount The number of indices
     * @param material   The material of the mesh
     */
    public Mesh(final VertexBuffer vBuffer, final VertexArray vArray, final int iboId, final int indexCount, final Material material) {
        this.vBuffer = vBuffer;
        this.vArray = vArray;
        this.iboId = iboId;
        this.indexCount = indexCount;
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
        if (this.iboId >= 0) {
            GL15.glDeleteBuffers(this.iboId);
        }
        if (Objects.nonNull(material)) {
            material.destroy();
        }
    }

    public VertexArray getVertexArray() {
        return this.vArray;
    }

    public int getIboId() {
        return this.iboId;
    }

    public int getIndexCount() {
        return this.indexCount;
    }

    public Material getMaterial() {
        return this.material;
    }
}
