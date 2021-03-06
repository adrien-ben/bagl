package com.adrienben.games.bagl.engine.rendering.text;

import com.adrienben.games.bagl.engine.rendering.TextureRegion;

public class Glyph {

    private final TextureRegion region;
    private final float xOffset;
    private final float yOffset;
    private final float xAdvance;

    public Glyph(TextureRegion region, float xOffset, float yOffset, float xAdvance) {
        this.region = region;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
    }

    public TextureRegion getRegion() {
        return region;
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
