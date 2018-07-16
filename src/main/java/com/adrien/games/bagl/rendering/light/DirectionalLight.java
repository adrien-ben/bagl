package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import org.joml.Vector3f;

public class DirectionalLight extends Light {

    private Vector3f direction;

    public DirectionalLight(Vector3f direction) {
        super();
        this.direction = direction;
    }

    public DirectionalLight(float intensity, Color color, Vector3f direction) {
        super(intensity, color);
        this.direction = direction;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

}
