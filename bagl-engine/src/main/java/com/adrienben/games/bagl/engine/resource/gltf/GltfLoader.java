package com.adrienben.games.bagl.engine.resource.gltf;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.animation.Animation;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.Joint;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.resource.gltf.mappers.AnimationMapper;
import com.adrienben.games.bagl.engine.resource.gltf.mappers.MeshMapper;
import com.adrienben.games.bagl.engine.resource.gltf.mappers.ModelNodeMapper;
import com.adrienben.games.bagl.engine.resource.gltf.mappers.TextureMapper;
import com.adrienben.games.bagl.engine.resource.gltf.reader.GltfBufferReader;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.tools.gltf.models.*;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private final TextureMapper textureMapper = new TextureMapper();
    private final MeshMapper meshMapper = new MeshMapper();
    private final AnimationMapper animationMapper = new AnimationMapper();
    private final ModelNodeMapper modelNodeMapper = new ModelNodeMapper();

    private String directory;
    private final List<Texture2D> textures = new ArrayList<>();
    private final List<Map<Mesh, Material>> meshes = new ArrayList<>();
    private ModelNode[] nodes;

    /**
     * Load a gltf file
     *
     * @param path The path of the file
     * @return The loaded model
     */
    public Model load(final ResourcePath path) {
        final var gltfAsset = loadFromFileAndSetCurrentDirectory(path);
        final var model = new Model();

        nodes = new ModelNode[gltfAsset.getNodes().size()];
        loadTextures(gltfAsset);
        loadMeshes(gltfAsset);
        gltfAsset.getScenes().forEach(scene -> mapGltfScene(scene, model));
        model.setAnimations(loadAnimations(gltfAsset));
        mapJoints(gltfAsset);

        cleanUp();
        return model;
    }

    private GltfAsset loadFromFileAndSetCurrentDirectory(final ResourcePath path) {
        final var gltfAsset = GltfAsset.Factory.fromFile(path.getAbsolutePath());
        if (Objects.isNull(gltfAsset)) {
            throw new IllegalStateException("Failed to load gltf " + path);
        }
        directory = path.getParent().toString();
        return gltfAsset;
    }

    private void loadTextures(final GltfAsset gltfAsset) {
        gltfAsset.getTextures().stream().map(this::mapTexture).forEach(textures::add);
    }

    private Texture2D mapTexture(final GltfTexture gltfTexture) {
        return textureMapper.map(gltfTexture, directory);
    }

    private void loadMeshes(final GltfAsset gltfAsset) {
        gltfAsset.getMeshes().stream().map(this::mapGltfMesh).forEach(meshes::add);
    }

    private Map<Mesh, Material> mapGltfMesh(final GltfMesh gltfMesh) {
        return meshMapper.map(gltfMesh, textures);
    }

    private List<Animation> loadAnimations(final GltfAsset gltfAsset) {
        return gltfAsset.getAnimations().stream().map(this::mapAnimation).collect(Collectors.toList());
    }

    private void mapGltfScene(final GltfScene scene, final Model model) {
        if (CollectionUtils.isNotEmpty(scene.getNodes())) {
            scene.getNodes().forEach(node -> mapGltfNode(node, model.addNode()));
        }
    }

    private void mapGltfNode(final GltfNode gltfNode, final ModelNode destination) {
        nodes[gltfNode.getIndex()] = destination;
        modelNodeMapper.map(gltfNode, destination, meshes);
        if (CollectionUtils.isNotEmpty(gltfNode.getChildren())) {
            gltfNode.getChildren().forEach(child -> mapGltfNode(child, destination.addChild()));
        }
    }

    private Animation mapAnimation(final GltfAnimation animation) {
        return animationMapper.map(animation, nodes);
    }

    private void mapJoints(final GltfAsset gltfAsset) {
        gltfAsset.getNodes().forEach(this::mapNodeJoints);
    }

    private void mapNodeJoints(final GltfNode gltfNode) {
        final var gltfSkin = gltfNode.getSkin();
        if (Objects.nonNull(gltfSkin)) {
            final var gltfSkinJoints = gltfSkin.getJoints();
            final var inverseBindMatrices = mapInverseBindMatrices(gltfSkin);
            final var joints = mapJoints(gltfSkinJoints, inverseBindMatrices);
            nodes[gltfNode.getIndex()].setJoints(joints);
        }
    }

    private List<Matrix4f> mapInverseBindMatrices(final GltfSkin gltfSkin) {
        if (Objects.isNull(gltfSkin.getInverseBindMatrices())) {
            return generateEntityMatrices(gltfSkin.getJoints().size());
        }
        return extractInverseBindMatrices(gltfSkin.getInverseBindMatrices());
    }

    private List<Matrix4f> generateEntityMatrices(final int count) {
        return IntStream.range(0, count).mapToObj(index -> new Matrix4f()).collect(Collectors.toList());
    }

    private List<Matrix4f> extractInverseBindMatrices(final GltfAccessor accessor) {
        final var inverseBindMatricesReader = new GltfBufferReader(accessor.getBufferView().getBuffer());
        return IntStream.range(0, accessor.getCount()).mapToObj(index -> inverseBindMatricesReader.readMatrix4(accessor, index)).collect(Collectors.toList());
    }

    private List<Joint> mapJoints(final List<GltfNode> gltfJoints, final List<Matrix4f> inverseBindMatrices) {
        final var joints = new ArrayList<Joint>();
        for (int i = 0; i < gltfJoints.size(); i++) {
            final var gltfSkinJoint = gltfJoints.get(i);
            final var jointNode = nodes[gltfSkinJoint.getIndex()];
            final var inverseBindMatrix = inverseBindMatrices.get(i);
            joints.add(new Joint(jointNode.getTransform(), inverseBindMatrix));
        }
        return joints;
    }

    private void cleanUp() {
        textures.clear();
        meshes.clear();
        nodes = null;
    }
}
