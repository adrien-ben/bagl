package com.adrien.games.bagl.rendering;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.Objects;

/**
 * Mesh data
 *
 * @author adrien
 */
public class Mesh {

    private static final int FLOAT_SIZE_IN_BYTES = Float.SIZE / 8;
    public static final int ELEMENTS_PER_VERTEX = 11;
    public static final int VERTEX_STRIDE = ELEMENTS_PER_VERTEX * FLOAT_SIZE_IN_BYTES;
    public static final int POSITION_INDEX = 0;
    public static final int POSITION_OFFSET = 0;
    public static final int ELEMENTS_PER_POSITION = 3;
    public static final int NORMAL_INDEX = 1;
    public static final int NORMAL_OFFSET = 3 * FLOAT_SIZE_IN_BYTES;
    public static final int ELEMENTS_PER_NORMAL = 3;
    public static final int COORDINATES_INDEX = 2;
    public static final int COORDINATES_OFFSET = 6 * FLOAT_SIZE_IN_BYTES;
    public static final int ELEMENTS_PER_COORDINATES = 2;
    public static final int TANGENT_INDEX = 3;
    public static final int TANGENT_OFFSET = 8 * FLOAT_SIZE_IN_BYTES;
    public static final int ELEMENTS_PER_TANGENT = 3;

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;
    private final int iboId;
    private final int indexCount;
    private final Material material;

    /**
     * Construct a mesh
     *
     * @param vaoId       The id od the vertex array object
     * @param vboId       The id of the vertex buffer object
     * @param vertexCount The number of vertices
     * @param iboId       The if of the index buffer
     * @param indexCount  The number of indices
     * @param material    The material of the mesh
     */
    public Mesh(final int vaoId, final int vboId, final int vertexCount, final int iboId, final int indexCount, final Material material) {
        this.vaoId = vaoId;
        this.vboId = vboId;
        this.vertexCount = vertexCount;
        this.iboId = iboId;
        this.indexCount = indexCount;
        this.material = material;
    }

    /**
     * Release resources
     */
    public void destroy() {
        if (this.vaoId >= 0) {
            GL30.glDeleteVertexArrays(this.vaoId);
        }
        if (this.vboId >= 0) {
            GL15.glDeleteBuffers(this.vboId);
        }
        if (this.iboId >= 0) {
            GL15.glDeleteBuffers(this.iboId);
        }
        if (Objects.nonNull(material)) {
            material.destroy();
        }
    }

    public int getVaoId() {
        return this.vaoId;
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
