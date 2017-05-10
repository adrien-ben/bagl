package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.Transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Node of the scene graph.
 *
 */
public class SceneNode<T> {

    private T t;

    /**
     * The local transform of this node.
     */
    private Transform localTransform;

    /**
     * The derived transform of this node.
     */
    private final Transform transform;

    /**
     * The parent node of this node. If null this node is a root.
     */
    private SceneNode<T> parent;

    /**
     * The children nodes of this node.
     */
    private final List<SceneNode<T>> children;

    public SceneNode() {
        this(null);
    }

    public SceneNode(T t) {
        this.localTransform = new Transform();
        this.transform = new Transform();
        this.t = t;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    /**
     * <p>Adds a child to this node.
     * <p>The child node is removed from its current parent
     * if it has one. This node is set as its new parent.
     * @param child The child node.
     */
    public void addChild(SceneNode<T> child) {
        if(Objects.nonNull(child.parent)) {
            child.parent.removeChild(child);
        }
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * Removes one of the children from this node.
     * @param child The child to remove.
     */
    public void removeChild(SceneNode<T> child) {
        this.children.remove(child);
        child.parent = null;
    }

    /**
     * Applies a consumer on every node of the tree, starting
     * with this node.
     * @param consumer The consumer to apply.
     */
    public void apply(Consumer<SceneNode<T>> consumer) {
        consumer.accept(this);
        this.children.stream().forEach(consumer);
    }

    /**
     * <p>Checks whether this node is a root node.
     * <p>A node is a root if it has no parent.
     * @return <code>true</code> if it is a root node, <code>false</code> otherwise.
     */
    public boolean isRoot() {
        return Objects.isNull(parent);
    }

    /**
     * <p>Checks whether this node is empty.
     * <p>An empty node is a node whose data is null.
     * @return <code>true</code> if empty, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return Objects.isNull(this.t);
    }

    /**
     * Returns the derived transform of the node.
     * <p>The derived transform is the local transform
     * transformed by the transform of its parent. If
     * this node is a root the the derived transform is
     * equal to the local transform.
     * @return The derived transform.
     */
    public Transform getTransform() {
        if(!isRoot()) {
            final Transform parentTransform = this.parent.getTransform();
            Transform.transform(this.localTransform, parentTransform, this.transform);
        } else {
            this.transform.set(this.localTransform);
        }
        return transform;
    }

    public Transform getLocalTransform() {
        return localTransform;
    }

    public void setLocalTransform(Transform localTransform) {
        this.localTransform = localTransform;
    }

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }

    public SceneNode<T> getParent() {
        return parent;
    }

    private void setParent(SceneNode<T> parent) {
        this.parent = parent;
    }

    public List<SceneNode<T>> getChildren() {
        return children;
    }

}
