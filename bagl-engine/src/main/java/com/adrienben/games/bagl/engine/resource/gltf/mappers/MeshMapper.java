package com.adrienben.games.bagl.engine.resource.gltf.mappers;

import com.adrienben.games.bagl.core.utils.Tuple2;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.opengl.buffer.BufferUsage;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.opengl.vertex.IndexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBufferParams;
import com.adrienben.games.bagl.opengl.vertex.VertexElement;
import com.adrienben.tools.gltf.models.GltfAccessor;
import com.adrienben.tools.gltf.models.GltfMesh;
import com.adrienben.tools.gltf.models.GltfPrimitive;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Map {@link GltfMesh} into maps of {@link Mesh} and {@link Material} couples.
 *
 * @author adrien.
 */
public class MeshMapper {

    private final ChannelMapper channelMapper = new ChannelMapper();
    private final VertexDataTypeMapper vertexDataTypeMapper = new VertexDataTypeMapper();
    private final IndexDataTypeMapper indexDataTypeMapper = new IndexDataTypeMapper();
    private final PrimitiveTypeMapper primitiveTypeMapper = new PrimitiveTypeMapper();
    private final MaterialMapper materialMapper = new MaterialMapper();

    private List<Texture2D> textureIndex;

    /**
     * Map a gltf mesh into a map mapping meshes to their material
     *
     * @param mesh The mesh to map
     * @return A map of meshes mapped to their material
     */
    public Map<Mesh, Material> map(final GltfMesh mesh, final List<Texture2D> textureIndex) {
        this.textureIndex = textureIndex;
        return mesh.getPrimitives().stream()
                .map(this::createMesh)
                .collect(Collectors.toMap(Tuple2::getFirst, Tuple2::getSecond));
    }

    /**
     * Create a mesh from a gltf primitive
     *
     * @param primitive The primitive from which to create the mesh
     * @return A new mesh
     */
    private Tuple2<Mesh, Material> createMesh(final GltfPrimitive primitive) {
        final var iBuffer = createIndexBuffer(primitive.getIndices());
        final var vBuffers = primitive.getAttributes().entrySet().stream()
                .map(entry -> createVertexBuffer(entry.getKey(), entry.getValue()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        final var primitiveType = primitiveTypeMapper.map(primitive.getMode());

        final Mesh mesh = Mesh.builder().vertexBuffers(vBuffers).indexBuffer(iBuffer).primitiveType(primitiveType).build();
        final Material material = materialMapper.map(primitive.getMaterial(), textureIndex);
        return new Tuple2<>(mesh, material);
    }

    /**
     * Create a index buffer from a primitive indices accessor
     *
     * @param accessor The accessor pointing to the index data
     * @return A new index buffer
     */
    private IndexBuffer createIndexBuffer(final GltfAccessor accessor) {
        final var indices = extractIndices(accessor);
        final var dataType = indexDataTypeMapper.map(accessor.getComponentType());
        final var indexBuffer = new IndexBuffer(indices, dataType, BufferUsage.STATIC_DRAW);
        MemoryUtil.memFree(indices);
        return indexBuffer;
    }

    private ByteBuffer extractIndices(final GltfAccessor accessor) {
        checkAccessorSupport(accessor);
        final var bufferView = accessor.getBufferView();
        final int offset = bufferView.getByteOffset() + accessor.getByteOffset();
        final int byteSize = accessor.getCount() * accessor.getComponentType().getByteSize() * accessor.getType().getComponentCount();
        return MemoryUtil.memAlloc(byteSize).put(bufferView.getBuffer().getData(), offset, byteSize).flip();
    }

    private void checkAccessorSupport(final GltfAccessor accessor) {
        if (Objects.nonNull(accessor.getSparse())) {
            throw new UnsupportedOperationException("Sparse buffer not supported");
        }
        if (Objects.isNull(accessor.getBufferView())) {
            throw new UnsupportedOperationException("Accessor has no buffer view");
        }
    }

    /**
     * Create a vertex buffer from a primitive attribute
     *
     * @param type     The type of attribute
     * @param accessor The accessor
     * @return A new vertex buffer
     */
    private Optional<VertexBuffer> createVertexBuffer(final String type, final GltfAccessor accessor) {
        final var channel = channelMapper.map(type);
        if (channel == -1) {
            return Optional.empty();
        }

        final var vertices = extractVertexData(accessor).orElseThrow(() -> new IllegalArgumentException(
                "Primitive attribute's accessor should not be null"));
        final var vertexBufferParams = mapVertexBufferParams(channel, accessor);
        final var vertexBuffer = new VertexBuffer(vertices, vertexBufferParams);
        MemoryUtil.memFree(vertices);
        return Optional.of(vertexBuffer);
    }

    /**
     * Extract the data from an accessor
     * <p>
     * Sparse accessor are not supported
     *
     * @param accessor The accessor to extract the data from
     * @return A new byte buffer
     */
    private Optional<ByteBuffer> extractVertexData(final GltfAccessor accessor) {
        checkAccessorSupport(accessor);
        final var bufferView = accessor.getBufferView();
        final var data = bufferView.getBuffer().getData();
        final var byteOffset = accessor.getByteOffset() + bufferView.getByteOffset();
        final var count = accessor.getCount();
        final var componentByteSize = accessor.getComponentType().getByteSize();
        final var componentCount = accessor.getType().getComponentCount();
        final var elementByteSize = componentByteSize * componentCount;
        final var byteStride = Optional.ofNullable(bufferView.getByteStride()).orElse(elementByteSize);

        final var extracted = MemoryUtil.memAlloc(count * elementByteSize);
        for (var index = 0; index < count; index++) {
            final var elementOffset = byteOffset + index * byteStride;
            for (var componentIndex = 0; componentIndex < componentCount; componentIndex++) {
                final var componentOffset = elementOffset + componentIndex * componentByteSize;
                extracted.put(data, componentOffset, componentByteSize);
            }
        }
        return Optional.of(extracted.flip());
    }

    private VertexBufferParams mapVertexBufferParams(final int channel, final GltfAccessor accessor) {
        return VertexBufferParams.builder()
                .dataType(vertexDataTypeMapper.map(accessor.getComponentType()))
                .element(new VertexElement(channel, accessor.getType().getComponentCount(), accessor.getNormalized()))
                .build();
    }
}
