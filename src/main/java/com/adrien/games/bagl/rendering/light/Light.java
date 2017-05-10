package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;

public class Light {

    private float intensity;
    private Color color;

    public Light() {
        this(1, Color.WHITE);
    }

    public Light(float intensity) {
        this(intensity, Color.WHITE);
    }

    public Light(float intensity, Color color) {
        this.intensity = intensity;
        this.color = color;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
