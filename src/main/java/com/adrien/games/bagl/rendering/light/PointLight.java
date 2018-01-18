package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import org.joml.Vector3f;

public class PointLight extends Light {

    private Vector3f position;
    private float radius;

    public PointLight(Vector3f position, float radius) {
        super();
        this.position = position;
        this.radius = radius;
    }

    public PointLight(float intensity, Color color, Vector3f position, float radius) {
        super(intensity, color);
        this.position = position;
        this.radius = radius;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

}
