package com.adrienben.games.bagl.engine;

import com.adrienben.games.bagl.core.utils.Dirtiable;
import org.joml.*;

/**
 * A transform represents a translation, rotation and scaling in space
 *
 * @author adrien
 */
public class Transform {

    private final Vector3f translation;
    private final Quaternionf rotation;
    private final Vector3f scale;
    private final Dirtiable<Matrix4f> transform;

    private final Vector3f buffer;

    /**
     * Construct a transform
     * <p>
     * By default their is no translation nor rotation and scale
     * is set to (1, 1, 1)
     */
    public Transform() {
        this.translation = new Vector3f();
        this.rotation = new Quaternionf();
        this.scale = new Vector3f(1, 1, 1);
        this.transform = new Dirtiable<>(new Matrix4f(), this::computeTransform);
        this.buffer = new Vector3f();
    }

    /**
     * Transform this transform relatively to another transform.
     * This transform is 'sent' to the space of the other transform
     *
     * @param transform The transform to apply
     * @return This for chaining
     */
    public Transform transform(final Transform transform) {
        Transform.transform(this, transform, this);
        return this;
    }

    /**
     * Transform a transform relatively to another transform.
     * The transform <code>toTransform</code> is 'sent' to the
     * space of the other transform
     *
     * @param toTransform The transform to apply a transform to. Will not be changed
     * @param transform   The transform to apply. Will not be changed
     * @param result      The transform where to store the result
     */
    public static void transform(final Transform toTransform, final Transform transform, final Transform result) {
        final var tm = transform.getTransformMatrix();
        toTransform.translation.mulPosition(tm, result.translation);
        toTransform.scale.mul(transform.scale, result.scale);
        toTransform.rotation.mul(transform.rotation, result.rotation);
        result.transform.dirty();
    }

    /**
     * Transform the given {@link AABBf} and store thr result into {@code destination}.
     * <p>
     * {@code toTransform} is scaled then moved but not rotated. The method returns {@code destination}.
     */
    public AABBf transformAABB(final AABBf toTransform, final AABBf destination) {
        buffer.set(toTransform.minX, toTransform.minY, toTransform.minZ).mul(scale).add(translation);
        destination.setMin(buffer);
        buffer.set(toTransform.maxX, toTransform.maxY, toTransform.maxZ).mul(scale).add(translation);
        destination.setMax(buffer);
        return destination;
    }

    /**
     * Compute the transform matrix
     *
     * @param transform The result of the computation.
     */
    private void computeTransform(final Matrix4f transform) {
        transform.translation(this.translation)
                .rotate(this.rotation)
                .scale(this.scale);
    }

    /**
     * Copy the values of another transform
     *
     * @param other The transform to copy
     * @return This form chaining
     */
    public Transform set(final Transform other) {
        this.translation.set(other.translation);
        this.rotation.set(other.rotation);
        this.scale.set(other.scale);
        this.transform.dirty();
        return this;
    }

    /**
     * Return the matrix of the transform
     * <p>
     * This matrix contains the combination of translation,
     * rotation and scaling. The matrix is computed here if
     * needed
     *
     * @return The transformation matrix
     */
    public Matrix4fc getTransformMatrix() {
        return this.transform.get();
    }

    /**
     * Set the translation. Flags the transform as dirty
     * so the transform matrix is recomputed when needed
     *
     * @param translation The translation vector to set
     * @return This for chaining
     */
    public Transform setTranslation(final Vector3fc translation) {
        this.translation.set(translation);
        this.transform.dirty();
        return this;
    }

    /**
     * Set the rotation. Flags the transform as dirty
     * so the transform matrix is recomputed when needed
     *
     * @param rotation The rotation to set
     * @return This for chaining
     */
    public Transform setRotation(final Quaternionfc rotation) {
        this.rotation.set(rotation);
        this.transform.dirty();
        return this;
    }

    /**
     * Set the scaling. Flags the transform as dirty
     * so the transform matrix is recomputed when needed
     *
     * @param scale The scale vector to set
     * @return This for chaining
     */
    public Transform setScale(final Vector3fc scale) {
        this.scale.set(scale);
        this.transform.dirty();
        return this;
    }

    public Vector3fc getTranslation() {
        return translation;
    }

    public Quaternionfc getRotation() {
        return rotation;
    }

    public Vector3fc getScale() {
        return scale;
    }
}
