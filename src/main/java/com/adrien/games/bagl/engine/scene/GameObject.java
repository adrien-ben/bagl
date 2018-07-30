package com.adrien.games.bagl.engine.scene;

import com.adrien.games.bagl.engine.Time;
import com.adrien.games.bagl.engine.Transform;

import java.util.*;

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
 * Each game object also has a list of components. An object can only
 * have one component by type at most. The game object is responsible
 * for updating its components. It is also responsible for forwarding
 * visit request coming from a {@link ComponentVisitor}
 * <p>
 * A game object can be disabled. When it is disabled its {@link GameObject#update(Time)}
 * and {@link GameObject#accept(ComponentVisitor)} methods have not effect. So its derived
 * transform won't be computed, and its components not updated nor visited. Then a
 * {@link ComponentVisitor} won't be able to 'see' the components of a disabled
 * game object. Children of a disabled object are considered disabled too. By default
 * game objects are enabled.
 * <p>
 * You still can add children and components to a disabled object. But they won't be
 * processed until the object is enabled again. You can also call {@link GameObject#destroy()}
 * on a disabled game object.
 *
 * @author adrien
 */
public class GameObject {

    private final String id;
    private final Set<String> tags;
    private boolean enabled;
    private final Transform localTransform;
    private final Transform transform;
    private Scene parentScene;
    private GameObject parent;
    private final List<GameObject> children;
    private final Map<Class<? extends Component>, Component> componentsByType;

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
        this.tags = Set.of(tags);
        this.enabled = true;
        this.localTransform = new Transform();
        this.transform = new Transform();
        this.parentScene = parent;
        this.parentScene.storeObject(this);
        this.parent = null;
        this.children = new ArrayList<>();
        this.componentsByType = new HashMap<>();
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
        if (!enabled) {
            return;
        }
        this.computeTransform();
        this.componentsByType.values().forEach(component -> component.update(time));
        this.children.forEach(child -> child.update(time));
    }

    /**
     * Compute the derived transform of this object by transforming
     * its local transform by the transform of its parent or by copying
     * its local transform if the object has no parent
     */
    private void computeTransform() {
        if (this.isRoot()) {
            this.transform.set(this.localTransform);
        } else {
            final var parentTransform = this.parent.transform;
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
        if (!enabled) {
            return;
        }
        this.componentsByType.values().forEach(component -> component.accept(visitor));
        this.children.forEach(child -> child.accept(visitor));
    }

    /**
     * Destroy this game object
     * <p>
     * Destroy all of its children first, then destroy all its components and
     * finally detaches forget about its parent.
     */
    public void destroy() {
        children.forEach(GameObject::destroy);
        children.clear();
        componentsByType.values().forEach(Component::destroy);
        componentsByType.clear();
        if (!isRoot()) {
            parentScene = null;
            parent = null;
        }
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
        final var child = new GameObject(this.parentScene, id, tags);
        this.children.add(child);
        child.parent = this;
        child.parentScene = this.parentScene;
        return child;
    }

    /**
     * Add a component to this object if there already was a component
     * of the same type, the old component will be replaced and its
     * parent object set to null
     *
     * @param component The component to add
     */
    public void addComponent(final Component component) {
        final var old = this.componentsByType.put(component.getClass(), component);
        component.setParentObject(this);
        if (Objects.nonNull(old)) {
            old.setParentObject(null);
        }
    }

    /**
     * Has this object a parent ?
     *
     * @return true if this is root of a graph
     */
    private boolean isRoot() {
        return Objects.isNull(this.parent);
    }

    /**
     * Retrieve a component by its type
     *
     * @param type The type of the component to retrieve
     * @return A component of type {@code T} or an empty optional
     */
    public <T extends Component> Optional<T> getComponentOfType(final Class<T> type) {
        return Optional.ofNullable(this.componentsByType.get(type)).map(type::cast);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public Set<String> getTags() {
        return this.tags;
    }
}
