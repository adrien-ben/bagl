package com.adrien.games.bagl.resource;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.BufferUsage;
import com.adrien.games.bagl.rendering.DataType;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.PrimitiveType;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.texture.Filter;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.Wrap;
import com.adrien.games.bagl.rendering.vertex.IndexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexBuffer;
import com.adrien.games.bagl.rendering.vertex.VertexBufferParams;
import com.adrien.games.bagl.rendering.vertex.VertexElement;
import com.adrien.games.bagl.utils.Tuple2;
import com.adrien.tools.gltf.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gltf file loader
 *
 * @author adrien
 */
public class GltfLoader {

    private String directory;

    /**
     * Load a gltf file
     *
     * @param path The path of the file
     * @return The loaded model
     */
    public Model load(final String path) {

        this.directory = Paths.get(path).getParent().toString();


        final GltfAsset gltfAsset = GltfAsset.Factory.fromGltfFile(path);
        if (Objects.isNull(gltfAsset)) {
            throw new IllegalStateException("Failed to load gltf " + path);
        }

        final Model model = new Model();

        gltfAsset.getMeshes()
                .stream()
                .map(com.adrien.tools.gltf.Mesh::getPrimitives)
                .flatMap(List::stream)
                .map(this::createMesh)
                .forEach(tuple -> model.addMesh(tuple.getFirst(), tuple.getSecond()));

        return model;
    }

