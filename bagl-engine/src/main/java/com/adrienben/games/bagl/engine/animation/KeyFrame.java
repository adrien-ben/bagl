package com.adrienben.games.bagl.engine.animation;

/**
 * Represent a value at a given time.
 *
 * @param <T> The type of the value
 * @author adrien
 */
public class KeyFrame<T> {

    private final float time;
    private final T value;

    public KeyFrame(float time, T value) {
        this.time = time;
        this.value = value;
    }

    public float getTime() {
        return time;
    }

    public T getValue() {
        return value;
    }
}
