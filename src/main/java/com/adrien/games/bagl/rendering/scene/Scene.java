package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.scene.components.ObjectComponent;
import com.adrien.games.bagl.rendering.texture.Cubemap;

import java.util.*;
import java.util.stream.Stream;

/**
 * 3D scene
 * <p>
 * Contains a {@link Component}'s graph. You can add components as follows:
 * <pre>
 * final Scene scene = new Scene();
 * scene.getRoot().addChild(...);
 * </pre>
 *
 * @author adrien
 */
public class Scene {

    private final Map<String, Component> componentsById;
    private final Map<String, List<Component>> componentsByTag;
    private final Map<Class<? extends Component>, List<Component>> componentsByType;
    private final Component root;

    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;

    /**
     * Construct an empty scene with a empty root component
     */
    public Scene() {
        this.root = new ObjectComponent("root", "root");
        this.root.setParentScene(this);
        this.componentsById = new HashMap<>();
        this.componentsByTag = new HashMap<>();
        this.componentsByType = new HashMap<>();
        this.environmentMap = null;
        this.irradianceMap = null;
        this.preFilteredMap = null;
    }

    /**
     * Update the scene
     * <p>
     * This method calls the root component's update method
     *
     * @param time The program time
     */
    public void update(final Time time) {
        this.root.update(time);
    }

    /**
     * Add a new component to the scene
     * <p>
     * This method should not be called by end users. It is call from the component when
     * a child is added. It it used to store components by id and tag so we can retrieve
     * it by calling {@link Scene#getComponentById(String)} and
     * {@link Scene#getComponentsByTag(String)} without the need to traverse the whole
     * graph
     *
     * @param component The component to add
     */
    public void addComponent(final Component component) {
        final String id = component.getId();
        if (this.componentsById.containsKey(id)) {
            throw new EngineException("The scene already contains a component with id " + id);
        }
        this.componentsById.put(id, component);

        component.getTags().forEach(tag -> {
            final List<Component> components = this.componentsByTag.computeIfAbsent(tag, key -> new ArrayList<>());
            components.add(component);
        });

        final Class<? extends Component> type = component.getClass();
        final List<Component> components = this.componentsByType.computeIfAbsent(type, key -> new ArrayList<>());
        components.add(component);
    }

    /**
     * Get a component by its id if it exists in the scene
     *
     * @param id The id of the component to retrieve
     * @return A {@link Optional} wrapping a {@link Component}
     */
    public Optional<Component> getComponentById(final String id) {
        return Optional.ofNullable(this.componentsById.get(id));
    }

    /**
     * Get a stream of components by their tag
     *
     * @param tag The tag of the components to retrieve
     * @return A stream of {@link Component} (may be empty)
     */
    public Stream<Component> getComponentsByTag(final String tag) {
        final List<Component> components = this.componentsByTag.get(tag);
        return Objects.isNull(components) ? Stream.empty() : components.stream();
    }

    /**
     * Get a stream of component by their type
     *
     * @param type The type of components to retrieve
     * @return A stream of {@link Component} (may be empty)
     */
    public <T extends Component> Stream<T> getComponentsByType(final Class<T> type) {
        final List<Component> components = this.componentsByType.get(type);
        return Objects.isNull(components) ? Stream.empty() : components.stream().map(type::cast);
    }

    public Optional<Cubemap> getEnvironmentMap() {
        return Optional.ofNullable(this.environmentMap);
    }

    public void setEnvironmentMap(final Cubemap environmentMap) {
        this.environmentMap = environmentMap;
    }

    public Optional<Cubemap> getIrradianceMap() {
        return Optional.ofNullable(this.irradianceMap);
    }

    public void setIrradianceMap(final Cubemap irradianceMap) {
        this.irradianceMap = irradianceMap;
    }

    public Optional<Cubemap> getPreFilteredMap() {
        return Optional.ofNullable(this.preFilteredMap);
    }

    public void setPreFilteredMap(final Cubemap preFilteredMap) {
        this.preFilteredMap = preFilteredMap;
    }

    public Component getRoot() {
        return this.root;
    }
}
