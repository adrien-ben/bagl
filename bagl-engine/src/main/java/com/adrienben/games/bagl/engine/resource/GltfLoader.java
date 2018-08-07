package com.adrienben.games.bagl.engine.resource;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.core.utils.Tuple2;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.opengl.BufferUsage;
import com.adrienben.games.bagl.opengl.DataType;
import com.adrienben.games.bagl.opengl.PrimitiveType;
import com.adrienben.games.bagl.opengl.texture.Filter;
import com.adrienben.games.bagl.opengl.texture.Texture;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.adrienben.games.bagl.opengl.texture.Wrap;
import com.adrienben.games.bagl.opengl.vertex.IndexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBufferParams;
import com.adrienben.games.bagl.opengl.vertex.VertexElement;
import com.adrienben.tools.gltf.models.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gltf file loader
 * <p>
 * Load the content of a gltf file into on {@link Model}. Each non empty
 * scene and each of their nodes containing a mesh will be transformed in
 * a {@link ModelNode}
 *
 * @author adrien
 */
public class GltfLoader {

    private static final float DEFAULT_EMISSIVE_INTENSITY = 1f;

    private String directory;

    /**
     * Load a gltf file
     *
     * @param path The path of the file
     * @return The loaded model
     */
    public Model load(final ResourcePath path) {
        this.directory = path.getParent().toString();

        final var gltfAsset = GltfAsset.Factory.fromFile(path.getAbsolutePath());
        if (Objects.isNull(gltfAsset)) {
            throw new IllegalStateException("Failed to load gltf " + path);
        }

        final var meshes = gltfAsset.getMeshes().stream().map(this::mapGltfMesh).collect(Collectors.toList());
        final var model = new Model();
        gltfAsset.getScenes().forEach(scene -> this.mapGltfScene(scene, model, meshes));
        return model;
    }

    /**
     * Map a gltf scene
     *
     * @param scene  The scene to map
     * @param model  The model to which to add the nodes
     * @param meshes The meshes of the asset
     */
    private void mapGltfScene(final GltfScene scene, final Model model, final List<Map<Mesh, Material>> meshes) {
        if (CollectionUtils.isNotEmpty(scene.getNodes())) {
            scene.getNodes().forEach(node -> {
                if (CollectionUtils.isNotEmpty(node.getChildren()) || Objects.nonNull(node.getMesh())) {
                    final var root = model.addNode(this.mapTransform(node));
                    if (Objects.nonNull(node.getMesh())) {
                        meshes.get(node.getMesh().getIndex()).forEach(root::addMesh);
                    }

                    if (CollectionUtils.isNotEmpty(node.getChildren())) {
                        node.getChildren().forEach(child -> this.mapGltfNode(child, root, meshes));
                    }
                }
            });
        }
    }

    /**
     * Map a gltf node
     *
     * @param gltfNode The node to mesh
     * @param parent   The parent node to which to add the children
     * @param meshes   The meshes of the asset
     */
    private void mapGltfNode(final GltfNode gltfNode, final ModelNode parent, final List<Map<Mesh, Material>> meshes) {
        if (CollectionUtils.isNotEmpty(gltfNode.getChildren()) || Objects.nonNull(gltfNode.getMesh())) {
            final var node = parent.addChild(this.mapTransform(gltfNode));

            if (Objects.nonNull(gltfNode.getMesh())) {
                meshes.get(gltfNode.getMesh().getIndex()).forEach(node::addMesh);
            }
            if (CollectionUtils.isNotEmpty(gltfNode.getChildren())) {
                gltfNode.getChildren().forEach(child -> this.mapGltfNode(child, node, meshes));
            }
        }
    }

    /**
     * Map a node's transform
     *
     * @param gltfNode The node whose map has to be mapped
     * @return A new transform
     */
    private Transform mapTransform(final GltfNode gltfNode) {
        final var transform = new Transform();
        transform.setTranslation(this.mapGltfVec3(gltfNode.getTranslation()));
        transform.setRotation(this.mapGltfQuaternion(gltfNode.getRotation()));
        transform.setScale(this.mapGltfVec3(gltfNode.getScale()));
        return transform;
    }

    /**
     * Map a vector3
     *
     * @param vec3 The vector to map
     * @return A new vector
     */
    private Vector3f mapGltfVec3(final GltfVec3 vec3) {
        return new Vector3f(vec3.getX(), vec3.getY(), vec3.getZ());
    }

    /**
     * Map a quaternion
     *
     * @param quaternion The quaternion to map
     * @return A new quaternion
     */
    private Quaternionf mapGltfQuaternion(final GltfQuaternion quaternion) {
        return new Quaternionf(quaternion.getI(), quaternion.getJ(), quaternion.getK(), quaternion.getA());
    }

    /**
     * Map a gltf mesh into a map mapping meshes to their material
     *
     * @param mesh The mesh to map
     * @return A map of meshes mapped to their material
     */
    private Map<Mesh, Material> mapGltfMesh(final GltfMesh mesh) {
        return mesh.getPrimitives().stream().map(this::createMesh).collect(Collectors.toMap(Tuple2::getFirst, Tuple2::getSecond));
    }

