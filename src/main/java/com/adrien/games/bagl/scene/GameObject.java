package com.adrien.games.bagl.scene;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Game object
 * <p>
 * A game object as a transform and a local transform. The
 * transform is computed when {@link GameObject#update(Time)}
 * is call. The transform is computed by transforming the
 * local transform by the transform of the object. The transform
 * should not be modified by the en user
 * <p>
 * A game object has a list of children. Each object is responsible
 * for updating its children. Its also keep a reference to its
 * if any and to its parent {@link Scene}
 * <p>
 * To add a children to a game object call
 * {@link GameObject#createChild(String, String...)}. This ensure
 * that parent object and scene are properly set
 * <p>
 * Each game object also has a list of components. The game object
 * is responsible for updating its components. It is also responsible
 * for forwarding visit request coming from a {@link ComponentVisitor}
 *
 * @author adrien
 */
public class GameObject {

    private final String id;
    private final List<String> tags;
    private final Transform localTransform;
    private final Transform transform;
    private Scene parentScene;
    private GameObject parent;
    private final List<GameObject> children;
    private final List<Component> components;

    /**
     * Construct a game object
     * <p>
     * The transform and local transform of a newly created object
     * as no translation nor rotation and its scale is (1, 1, 1)
     *
     * @param id     The id of the game object
     * @param parent The parent scene of the object
     * @param tags   The tags of the game object
     */
    public GameObject(final Scene parent, final String id, final String... tags) {
        this.id = id;
        this.tags = Arrays.asList(tags);
        this.localTransform = new Transform();
        this.transform = new Transform();
        this.parentScene = parent;
        this.parentScene.storeObject(this);
        this.parent = null;
        this.children = new ArrayList<>();
        this.components = new ArrayList<>();
    }

    /**
     * Update the game object
     * <p>
     * The derived transform of the object is compute and each
     * components of the game object are updated. Then each of
     * its children is updated
     *
     * @param time The time of the program
     */
    public void update(final Time time) {
        this.computeTransform();
        this.components.forEach(component -> component.update(time));
        this.children.forEach(child -> child.update(time));
    }

    /**
     * Compute the derived transform of this object by transforming
     * its local transform by the transform of its parent or by copying
     * its local transform if the object has no parent
     */
    public void computeTransform() {
        if (this.isRoot()) {
            this.transform.set(this.localTransform);
        } else {
            final Transform parentTransform = this.parent.transform;
            Transform.transform(this.localTransform, parentTransform, this.transform);
        }
    }

    /**
     * Accept a component visitor
     * <p>
     * Forward the visit request to its components then to its
     * children
     *
     * @param visitor The visitor
     */
    public void accept(final ComponentVisitor visitor) {
        this.components.forEach(component -> component.accept(visitor));
        this.children.forEach(child -> child.accept(visitor));
    }

    /**
     * Create a new child object
     * <p>
     * This creates a new instance of a game object. It sets this as
     * the parent of the new object and sets the parent scene to be the
     * same as this'
     *
     * @param id   The id of the child
     * @param tags The tags of the child
     * @return The created child
     */
    public GameObject createChild(final String id, final String... tags) {
        final GameObject child = new GameObject(this.parentScene, id, tags);
        this.children.add(child);
        child.parent = this;
        child.parentScene = this.parentScene;
        return child;
    }

    /**
     * Add a component to this object
     *
     * @param component The component to add
     */
    public void addComponent(final Component component) {
        this.components.add(component);
        component.setParentObject(this);
    }

    /**
     * Has this object a parent ?
     *
     * @return true if this is root of a graph
     */
    private boolean isRoot() {
        return Objects.isNull(this.parent);
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
