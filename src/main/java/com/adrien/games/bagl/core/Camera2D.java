package com.adrien.games.bagl.core;

import org.joml.Matrix4f;
import org.joml.Vector2f;

/**
 * Two-dimensional camera. Can be use for 2D rendering like sprite or UI
 *
 * @author Adrien
 */
public class Camera2D {

    private final Vector2f position;
    private final int width;
    private final int height;

    private final Matrix4f orthographic;
    private boolean dirty;

    /**
     * Construct a 2D camera
     *
     * @param position The position of the camera
     * @param width    The width of the camera
     * @param height   The height of the camera
     */
    public Camera2D(final Vector2f position, final int width, final int height) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.orthographic = new Matrix4f();
        this.computeOrthographic();
    }

    /**
     * Compute the orthographic projection matrix
     */
    private void computeOrthographic() {
        final int left = Math.round(position.x()) - this.width / 2;
        final int bottom = Math.round(position.y()) - this.height / 2;
        this.orthographic.setOrtho2D(left, left + this.width, bottom, bottom + this.height);
        this.dirty = false;
    }

    /**
     * Translate the camera
     *
     * @param translation The translation to apply
     */
    public void translate(final Vector2f translation) {
        this.position.add(translation);
        this.dirty = true;
    }

    public Vector2f getPosition() {
        return this.position;
    }

    public void setPosition(final Vector2f position) {
        this.position.set(position.x(), position.y());
        this.dirty = true;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Matrix4f getOrthographic() {
        if (this.dirty) {
            this.computeOrthographic();
        }
        return this.orthographic;
    }
}
