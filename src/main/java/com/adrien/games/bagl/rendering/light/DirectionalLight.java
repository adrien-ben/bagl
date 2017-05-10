package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.math.Vector3;

public class DirectionalLight extends Light {

    private Vector3 direction;

    public DirectionalLight(Vector3 direction) {
        super();
        this.direction = direction;
    }

    public DirectionalLight(float intensity, Color color, Vector3 direction) {
        super(intensity, color);
        this.direction = direction;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public void setDirection(Vector3 direction) {
        this.direction = direction;
    }

}
