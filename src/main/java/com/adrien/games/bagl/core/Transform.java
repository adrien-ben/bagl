package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;

/**
 * A transform represents a translation, rotation and scaling in space.
 */
public class Transform {

    private Vector3 translation;
    private Quaternion rotation;
    private Vector3 scale;
    private final Matrix4 transform;
    private boolean isDirty;

    private final Matrix4 transBuff;
    private final Matrix4 rotBuff;
    private final Matrix4 scaleBuff;

    public Transform() {
        this.translation = new Vector3();
        this.rotation = new Quaternion(1, 0, 0, 0);
        this.scale = new Vector3(1, 1, 1);
        this.transform = Matrix4.createIdentity();
        this.isDirty = false;

        this.transBuff = Matrix4.createZero();
        this.rotBuff = Matrix4.createZero();
        this.scaleBuff = Matrix4.createZero();
    }

    /**
     * Transforms this transform relatively to another transform.
     * This transform is 'sent' to the space of the other transform.
     *
     * @param transform The transform to apply.
     * @return This for chaining.
     */
    public Transform transform(Transform transform) {
        final Matrix4 tm = transform.getTransformMatrix();
        this.translation.transform(tm, 1);
        this.scale.transform(tm, 0);
        this.rotation.mul(transform.getRotation());
        this.isDirty = true;
        return this;
    }

    /**
     * Transforms a transform relatively to another transform.
     * The transform <code>toTransform</code> is 'sent' to the
     * space of the other transform.
     *
     * @param toTransform The transform to apply a transform to. Will not be changed.
     * @param transform   The transform to apply. Will not be changed.
     * @param result      The transform where to store the result.
     */
    public static void transform(Transform toTransform, Transform transform, Transform result) {
        final Matrix4 tm = transform.getTransformMatrix();
        Vector3.transform(tm, toTransform.translation, 1, result.translation);
        Vector3.mul(toTransform.scale, transform.scale, result.scale);
        Quaternion.mul(toTransform.rotation, transform.rotation, result.rotation);
        result.isDirty = true;
    }

    private void computeTransform() {
        this.transBuff.setTranslation(this.translation);
        this.rotBuff.setRotation(this.rotation);
        this.scaleBuff.setScale(this.scale);

        Matrix4.mul(this.transBuff, this.rotBuff, this.transform);
        Matrix4.mul(this.transform, this.scaleBuff, this.transform);

        this.isDirty = false;
    }

    /**
     * Copies the values of another transform.
     *
     * @param other The transform to copy.
     * @return This form chaining.
     */
    public Transform set(Transform other) {
        this.translation.set(other.translation);
        this.rotation.set(other.rotation);
        this.scale.set(other.scale);
        this.isDirty = true;
        return this;
    }

    /**
     * Returns the matrix of the transform.
     * <p>This matrix contains the combination of translation,
     * rotation and scaling. The matrix is computed here if
     * needed.
     *
     * @return A matrix.
     */
    public Matrix4 getTransformMatrix() {
        if (this.isDirty) {
            this.computeTransform();
        }
        return this.transform;
    }

    public Vector3 getTranslation() {
        return translation;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public Vector3 getScale() {
        return scale;
    }

    /**
     * Sets the translation. Flags the transform as dirty
     * so the transform matrix is recomputed when needed.
     *
     * @param translation The translation vector to set.
     * @return This for chaining.
     */
    public Transform setTranslation(Vector3 translation) {
        this.translation = translation;
        this.isDirty = true;
        return this;
    }

    /**
     * Sets the rotation. Flags the transform as dirty
     * so the transform matrix is recomputed when needed.
     *
     * @param rotation The rotation to set.
     * @return This for chaining.
     */
    public Transform setRotation(Quaternion rotation) {
        this.rotation = rotation;
        this.isDirty = true;
        return this;
    }

    /**
     * Sets the scaling. Flags the transform as dirty
     * so the transform matrix is recomputed when needed.
     *
     * @param scale The scale vector to set.
     * @return This for chaining.
     */
    public Transform setScale(Vector3 scale) {
        this.scale = scale;
        this.isDirty = true;
        return this;
    }

}
