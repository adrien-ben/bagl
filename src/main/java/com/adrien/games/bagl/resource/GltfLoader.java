package com.adrien.games.bagl.resource;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
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

        final GltfAsset gltfAsset = GltfAsset.Factory.fromFile(path);
        if (Objects.isNull(gltfAsset)) {
            throw new IllegalStateException("Failed to load gltf " + path);
        }

        final Model model = new Model();

        gltfAsset.getMeshes()
                .stream()
                .map(GltfMesh::getPrimitives)
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
    private Tuple2<Mesh, Material> createMesh(final GltfPrimitive primitive) {
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
    private IndexBuffer createIndexBuffer(final GltfAccessor accessor) {
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
    private Optional<VertexBuffer> createVertexBuffer(final String type, final GltfAccessor accessor) {
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
    private DataType mapDataType(final GltfComponentType componentType) {
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
    private Optional<ByteBuffer> extractData(final GltfAccessor accessor) {
        if (Objects.isNull(accessor)) {
            return Optional.empty();
        }
        if (Objects.nonNull(accessor.getSparse())) {
            throw new UnsupportedOperationException("Sparse buffer not supported");
        }
        if (Objects.isNull(accessor.getBufferView())) {
            throw new UnsupportedOperationException("Accessor has no buffer view");
        }

        final GltfBufferView bufferView = accessor.getBufferView();
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
    private PrimitiveType mapPrimitiveType(final GltfPrimitiveMode mode) {
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
    private Material mapMaterial(final GltfMaterial gltfMaterial) {
        final GltfColor color = gltfMaterial.getPbrMetallicRoughness().getBaseColorFactor();
        final GltfColor emissive = gltfMaterial.getEmissiveFactor();
        final float metallic = gltfMaterial.getPbrMetallicRoughness().getMetallicFactor();
        final float roughness = gltfMaterial.getPbrMetallicRoughness().getRoughnessFactor();
        final Texture diffuseTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getBaseColorTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final Texture emissiveTexture = Optional.ofNullable(gltfMaterial.getEmissiveTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final Texture pbrTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getMetallicRoughnessTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final Texture normalMap = Optional.ofNullable(gltfMaterial.getNormalTexture()).map(GltfNormalTextureInfo::getTexture)
                .map(this::mapTexture).orElse(null);

        return Material.builder()
                .diffuse(new Color(color.getR(), color.getG(), color.getB(), color.getA()))
                .emissive(new Color(emissive.getR(), emissive.getG(), emissive.getB(), emissive.getA()))
                .emissiveIntensity(1f)
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
    private Texture mapTexture(final GltfTexture texture) {
        if (Objects.isNull(texture) || Objects.isNull(texture.getSource())) {
            return null;
        }

        final GltfSampler sampler = texture.getSampler();
        final Optional<Filter> magFilter = Optional.ofNullable(sampler.getMagFilter()).map(this::mapFilter);
        final Optional<Filter> minFilter = Optional.ofNullable(sampler.getMinFilter()).map(this::mapFilter);

        final TextureParameters.Builder params = TextureParameters.builder();
        magFilter.ifPresent(params::magFilter);
        minFilter.ifPresent(params::minFilter);
        params.mipmaps(magFilter.map(Filter::isMipmap).orElse(false)
                || minFilter.map(Filter::isMipmap).orElse(false));
        params.sWrap(this.mapWrap(sampler.getWrapS()));
        params.tWrap(this.mapWrap(sampler.getWrapT()));
        params.anisotropic(Configuration.getInstance().getAnisotropicLevel());

        return this.generateTexture(texture.getSource(), params);
    }

    /**
     * Generate a texture from a {@link GltfImage}
     *
     * @param gltfImage The gltf image from which to generate the texture
     * @param params    The parameters of the texture
     * @return A new texture
     */
    private Texture generateTexture(final GltfImage gltfImage, final TextureParameters.Builder params) {
        final Texture tex;
        if (Objects.nonNull(gltfImage.getBufferView())) {
            final GltfBufferView view = gltfImage.getBufferView();
            final int length = view.getByteLength();
            final ByteBuffer imageData = MemoryUtil.memAlloc(length)
                    .put(view.getBuffer().getData(), view.getByteOffset(), length).flip();
            tex = Texture.fromMemory(imageData, params);
            MemoryUtil.memFree(imageData);
        } else if (Objects.nonNull(gltfImage.getData())) {
            final byte[] data = gltfImage.getData();
            final ByteBuffer imageData = MemoryUtil.memAlloc(data.length).put(data).flip();
            tex = Texture.fromMemory(imageData, params);
            MemoryUtil.memFree(imageData);
        } else {
            final String path = Paths.get(this.directory, gltfImage.getUri()).toString();
            tex = Texture.fromFile(path, params);
        }
        return tex;
    }

    /**
     * Map gltf texture filter
     *
     * @param filter The filter to map
     * @return The corresponding texture filter
     */
    private Filter mapFilter(final GltfFilter filter) {
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
    private Wrap mapWrap(final GltfWrapMode wrapMode) {
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
