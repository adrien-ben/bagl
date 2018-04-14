package com.adrien.games.bagl.core.camera;

import com.adrien.games.bagl.utils.Dirtiable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Two-dimensional camera. Can be use for 2D rendering like sprite or UI
 *
 * @author Adrien
 */
public class Camera2D {

    private final Vector2f position;
    private final int width;
    private final int height;

    private final Dirtiable<Matrix4f> orthographic;

    /**
     * Construct a 2D camera
     *
     * @param position The position of the camera
     * @param width    The width of the camera
     * @param height   The height of the camera
     */
    public Camera2D(final Vector2fc position, final int width, final int height) {
        this.position = new Vector2f(position);
        this.width = width;
        this.height = height;
        this.orthographic = new Dirtiable<>(new Matrix4f(), this::computeOrthographic);
        this.computeOrthographic(this.orthographic.get());
    }

    /**
     * Compute the orthographic projection matrix
     */
    private void computeOrthographic(final Matrix4f orthographic) {
        final var left = Math.round(position.x()) - this.width / 2;
        final var bottom = Math.round(position.y()) - this.height / 2;
        orthographic.setOrtho2D(left, left + this.width, bottom, bottom + this.height);
    }

    /**
     * Translate the camera
     *
     * @param translation The translation to apply
     */
    public void translate(final Vector2fc translation) {
        this.position.add(translation);
        this.orthographic.dirty();
    }

    public Vector2fc getPosition() {
        return this.position;
    }

    public void setPosition(final Vector2fc position) {
        this.position.set(position);
        this.orthographic.dirty();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Matrix4fc getOrthographic() {
        return this.orthographic.get();
    }
}
