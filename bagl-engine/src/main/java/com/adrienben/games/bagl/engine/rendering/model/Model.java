package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.core.Asset;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.Time;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.animation.Animation;

import java.util.*;

/**
 * A model is the root of a {@link ModelNode} tree
 * <p>
 * It contains all the {@link Mesh}es of the hierarchy.
 *
 * @author adrien
 */
public final class Model implements Asset {

    private final List<ModelNode> nodes = new ArrayList<>();
    private final Set<Mesh> meshes = new HashSet<>();
    private List<Animation<Transform>> animations;

    /**
     * {@inheritDoc}
     *
     * @see Asset#destroy()
     */
    @Override
    public void destroy() {
        this.meshes.forEach(Mesh::destroy);
    }


    public void update(final Time time) {
        if (CollectionUtils.isNotEmpty(animations)) {
            animations.get(0).step(time);
        }
    }

    /**
     * Transform the model
     * <p>
     * The call will be forwarded to its nodes
     *
     * @param transform The transform to apply to the model
     */
    public void transform(final Transform transform) {
        this.nodes.forEach(node -> node.transform(transform));
    }

    /**
     * Add a node the the model
     *
     * @param transform The transform of the node to add
     * @return The new node
     */
    public ModelNode addNode(final Transform transform) {
        final var node = new ModelNode(this, transform);
        this.nodes.add(node);
        return node;
    }

    /**
     * Add a node the the model
     *
     * @return The new node
     */
    public ModelNode addNode() {
        return this.addNode(new Transform());
    }

    /**
     * Register a mesh
     * <p>
     * Node added to the hierarchy should register their meshes so their can be
     * destroyed when the model is destroyed. Since the mesh can be shared between
     * nodes this helps preventing that meshes are destroyed several time
     *
     * @param mesh The mesh to register
     */
    void registerMesh(final Mesh mesh) {
        this.meshes.add(mesh);
    }

    public List<ModelNode> getNodes() {
        return this.nodes;
    }

    public List<Animation<Transform>> getAnimations() {
        return Collections.unmodifiableList(animations);
    }

    public void setAnimations(final List<Animation<Transform>> animations) {
        this.animations = animations;
    }
}
