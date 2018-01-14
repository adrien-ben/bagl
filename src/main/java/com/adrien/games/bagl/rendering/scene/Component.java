package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.Transform;
import com.adrien.games.bagl.rendering.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base component
 * <p>
 * Contains transformation data of the component
 *
 * @author adrien
 */
public abstract class Component {

    protected final Transform localTransform;
    protected final Transform transform;
    private List<Component> children;
    private Component parent;

    /**
     * Construct a new component
     */
    protected Component() {
        this.localTransform = new Transform();
        this.transform = new Transform();
        this.children = new ArrayList<>();
        this.parent = null;
    }

    /**
     * Traverse the scene graph
     * <p>
     * This method first compute the derived transform of the component.
     * Then it calls the {@link Component#visit(Renderer)} method of the
     * component. Finally it calls the {@link Component#traverse(Renderer)}
     * of its children
     *
     * @param renderer
     */
    public void traverse(final Renderer renderer) {
        this.computeTransform();
        this.visit(renderer);
        this.children.forEach(child -> child.traverse(renderer));
    }

    /**
     * Action to perform when visited by the {@link Renderer}
     * <p>
     * This method must be implement by classes extending this class
     *
     * @param renderer The visiting renderer
     */
    public abstract void visit(final Renderer renderer);

    /**
     * Add a child component to this component
     * <p>
     * The parent of the child become the component to which
     * it is added as child
     *
     * @param child The child component to add
     */
    public void addChild(final Component child) {
        this.children.add(child);
        child.parent = this;
    }

    /**
     * Check if this component is a root component
     * <p>
     * A component is considered as root if it as no parent
     *
     * @return true if the component is root
     */
    public boolean isRoot() {
        return Objects.isNull(parent);
    }

    /**
     * Compute the derived transform of this component
     * <p>
     * If this component is a root then its derived transform is equal
     * to its local transform. If it is not a root, its transform is
     * its local transform combined to the transform of its parent
     */
    private void computeTransform() {
        if (!this.isRoot()) {
            final Transform parentTransform = this.parent.getTransform();
            Transform.transform(this.localTransform, parentTransform, this.transform);
        } else {
            this.transform.set(this.localTransform);
        }
    }

    public Transform getTransform() {
        return this.transform;
    }

    public Transform getLocalTransform() {
        return this.localTransform;
    }
}
