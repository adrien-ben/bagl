package com.adrien.games.bagl.engine.scene;

import com.adrien.games.bagl.engine.Time;

/**
 * Base class for game component
 *
 * @author adrien
 */
public abstract class Component {

    protected GameObject parentObject;

    /**
     * Construct a new game component
     */
    public Component() {
        this.parentObject = null;
    }

    /**
     * Update the component
     *
     * @param time The time of the program
     */
    public abstract void update(final Time time);

    /**
     * Accept a visitor
     *
     * @param visitor THe visitor to accept
     */
    public void accept(final ComponentVisitor visitor) {
    }

    /**
     * Destroy resources linked to this component
     */
    public void destroy() {
    }

    public void setParentObject(final GameObject parentObject) {
        this.parentObject = parentObject;
    }

    public GameObject getParentObject() {
        return this.parentObject;
    }
}
