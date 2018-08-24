package com.adrienben.games.bagl.engine.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Define an interpolation function.
 *
 * @param <T> The type of the date to interpolate.
 * @author adrien
 */
@FunctionalInterface
public interface Interpolator<T> {

    /**
     * Define a {@link Vector3f} linear interpolator.
     *
     * @return a new {@link Interpolator}.
     */
    static Interpolator<Vector3f> vector3fLerp() {
        return Vector3f::lerp;
    }

    /**
     * Define a {@link org.joml.Vector3f} step interpolator.
     *
     * @return a new {@link Interpolator}.
     */
    static Interpolator<Vector3f> vector3fStep() {
        return (start, end, factor, destination) -> destination.set(start);
    }

    /**
     * Define a {@link Quaternionf} linear interpolator.
     *
     * @return a new {@link Interpolator}.
     */
    static Interpolator<Quaternionf> quaternionfSlerp() {
        return Quaternionf::slerp;
    }

    /**
     * Define a {@link Quaternionf} step interpolator.
     *
     * @return a new {@link Interpolator}.
     */
    static Interpolator<Quaternionf> quaternionfStep() {
        return (start, end, factor, destination) -> destination.set(start);
    }

    /**
     * Interpolate between {@code start} and {@code end} using {@code interpolationValue} and store the result in {@code destination}.
     *
     * @param start              The start value.
     * @param end                The end value.
     * @param interpolationValue The interpolation value.
     * @param destination        Where to store the result
     * @implSpec {@code start} and {@code end} must not be modified.
     */
    void interpolate(T start, T end, float interpolationValue, T destination);
}
