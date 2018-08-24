package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.core.math.AABBs;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.core.validation.Validation;
import com.adrienben.games.bagl.opengl.PrimitiveType;
import com.adrienben.games.bagl.opengl.vertex.IndexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexArray;
import com.adrienben.games.bagl.opengl.vertex.VertexBuffer;
import org.joml.AABBf;

import java.util.*;

/**
 * Mesh data.
 * <p>
 * Mesh are created using the {@link Mesh.Builder} class. You can get a
 * builder instance by calling {@link Mesh#builder()}.
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
    private final int vertexCount;
    private final IndexBuffer iBuffer;
    private final PrimitiveType primitiveType;
    private final AABBf aabb;

    private Mesh(final Builder builder) {
        this.vBuffers = new ArrayList<>(Validation.validate(builder.vertexBuffers, CollectionUtils::isNotEmpty, "Mesh requires at least one vertex buffer"));
        this.vArray = generateVertexArray();
        this.vertexCount = this.vBuffers.get(0).getVertexCount();
        this.iBuffer = builder.indexBuffer;
        this.primitiveType = builder.primitiveType;
        this.aabb = builder.aabb;
    }

    private VertexArray generateVertexArray() {
        final var vArray = new VertexArray();
        vArray.bind();
        this.vBuffers.forEach(vArray::attachVertexBuffer);
        vArray.unbind();
        return vArray;
    }

    /**
     * Create a new mesh builder.
     */
    public static Builder builder() {
        return new Builder();
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

    public AABBf getAabb() {
        return aabb;
    }

    /**
     * Mesh builder.
     * <p>
     * You can set the following parameters :
     * <li>vertexBuffers (at least one required)</li>
     * <li>indexBuffer : default = null</li>
     * <li>primitiveType (required) : default = {@link PrimitiveType#TRIANGLES}</li>
     * <li>aabb (required) : default = {@link AABBs#createZero()}</li>
     */
    public static class Builder {

        private List<VertexBuffer> vertexBuffers = new ArrayList<>();
        private IndexBuffer indexBuffer = null;
        private PrimitiveType primitiveType = PrimitiveType.TRIANGLES;
        private AABBf aabb = AABBs.createZero();

        private Builder() {
        }

        public Mesh build() {
            return new Mesh(this);
        }

        public Builder vertexBuffer(final VertexBuffer vertexBuffer) {
            this.vertexBuffers.add(Objects.requireNonNull(vertexBuffer));
            return this;
        }

        public Builder vertexBuffers(final Collection<VertexBuffer> vertexBuffers) {
            this.vertexBuffers.addAll(Objects.requireNonNull(vertexBuffers));
            return this;
        }

        public Builder indexBuffer(final IndexBuffer indexBuffer) {
            this.indexBuffer = indexBuffer;
            return this;
        }

        public Builder primitiveType(final PrimitiveType primitiveType) {
            this.primitiveType = Objects.requireNonNull(primitiveType);
            return this;
        }

        public Builder aabb(final AABBf aabb) {
            this.aabb = Objects.requireNonNull(aabb);
            return this;
        }
    }
}
