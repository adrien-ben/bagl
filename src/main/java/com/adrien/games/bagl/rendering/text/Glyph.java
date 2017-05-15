package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.rendering.texture.TextureRegion;

public class Glyph {

    private final TextureRegion region;
    private final float xOffset;
    private final float yOffset;
    private final float xAdvance;
    private final char value;

    public Glyph(TextureRegion region, float xOffset, float yOffset, float xAdvance, char value) {
        this.region = region;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
        this.value = value;
    }

    @Override
    public String toString() {
        return "char : " + this.value;
    }

    public TextureRegion getRegion() {
        return region;
    }

    public char getValue() {
        return value;
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public float getXAdvance() {
        return xAdvance;
    }

}
