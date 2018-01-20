package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Base component
 * <p>
 * Contains transformation data of the component and
 * identification data (id and tags)
 *
 * @author adrien
 */
public abstract class Component {

    private Scene parentScene;
    private final String id;
    private final List<String> tags;
    private final Transform localTransform;
    protected final Transform transform;
    private List<Component> children;
    private Component parent;

    /**
     * Construct a new component
     *
     * @param id   The id of the component
     * @param tags The tags of the component
     */
    protected Component(final String id, final String... tags) {
        this.parentScene = null;
        this.id = id;
        this.tags = Arrays.asList(tags);
        this.localTransform = new Transform();
        this.transform = new Transform();
        this.children = new ArrayList<>();
        this.parent = null;
    }

    /**
     * Update the component
     * <p>
     * This method calls {@link Component#onUpdate(Time)} method then calls
     * the update method of its children
     * <p>
     * This method also update the component's transform BEFORE calling
     * {@link Component#onUpdate(Time)}
     *
     * @param time The time of the program
     */
    public void update(final Time time) {
        this.computeTransform();
        this.onUpdate(time);
        this.children.forEach(child -> child.update(time));
    }

    /**
     * Action to perform when the component is being updated
     * <p>
     * When called the transform of the component have already been
     * computed, so if you make any change to the local transform
     * of the component and want then taken into account at the current
     * frame you will have to call {@link Component#computeTransform()}
     * yourself
     *
     * @param time The time of the program
     */
    protected abstract void onUpdate(final Time time);

    /**
     * Accept a visitor
     * <p>
     * This method calls the {@link Component#onAccept(ComponentVisitor)}'s
     * method of this component then calls the accept method of its children
     *
     * @param visitor The visitor to accept
     */
    public void accept(final ComponentVisitor visitor) {
        this.onAccept(visitor);
        this.children.forEach(child -> child.accept(visitor));
    }

    /**
     * Action to perform when visited by a {@link ComponentVisitor}
     *
     * @param visitor The visitor
     */
    protected abstract void onAccept(final ComponentVisitor visitor);

    /**
     * Add a child component to this component
     * <p>
     * The parent of the child become the component to which
     * it is added as child. The parent scene of the current
     * component is also set as the parent scene of the added
     * child and the child is added to the parent scene for
     * faster retrieval
     *
     * @param child The child component to add
     */
    public void addChild(final Component child) {
        this.children.add(child);
        child.parent = this;
        child.parentScene = this.parentScene;
        // FIXME : crash if the current component has no parent scene
        this.parentScene.addComponent(child);
    }

    /**
     * Check if this component is a root component
     * <p>
     * A component is considered as root if it as no parent
     *
     * @return true if the component is root
     */
    private boolean isRoot() {
        return Objects.isNull(this.parent);
    }

    /**
     * Compute the derived transform of this component
     * <p>
     * If this component is a root then its derived transform is equal
     * to its local transform. If it is not a root, its transform is
     * its local transform combined to the transform of its parent
     */
    protected void computeTransform() {
        if (this.isRoot()) {
            this.transform.set(this.localTransform);
        } else {
            final Transform parentTransform = this.parent.getTransform();
            Transform.transform(this.localTransform, parentTransform, this.transform);
        }
    }

    public void setParentScene(final Scene scene) {
        this.parentScene = scene;
    }

    public Transform getTransform() {
        return this.transform;
    }

    public Transform getLocalTransform() {
        return this.localTransform;
    }

    public String getId() {
        return this.id;
    }

    public List<String> getTags() {
        return this.tags;
    }
}
