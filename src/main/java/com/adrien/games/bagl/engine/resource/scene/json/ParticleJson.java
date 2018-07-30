package com.adrien.games.bagl.engine.resource.scene.json;

import com.adrien.games.bagl.opengl.BlendMode;

public class ParticleJson {

    private String texturePath;
    private String textureId;
    private ColorJson startColor;
    private ColorJson endColor;
    private BlendMode blendMode;
    private float rate;
    private int batchSize;
    private ParticleInitializerJson initializer;

    public String getTexturePath() {
        return texturePath;
    }

    public String getTextureId() {
        return textureId;
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
