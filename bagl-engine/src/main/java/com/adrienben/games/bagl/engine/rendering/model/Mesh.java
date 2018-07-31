package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.opengl.PrimitiveType;
import com.adrienben.games.bagl.opengl.vertex.IndexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexArray;
import com.adrienben.games.bagl.opengl.vertex.VertexBuffer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    private final List<VertexBuffer> vBuffers;
    private final VertexArray vArray;
    private final IndexBuffer iBuffer;
    private final PrimitiveType primitiveType;
    private final int vertexCount;

    /**
     * Construct a mesh
     *
     * @param vBuffer The vertex buffer
     * @param iBuffer The index buffer
     */
    public Mesh(final VertexBuffer vBuffer, final IndexBuffer iBuffer) {
        this.vBuffers = Collections.singletonList(vBuffer);
        this.iBuffer = iBuffer;
        this.vArray = this.generateVertexArray();
        this.primitiveType = PrimitiveType.TRIANGLES;
        this.vertexCount = vBuffer.getVertexCount();
    }

    /**
     * Construct a mesh
     *
     * @param vBuffer       The vertex buffer
     * @param primitiveType The type of primitives to use when rendering
     */
    public Mesh(final VertexBuffer vBuffer, final PrimitiveType primitiveType) {
        this.vBuffers = Collections.singletonList(vBuffer);
        this.iBuffer = null;
        this.vArray = this.generateVertexArray();
        this.primitiveType = primitiveType;
        this.vertexCount = vBuffer.getVertexCount();
    }

    /**
     * Construct a mesh
     *
     * @param vBuffers      The vertex buffers
     * @param iBuffer       The index buffer
     * @param primitiveType The type of primitives to use when rendering
     */
    public Mesh(final List<VertexBuffer> vBuffers, final IndexBuffer iBuffer, final PrimitiveType primitiveType) {
        this.vBuffers = vBuffers;
        this.iBuffer = iBuffer;
        this.vArray = this.generateVertexArray();
        this.primitiveType = primitiveType;
        this.vertexCount = this.vBuffers.get(0).getVertexCount();
    }

    /**
     * Generate a vertex array from the vertex buffers of the mesh
     *
     * @return The generate vertex array
     */
    private VertexArray generateVertexArray() {
        final var vArray = new VertexArray();
        vArray.bind();
        this.vBuffers.forEach(vArray::attachVertexBuffer);
        vArray.unbind();
        return vArray;
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.vBuffers.forEach(VertexBuffer::destroy);
        this.vArray.destroy();
        if (Objects.nonNull(this.iBuffer)) {
            this.iBuffer.destroy();
        }
    }

    public VertexArray getVertexArray() {
        return this.vArray;
    }

    public Optional<IndexBuffer> getIndexBuffer() {
        return Optional.ofNullable(this.iBuffer);
    }

    public PrimitiveType getPrimitiveType() {
        return this.primitiveType;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }
}
