package com.adrien.games.bagl.rendering.model;

import com.adrien.games.bagl.core.Transform;
import com.adrien.games.bagl.rendering.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A node of a model
 * <p>
 * It contains transform information and a set of mesh composing
 * the node. It can have children nodes
 * <p>
 * Client cannot instantiate model nodes directly. This can only be done
 * using {@link Model#addNode()}, {@link Model#addNode(Transform)},
 * {@link ModelNode#addChild()} or {@link ModelNode#addChild(Transform)}.
 * This ensure a proper set up of the hierarchy.
 *
 * @author adrien
 */
public final class ModelNode {

    private final Model owner;
    private final ModelNode parent;
    private final Transform transform;
    private final Transform localTransform;
    private final Map<Mesh, Material> meshes = new HashMap<>();
    private final List<ModelNode> children = new ArrayList<>();

    /**
     * Construct a new node
     *
     * @param owner     The model that owns the node hierarchy
     * @param parent    The parent node (can be null)
     * @param transform The local transform of the node
     */
    private ModelNode(final Model owner, final ModelNode parent, final Transform transform) {
        this.owner = owner;
        this.parent = parent;
        this.localTransform = transform;
        this.transform = new Transform().transform(transform);
    }

    /**
     * Construct a new node
     *
     * @param owner     The model that owns the node hierarchy
     * @param transform The local transform of the node
     */
    ModelNode(final Model owner, final Transform transform) {
        this(owner, null, transform);
    }

    /**
     * Transform the node
     *
     * @param transform The transform to apply
     */
    public void transform(final Transform transform) {
        Transform.transform(this.localTransform, transform, this.transform);
        this.children.forEach(child -> child.transform(this.transform));
    }

    /**
     * Add a mesh to the node
     *
     * @param mesh     The mesh to add
     * @param material The material to use with the mesh
     * @return This
     */
    public ModelNode addMesh(final Mesh mesh, final Material material) {
        this.meshes.put(mesh, material);
        this.owner.registerMesh(mesh);
        return this;
    }

    /**
     * Create a child for this node
     *
     * @param transform The local transform of the child
     * @return The created child node
     */
    public ModelNode addChild(final Transform transform) {
        final ModelNode child = new ModelNode(this.owner, this, transform);
        this.children.add(child);
        return child;
    }

    /**
     * Create a child for this node with a default transform
     *
     * @return The created child node
     */
    public ModelNode addChild() {
        return this.addChild(new Transform());
    }

    public Transform getTransform() {
        return this.transform;
    }

    public Map<Mesh, Material> getMeshes() {
        return this.meshes;
    }

    public List<ModelNode> getChildren() {
        return this.children;
    }
}