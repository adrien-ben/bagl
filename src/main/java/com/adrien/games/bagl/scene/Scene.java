package com.adrien.games.bagl.scene;

import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.core.Time;

import java.util.*;
import java.util.stream.Stream;

/**
 * Game scene
 * <p>
 * A scene holds the root of a graph of {@link GameObject}
 * <p>
 * TODO: object/component retrieval
 *
 * @author adrien
 */
public class Scene {

    private final GameObject root;
    private final Map<String, GameObject> objectsById;
    private final Map<String, List<GameObject>> objectsByTags;

    /**
     * Construct a new scene
     * <p>
     * Create the root of the graph. The id of the root is 'root"
     */
    public Scene() {
        this.objectsById = new HashMap<>();
        this.objectsByTags = new HashMap<>();
        this.root = new GameObject(this, "root");
    }

    /**
     * Store a game object
     * <p>
     * This is used for fast retrieval of the game objects
     * <p>
     * This method should NOT be called by end users
     *
     * @param object The object to store
     * @throws EngineException If there is a duplicated id
     */
    public void storeObject(final GameObject object) {
        final String id = object.getId();
        if (this.objectsById.containsKey(id)) {
            throw new EngineException("The id " + id + " is already used in this scene");
        }
        this.objectsById.put(id, object);

        object.getTags().forEach(tag -> this.objectsByTags.computeIfAbsent(tag, key -> new ArrayList<>()).add(object));
    }

    /**
     * Update the scene
     * <p>
     * Update the root of the game object graph which will update
     * its children
     *
     * @param time The time of the program
     */
    public void update(final Time time) {
        this.root.update(time);
    }

    /**
     * Accept a component visitor
     * <p>
     * This method forward the visit request to the root of the
     * graph which will forward the call to its component and then
     * to its children
     *
     * @param visitor The visitor
     */
    public void accept(final ComponentVisitor visitor) {
        this.root.accept(visitor);
    }

    /**
     * Retrieve a game object by its id
     *
     * @param id The id of the object to find
     * @return The {@link GameObject} if found or an empty optional
     */
    public Optional<GameObject> getObjectById(final String id) {
        return Optional.ofNullable(this.objectsById.get(id));
    }

    /**
     * Retrieve game objects by their tag
     *
     * @param tag The tag to look for
     * @return A stream of {@link GameObject}
     */
    public Stream<GameObject> getObjectsByTag(final String tag) {
        final List<GameObject> gameObjects = this.objectsByTags.get(tag);
        return Objects.isNull(gameObjects) ? Stream.empty() : gameObjects.stream();
    }

    public GameObject getRoot() {
        return this.root;
    }
}
