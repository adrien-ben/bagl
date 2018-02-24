package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class PointLight extends Light {

    private final Vector3f position;
    private float radius;

    public PointLight(final Vector3fc position, final float radius) {
        super();
        this.position = new Vector3f(position);
        this.radius = radius;
    }

    public PointLight(final float intensity, final Color color, final Vector3f position, final float radius) {
        super(intensity, color);
        this.position = position;
        this.radius = radius;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getRadius() {
        return radius;
    }

    public void setPosition(Vector3fc position) {
        this.position.set(position);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
