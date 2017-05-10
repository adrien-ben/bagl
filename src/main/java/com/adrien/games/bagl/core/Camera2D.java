package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector2;

/**
 * Two-dimensional camera. Can be use for 2D rendering like sprite or UI.
 * @author Adrien
 *
 */
public class Camera2D {

    private final Vector2 position;
    private final int width;
    private final int height;

    private final Matrix4 orthographic;
    private boolean dirty;

    public Camera2D(Vector2 position, int width, int height) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.orthographic = Matrix4.createIdentity();
        this.computeOrthographic();
    }

    private void computeOrthographic() {
        int left = Math.round(position.getX()) - this.width/2;
        int bottom = Math.round(position.getY()) - this.height/2;
        this.orthographic.setOrthographic(left, left + this.width, bottom, bottom + this.height);
        this.dirty = false;
    }

    public void translate(Vector2 direction) {
        this.position.add(direction);
        this.dirty = true;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public void setPosition(Vector2 position) {
        this.position.setX(position.getX());
        this.position.setY(position.getY());
        this.dirty = true;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Matrix4 getOrthographic() {
        if(this.dirty) {
            this.computeOrthographic();
        }
        return this.orthographic;
    }

}
