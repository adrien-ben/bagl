package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.rendering.PrimitiveType;
import com.adrien.games.bagl.rendering.vertex.IndexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexArray;
import com.adrien.games.bagl.rendering.vertex.VertexBuffer;

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

    private final VertexBuffer vBuffer;
    private final VertexArray vArray;
    private final IndexBuffer iBuffer;
    private final PrimitiveType primitiveType;

    /**
     * Construct a mesh
     *
     * @param vBuffer The vertex buffer
     * @param vArray  The vertex array
     * @param iBuffer The index buffer
     */
    public Mesh(final VertexBuffer vBuffer, final VertexArray vArray, final IndexBuffer iBuffer) {
        this.vBuffer = vBuffer;
        this.vArray = vArray;
        this.iBuffer = iBuffer;
        this.primitiveType = PrimitiveType.TRIANGLES;
    }

    /**
     * Construct a mesh
     *
     * @param vBuffer       The vertex buffer
     * @param vArray        The vertex array
     * @param primitiveType The type of primitives to use when rendering
     */
    public Mesh(final VertexBuffer vBuffer, final VertexArray vArray, final PrimitiveType primitiveType) {
        this.vBuffer = vBuffer;
        this.vArray = vArray;
        this.iBuffer = null;
        this.primitiveType = primitiveType;
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.vBuffer.destroy();
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
        return this.vBuffer.getVertexCount();
    }
}
