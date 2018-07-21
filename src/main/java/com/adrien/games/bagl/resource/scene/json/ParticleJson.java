package com.adrien.games.bagl.resource.scene.json;

import com.adrien.games.bagl.rendering.BlendMode;

public class ParticleJson {

    private String texture;
    private ColorJson startColor;
    private ColorJson endColor;
    private BlendMode blendMode;
    private float rate;
    private int batchSize;
    private ParticleInitializerJson initializer;

    public String getTexture() {
        return texture;
    }

    public ColorJson getStartColor() {
        return startColor;
    }

    public ColorJson getEndColor() {
        return endColor;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public float getRate() {
        return rate;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public ParticleInitializerJson getInitializer() {
        return initializer;
    }
}
