package com.adrien.games.bagl.core;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A transform represents a translation, rotation and scaling in space
 *
 * @author adrien
 */
public class Transform {

    private Vector3f translation;
    private Quaternionf rotation;
    private Vector3f scale;
    private final Matrix4f transform;
    private boolean isDirty;

    /**
     * Construct a transform
     * <p>
     * By default their is no translation nor rotation and scale
     * is set to (1, 1, )
     */
    public Transform() {
        this.translation = new Vector3f();
        this.rotation = new Quaternionf();
        this.scale = new Vector3f(1, 1, 1);
        this.transform = new Matrix4f();
        this.isDirty = false;
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
        final Matrix4f tm = transform.getTransformMatrix();
        toTransform.translation.mulPosition(tm, result.translation);
        toTransform.scale.mul(transform.scale, result.scale);
        toTransform.rotation.mul(transform.rotation, result.rotation);
        result.isDirty = true;
    }

    /**
     * Compute the transform matrix
     */
    private void computeTransform() {
        this.transform.translation(this.translation)
                .rotate(this.rotation)
                .scale(this.scale);
        this.isDirty = false;
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
        this.isDirty = true;
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
    public Matrix4f getTransformMatrix() {
        if (this.isDirty) {
            this.computeTransform();
        }
        return this.transform;
    }

    /**
     * Set the translation. Flags the transform as dirty
     * so the transform matrix is recomputed when needed
     *
     * @param translation The translation vector to set
     * @return This for chaining
     */
    public Transform setTranslation(final Vector3f translation) {
        this.translation = translation;
        this.isDirty = true;
        return this;
    }

    /**
     * Set the rotation. Flags the transform as dirty
     * so the transform matrix is recomputed when needed
     *
     * @param rotation The rotation to set
     * @return This for chaining
     */
    public Transform setRotation(final Quaternionf rotation) {
        this.rotation = rotation;
        this.isDirty = true;
        return this;
    }

    /**
     * Set the scaling. Flags the transform as dirty
     * so the transform matrix is recomputed when needed
     *
     * @param scale The scale vector to set
     * @return This for chaining
     */
    public Transform setScale(final Vector3f scale) {
        this.scale = scale;
        this.isDirty = true;
        return this;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }
}
