package com.adrienben.games.bagl.engine.rendering.model;

import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.Material;

import java.util.*;

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
    private List<Joint> joints;

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
     * @param toApply The transform to apply
     */
    public void transform(final Transform toApply) {
        Transform.transform(localTransform, toApply, transform);
        children.forEach(child -> child.transform(transform));
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
        final var child = new ModelNode(this.owner, this, transform);
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

    public Transform getLocalTransform() {
        return localTransform;
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

    public Optional<List<Joint>> getJoints() {
        return Optional.ofNullable(joints);
    }

    public void setJoints(final List<Joint> joints) {
        this.joints = joints;
    }
}
