package com.adrien.games.bagl.scene;

import com.adrien.games.bagl.core.Time;

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

    /**
     * Construct a new scene
     * <p>
     * Create the root of the graph. The id of the root is 'root"
     */
    public Scene() {
        this.root = new GameObject(this, "root");
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
     * @param visitor
     */
    public void accept(final ComponentVisitor visitor) {
        this.root.accept(visitor);
    }

    public GameObject getRoot() {
        return this.root;
    }
}
