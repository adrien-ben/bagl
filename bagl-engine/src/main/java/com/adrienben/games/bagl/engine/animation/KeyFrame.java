package com.adrienben.games.bagl.engine.animation;

/**
 * Represent a value at a given time.
 *
 * @param <T> The type of the value
 * @author adrien
 */
public record KeyFrame<T>(float time, T value) {
}
