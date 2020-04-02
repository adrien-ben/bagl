package com.adrienben.games.bagl.engine.resource.gltf;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.animation.Animation;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.resource.gltf.mappers.*;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.tools.gltf.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final TextureMapper textureMapper = new TextureMapper();
    private final MeshMapper meshMapper = new MeshMapper();
    private final AnimationMapper animationMapper = new AnimationMapper();
    private final ModelNodeMapper modelNodeMapper = new ModelNodeMapper();
    private final JointMapper jointMapper = new JointMapper();

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

    private List<Animation<Transform>> loadAnimations(final GltfAsset gltfAsset) {
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

    private Animation<Transform> mapAnimation(final GltfAnimation animation) {
        return animationMapper.map(animation, nodes);
    }

    private void mapJoints(final GltfAsset gltfAsset) {
        gltfAsset.getNodes().forEach(node -> jointMapper.map(node, nodes));
    }

    private void cleanUp() {
        textures.clear();
        meshes.clear();
        nodes = null;
    }
}
