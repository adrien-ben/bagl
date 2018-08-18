package com.adrienben.games.bagl.engine.resource.gltf;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.core.utils.Tuple2;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.resource.gltf.mappers.*;
import com.adrienben.games.bagl.opengl.BufferUsage;
import com.adrienben.games.bagl.opengl.texture.Filter;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.adrienben.games.bagl.opengl.vertex.IndexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBuffer;
import com.adrienben.games.bagl.opengl.vertex.VertexBufferParams;
import com.adrienben.games.bagl.opengl.vertex.VertexElement;
import com.adrienben.tools.gltf.models.*;
import org.joml.AABBf;
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

    private final ChannelMapper channelMapper;
    private final DataTypeMapper dataTypeMapper;
    private final PrimitiveTypeMapper primitiveTypeMapper;
    private final FilterMapper filterMapper;
    private final WrapMapper wrapMapper;
    private final AlphaModeMapper alphaModeMapper;

    private String directory;

    public GltfLoader() {
        this.channelMapper = new ChannelMapper();
        this.dataTypeMapper = new DataTypeMapper();
        this.primitiveTypeMapper = new PrimitiveTypeMapper();
        this.filterMapper = new FilterMapper();
        this.wrapMapper = new WrapMapper();
        this.alphaModeMapper = new AlphaModeMapper();
    }

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
        return mesh.getPrimitives()
                .stream()
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
        final var iBuffer = this.createIndexBuffer(primitive.getIndices());
        final var vBuffers = primitive.getAttributes().entrySet().stream()
                .map(entry -> this.createVertexBuffer(entry.getKey(), entry.getValue()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        final var primitiveType = primitiveTypeMapper.map(primitive.getMode());
        final var aabb = getMeshAabb(primitive);

        final Mesh mesh = Mesh.builder().vertexBuffers(vBuffers).indexBuffer(iBuffer).primitiveType(primitiveType).aabb(aabb).build();
        final Material material = this.mapMaterial(primitive.getMaterial());

        return new Tuple2<>(mesh, material);
    }

    private AABBf getMeshAabb(final GltfPrimitive primitive) {
        final var positionAccessor = Optional.of(primitive.getAttributes()).map(attributes -> attributes.get("POSITION"));
        final var min = positionAccessor.map(GltfAccessor::getMin).map(this::getVector3fFromAccessorsMinMax).orElse(new Vector3f());
        final var max = positionAccessor.map(GltfAccessor::getMax).map(this::getVector3fFromAccessorsMinMax).orElse(new Vector3f());
        return new AABBf(min, max);
    }

    private Vector3f getVector3fFromAccessorsMinMax(final List<Float> accessorsMinMax) {
        return new Vector3f(accessorsMinMax.get(0), accessorsMinMax.get(1), accessorsMinMax.get(2));
    }

    /**
     * Create a index buffer from a primitive indices accessor
     *
     * @param accessor The accessor pointing to the index data
     * @return A new index buffer
     */
    private IndexBuffer createIndexBuffer(final GltfAccessor accessor) {
        return this.extractData(accessor).map(indices -> {
            final var dataType = dataTypeMapper.map(accessor.getComponentType());
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

        final var channel = channelMapper.map(type);
        if (channel == -1) {
            return Optional.empty();
        }

        final var vertexBufferParams = mapVertexBufferParams(channel, accessor);
        final var vertexBuffer = new VertexBuffer(vertices, vertexBufferParams);
        MemoryUtil.memFree(vertices);
        return Optional.of(vertexBuffer);
    }

    private VertexBufferParams mapVertexBufferParams(final int channel, final GltfAccessor accessor) {
        return VertexBufferParams.builder()
                .dataType(dataTypeMapper.map(accessor.getComponentType()))
                .element(new VertexElement(channel, accessor.getType().getComponentCount(), accessor.getNormalized()))
                .build();
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
        final var roughnessMetallicMap = Optional.ofNullable(gltfMaterial.getPbrMetallicRoughness().getMetallicRoughnessTexture())
                .map(GltfTextureInfo::getTexture).map(this::mapTexture).orElse(null);
        final var normalMap = Optional.ofNullable(gltfMaterial.getNormalTexture()).map(GltfNormalTextureInfo::getTexture)
                .map(this::mapTexture).orElse(null);
        final var alphaMode = alphaModeMapper.map(gltfMaterial.getAlphaMode());
        final var alphaCutoff = gltfMaterial.getAlphaCutoff();

        final Material.Builder builder = Material.builder()
                .diffuse(mapColor(color))
                .emissive(mapColor(emissive))
                .emissiveIntensity(DEFAULT_EMISSIVE_INTENSITY)
                .roughness(gltfMaterial.getPbrMetallicRoughness().getRoughnessFactor())
                .metallic(gltfMaterial.getPbrMetallicRoughness().getMetallicFactor())
                .diffuse(diffuseTexture)
                .emissive(emissiveTexture)
                .roughnessMetallic(roughnessMetallicMap)
                .normals(normalMap)
                .doubleSided(gltfMaterial.getDoubleSided())
                .alphaMode(alphaMode)
                .alphaCutoff(alphaCutoff);

        Optional.ofNullable(gltfMaterial.getOcclusionTexture()).ifPresent(gltfOcclusion -> {
            builder.occlusionStrength(gltfOcclusion.getStrength());
            builder.occlusion(mapTexture(gltfOcclusion.getTexture()));
        });

        return builder.build();
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
    private Texture2D mapTexture(final GltfTexture texture) {
        if (Objects.isNull(texture) || Objects.isNull(texture.getSource())) {
            return null;
        }
        final var params = mapTextureParameters(texture.getSampler());
        return this.generateTextureFromImage(texture.getSource(), params);
    }

    private TextureParameters.Builder mapTextureParameters(final GltfSampler sampler) {
        final var magFilter = Optional.ofNullable(sampler.getMagFilter()).map(filterMapper::map);
        final var minFilter = Optional.ofNullable(sampler.getMinFilter()).map(filterMapper::map);
        final var isMipmap = magFilter.map(Filter::isMipmap).orElse(false)
                || minFilter.map(Filter::isMipmap).orElse(false);

        final var params = TextureParameters.builder();
        magFilter.ifPresent(params::magFilter);
        minFilter.ifPresent(params::minFilter);
        params.mipmaps(isMipmap);
        params.sWrap(wrapMapper.map(sampler.getWrapS()));
        params.tWrap(wrapMapper.map(sampler.getWrapT()));
        params.anisotropic(Configuration.getInstance().getAnisotropicLevel());
        return params;
    }

    /**
     * Generate a texture from a {@link GltfImage}
     *
     * @param gltfImage The gltf image from which to generate the texture
     * @param params    The parameters of the texture
     * @return A new texture
     */
    private Texture2D generateTextureFromImage(final GltfImage gltfImage, final TextureParameters.Builder params) {
        if (Objects.nonNull(gltfImage.getBufferView())) {
            return generateTextureFromBufferView(gltfImage.getBufferView(), params);
        } else if (Objects.nonNull(gltfImage.getData())) {
            return generateTextureFromImageData(gltfImage.getData(), params);
        }
        return generateTextureFromFile(gltfImage.getUri(), params);
    }

    private Texture2D generateTextureFromBufferView(final GltfBufferView bufferView, final TextureParameters.Builder params) {
        final var length = bufferView.getByteLength();
        final var imageData = MemoryUtil.memAlloc(length)
                .put(bufferView.getBuffer().getData(), bufferView.getByteOffset(), length).flip();
        return generateTextureFromByteBufferAndFreeBuffer(imageData, params);
    }

    private Texture2D generateTextureFromImageData(final byte[] data, final TextureParameters.Builder params) {
        final var imageData = MemoryUtil.memAlloc(data.length).put(data).flip();
        return generateTextureFromByteBufferAndFreeBuffer(imageData, params);
    }

    private Texture2D generateTextureFromByteBufferAndFreeBuffer(final ByteBuffer byteBuffer, final TextureParameters.Builder params) {
        final var texture = Texture2D.fromMemory(byteBuffer, params);
        MemoryUtil.memFree(byteBuffer);
        return texture;
    }

    private Texture2D generateTextureFromFile(final String path, final TextureParameters.Builder params) {
        return Texture2D.fromFile(ResourcePath.get(this.directory, path), params);
    }
}