    /**
     * Create a mesh from a gltf primitive
     *
     * @param primitive The primitive from which to create the mesh
     * @return A new mesh
     */
    private Tuple2<Mesh, Material> createMesh(final Primitive primitive) {
        final IndexBuffer iBuffer = this.createIndexBuffer(primitive.getIndices());

        final List<VertexBuffer> vBuffers = primitive.getAttributes()
                .entrySet()
                .stream()
                .map(entry -> this.createVertexBuffer(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());


        final Mesh mesh = new Mesh(vBuffers, iBuffer, this.mapPrimitiveType(primitive.getMode()));
        final Material material = this.mapMaterial(primitive.getMaterial());

        return new Tuple2<>(mesh, material);
    }

    /**
     * Create a index buffer from a primitive indices accessor
     *
     * @param accessor The accessor pointing to the index data
     * @return A new index buffer
     */
    private IndexBuffer createIndexBuffer(final Accessor accessor) {
        final Optional<ByteBuffer> byteBuffer = this.extractData(accessor);
        if (!byteBuffer.isPresent()) {
            return null;
        }

        final ByteBuffer indices = byteBuffer.get();
        final DataType dataType = this.mapDataType(accessor.getComponentType());
        final IndexBuffer indexBuffer = new IndexBuffer(indices, dataType, BufferUsage.STATIC_DRAW);
        MemoryUtil.memFree(indices);
        return indexBuffer;
    }

    /**
     * Create a vertex buffer from a primitive attribute
     *
     * @param type     The type of attribute
     * @param accessor The accessor
     * @return A new vertex buffer
     */
    private Optional<VertexBuffer> createVertexBuffer(final String type, final Accessor accessor) {
        final ByteBuffer vertices = this.extractData(accessor).orElseThrow(() -> new IllegalArgumentException(
                "Primitive attribute's accessor should not be null"));

        final int channel = this.mapChannel(type);
        if (channel == -1) {
            return Optional.empty();
        }

        final VertexBufferParams.Builder builder = VertexBufferParams.builder()
                .dataType(this.mapDataType(accessor.getComponentType()))
                .element(new VertexElement(channel, accessor.getType().getComponentCount(), accessor.getNormalized()));

        final VertexBuffer vertexBuffer = new VertexBuffer(vertices, builder.build());
        MemoryUtil.memFree(vertices);
        return Optional.of(vertexBuffer);
    }

    /**
     * Retrieve the vertex attribute channel from the type of attribute
     * <p>
     * If the type of attribute iss not supported it returns -1
     *
     * @param type The type of a primitive attribute
     * @return The channel on which to map the vertex elements
     */
    private int mapChannel(final String type) {
        switch (type) {
            case "POSITION":
                return Mesh.POSITION_INDEX;
            case "NORMAL":
                return Mesh.NORMAL_INDEX;
            case "TANGENT":
                return Mesh.TANGENT_INDEX;
            case "TEXCOORD_0":
                return Mesh.COORDINATES_INDEX;
            default:
                return -1;
        }
    }

    /**
     * Map a gltf component type to a data type
     *
     * @param componentType The component type to map
     * @return The corresponding data type
     */
    private DataType mapDataType(final ComponentType componentType) {
        switch (componentType) {
            case BYTE:
                return DataType.BYTE;
            case UNSIGNED_BYTE:
                return DataType.UNSIGNED_BYTE;
            case SHORT:
                return DataType.SHORT;
            case UNSIGNED_SHORT:
                return DataType.UNSIGNED_SHORT;
            case UNSIGNED_INT:
                return DataType.UNSIGNED_INT;
            case FLOAT:
                return DataType.FLOAT;
            default:
                throw new UnsupportedOperationException("Unsupported component type " + componentType);
        }
    }

    /**
     * Extract the data from an accessor
     * <p>
     * Sparse accessor are not supported
     *
     * @param accessor The accessor to extract the data from
     * @return A new byte buffer
     */
    private Optional<ByteBuffer> extractData(final Accessor accessor) {
        if (Objects.isNull(accessor)) {
            return Optional.empty();
        }
        if (Objects.nonNull(accessor.getSparse())) {
            throw new UnsupportedOperationException("Sparse buffer not supported");
        }
        if (Objects.isNull(accessor.getBufferView())) {
            throw new UnsupportedOperationException("Accessor has no buffer view");
        }

        final BufferView bufferView = accessor.getBufferView();
        final byte[] data = bufferView.getBuffer().getData();
        final int byteOffset = accessor.getByteOffset() + bufferView.getByteOffset();
        final int count = accessor.getCount();
        final int componentByteSize = accessor.getComponentType().getByteSize();
        final int componentCount = accessor.getType().getComponentCount();
        final int elementByteSize = componentByteSize * componentCount;
        final int byteStride = Optional.ofNullable(bufferView.getByteStride()).orElse(elementByteSize);

        final ByteBuffer extracted = MemoryUtil.memAlloc(count * elementByteSize);
        for (int index = 0; index < count; index++) {
            final int elementOffset = byteOffset + index * byteStride;

            for (int componentIndex = 0; componentIndex < componentCount; componentIndex++) {
                final int componentOffset = elementOffset + componentIndex * componentByteSize;

                for (int byteIndex = 0; byteIndex < componentByteSize; byteIndex++) {
                    final int position = index * elementByteSize + componentIndex * componentByteSize + byteIndex;
                    extracted.put(position, data[componentOffset + byteIndex]);
                }
            }
        }
        return Optional.of(extracted);
    }

    /**
     * Map a gltf primitive mode into a primitive type
     *
     * @param mode The mode to map
     * @return The corresponding primitive type
     * @throws UnsupportedOperationException if the mode is not supported
     */
    private PrimitiveType mapPrimitiveType(final PrimitiveMode mode) {
        switch (mode) {
            case POINTS:
                return PrimitiveType.POINTS;
            case TRIANGLES:
                return PrimitiveType.TRIANGLES;
            case TRIANGLE_STRIP:
                return PrimitiveType.TRIANGLE_STRIP;
            default:
                throw new UnsupportedOperationException("Unsupported primitive type " + mode);
        }
    }

    /**
     * Map a gltf material
     *
     * @param gltfMaterial The material to map
     * @return A new material
     */
    private Material mapMaterial(final com.adrien.tools.gltf.Material gltfMaterial) {
        final com.adrien.tools.gltf.Color color = gltfMaterial.getPbrMetallicRoughness().getBaseColorFactor();
        final com.adrien.tools.gltf.Color emissive = gltfMaterial.getEmissiveFactor();
        final float metallic = gltfMaterial.getPbrMetallicRoughness().getMetallicFactor();
        final float roughness = gltfMaterial.getPbrMetallicRoughness().getRoughnessFactor();
        final Texture diffuseTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getBaseColorTexture())
                .map(TextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final Texture emissiveTexture = Optional.ofNullable(gltfMaterial.getEmissiveTexture())
                .map(TextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final Texture pbrTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getMetallicRoughnessTexture())
                .map(TextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final Texture normalMap = Optional.ofNullable(gltfMaterial.getNormalTexture()).map(NormalTextureInfo::getTexture)
                .map(this::mapTexture).orElse(null);

        return Material.builder()
                .diffuse(new Color(color.getR(), color.getG(), color.getB(), color.getA()))
                .emissive(new Color(emissive.getR(), emissive.getG(), emissive.getB(), emissive.getA()))
                .roughness(roughness)
                .metallic(metallic)
                .diffuse(diffuseTexture)
                .emissive(emissiveTexture)
                .orm(pbrTexture)
                .normals(normalMap)
                .build();
    }

    /**
     * Map a gltf texture
     *
     * @param texture The texture to map
     * @return A new texture or null
     */
    private Texture mapTexture(final com.adrien.tools.gltf.Texture texture) {
        if (Objects.isNull(texture) || Objects.isNull(texture.getSource())) {
            return null;
        }

        final String path = Paths.get(this.directory, texture.getSource().getUri()).toString();
        System.out.println(path);

        final Sampler sampler = texture.getSampler();

        final TextureParameters.Builder params = TextureParameters.builder();
        params.mipmaps(true);
        Optional.ofNullable(sampler.getMagFilter()).ifPresent(filter -> params.magFilter(this.mapFilter(filter)));
        Optional.ofNullable(sampler.getMinFilter()).ifPresent(filter -> params.minFilter(this.mapFilter(filter)));
        params.sWrap(this.mapWrap(sampler.getWrapS()));
        params.tWrap(this.mapWrap(sampler.getWrapT()));

        return Texture.fromFile(path, params);
    }

    /**
     * Map gltf texture filter
     *
     * @param filter The filter to map
     * @return The corresponding texture filter
     */
    private Filter mapFilter(final com.adrien.tools.gltf.Filter filter) {
        switch (filter) {
            case NEAREST:
                return Filter.NEAREST;
            case LINEAR:
                return Filter.LINEAR;
            case NEAREST_MIPMAP_NEAREST:
                return Filter.MIPMAP_NEAREST_NEAREST;
            case NEAREST_MIPMAP_LINEAR:
                return Filter.MIPMAP_NEAREST_LINEAR;
            case LINEAR_MIPMAP_NEAREST:
                return Filter.MIPMAP_LINEAR_NEAREST;
            case LINEAR_MIPMAP_LINEAR:
                return Filter.MIPMAP_LINEAR_LINEAR;
            default:
                throw new UnsupportedOperationException("Unsupported filter " + filter);
        }
    }

    /**
     * Map gltf wrap mode
     *
     * @param wrapMode the wrap mode to map
     * @return The corresponding wrap
     */
    private Wrap mapWrap(final WrapMode wrapMode) {
        switch (wrapMode) {
            case REPEAT:
                return Wrap.REPEAT;
            case CLAMP_TO_EDGE:
                return Wrap.CLAMP_TO_EDGE;
            case MIRRORED_REPEAT:
                return Wrap.MIRRORED_REPEAT;
            default:
                throw new UnsupportedOperationException("Unsupported wrap mode " + wrapMode);
        }
    }
}
