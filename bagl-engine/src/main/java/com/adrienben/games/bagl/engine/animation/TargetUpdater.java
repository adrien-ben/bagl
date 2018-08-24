package com.adrienben.games.bagl.engine.animation;

import com.adrienben.games.bagl.engine.Transform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Define a function that update some data of a target.
 *
 * @param <T> The type of the target.
 * @param <U> The type the the data to set.
 */
@FunctionalInterface
public interface TargetUpdater<T, U> {

    /**
     * Define a {@link Transform} translation updater.
     *
     * @return a new {@link TargetUpdater}.
     */
    static TargetUpdater<Transform, Vector3f> transformTranslationUpdater() {
        return Transform::setTranslation;
    }

    /**
     * Define a {@link Transform} rotation updater.
     *
     * @return a new {@link TargetUpdater}.
     */
    static TargetUpdater<Transform, Quaternionf> transformRotationUpdater() {
        return Transform::setRotation;
    }

    /**
     * Define a {@link Transform} scale updater.
     *
     * @return a new {@link TargetUpdater}.
     */
    static TargetUpdater<Transform, Vector3f> translationScaleUpdater() {
        return Transform::setScale;
    }

    /**
     * Update {@code target} with {@code data}.
     *
     * @param target The target to update.
     * @param data   The data to update target with.
     */
    void update(T target, U data);
}