    /**
     * Create a mesh from a gltf primitive
     *
     * @param primitive The primitive from which to create the mesh
     * @return A new mesh
     */
    private Tuple2<Mesh, Material> createMesh(final GltfPrimitive primitive) {
        final var iBuffer = this.createIndexBuffer(primitive.getIndices());

        final var vBuffers = primitive.getAttributes()
                .entrySet()
                .stream()
                .map(entry -> this.createVertexBuffer(entry.getKey(), entry.getValue()))
                .flatMap(Optional::stream)
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
        return this.extractData(accessor).map(indices -> {
            final var dataType = this.mapDataType(accessor.getComponentType());
            final var indexBuffer = new IndexBuffer(indices, dataType, BufferUsage.STATIC_DRAW);
            MemoryUtil.memFree(indices);
            return indexBuffer;
        }).orElse(null);
    }

    /**
     * Create a vertex buffer from a primitive attribute
     *
     * @param type     The type of attribute
     * @param accessor The accessor
     * @return A new vertex buffer
     */
    private Optional<VertexBuffer> createVertexBuffer(final String type, final GltfAccessor accessor) {
        final var vertices = this.extractData(accessor).orElseThrow(() -> new IllegalArgumentException(
                "Primitive attribute's accessor should not be null"));

        final var channel = this.mapChannel(type);
        if (channel == -1) {
            return Optional.empty();
        }

        final var builder = VertexBufferParams.builder()
                .dataType(this.mapDataType(accessor.getComponentType()))
                .element(new VertexElement(channel, accessor.getType().getComponentCount(), accessor.getNormalized()));

        final var vertexBuffer = new VertexBuffer(vertices, builder.build());
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

                for (var byteIndex = 0; byteIndex < componentByteSize; byteIndex++) {
                    final var position = index * elementByteSize + componentIndex * componentByteSize + byteIndex;
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
        final var color = gltfMaterial.getPbrMetallicRoughness().getBaseColorFactor();
        final var emissive = gltfMaterial.getEmissiveFactor();
        final var diffuseTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getBaseColorTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final var emissiveTexture = Optional.ofNullable(gltfMaterial.getEmissiveTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final var pbrTexture = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getMetallicRoughnessTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final var normalMap = Optional.ofNullable(gltfMaterial.getNormalTexture()).map(GltfNormalTextureInfo::getTexture)
                .map(this::mapTexture).orElse(null);
        final var alphaMode = mapAlphaMode(gltfMaterial.getAlphaMode());
        final var alphaCutoff = gltfMaterial.getAlphaCutoff();

        return Material.builder()
                .diffuse(mapColor(color))
                .emissive(mapColor(emissive))
                .emissiveIntensity(DEFAULT_EMISSIVE_INTENSITY)
                .roughness(gltfMaterial.getPbrMetallicRoughness().getRoughnessFactor())
                .metallic(gltfMaterial.getPbrMetallicRoughness().getMetallicFactor())
                .diffuse(diffuseTexture)
                .emissive(emissiveTexture)
                .orm(pbrTexture)
                .normals(normalMap)
                .doubleSided(gltfMaterial.getDoubleSided())
                .alphaMode(alphaMode)
                .alphaCutoff(alphaCutoff)
                .build();
    }

    private Color mapColor(final GltfColor color) {
        return new Color(color.getR(), color.getG(), color.getB(), color.getA());
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

        final var sampler = texture.getSampler();
        final var magFilter = Optional.ofNullable(sampler.getMagFilter()).map(this::mapFilter);
        final var minFilter = Optional.ofNullable(sampler.getMinFilter()).map(this::mapFilter);

        final var params = TextureParameters.builder();
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
            final var view = gltfImage.getBufferView();
            final var length = view.getByteLength();
            final var imageData = MemoryUtil.memAlloc(length)
                    .put(view.getBuffer().getData(), view.getByteOffset(), length).flip();
            tex = Texture.fromMemory(imageData, params);
            MemoryUtil.memFree(imageData);
        } else if (Objects.nonNull(gltfImage.getData())) {
            final var data = gltfImage.getData();
            final var imageData = MemoryUtil.memAlloc(data.length).put(data).flip();
            tex = Texture.fromMemory(imageData, params);
            MemoryUtil.memFree(imageData);
        } else {
            tex = Texture.fromFile(ResourcePath.get(this.directory, gltfImage.getUri()), params);
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

    private AlphaMode mapAlphaMode(final GltfAlphaMode alphaMode) {
        switch (alphaMode) {
            case OPAQUE:
                return AlphaMode.OPAQUE;
            case MASK:
                return AlphaMode.MASK;
            case BLEND:
                return AlphaMode.BLEND;
            default:
                throw new UnsupportedOperationException("Unsupported alpha mode " + alphaMode);
        }
    }
}
