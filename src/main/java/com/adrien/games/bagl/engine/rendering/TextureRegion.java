package com.adrien.games.bagl.engine.rendering;

import com.adrien.games.bagl.opengl.texture.Texture;

/**
 * This class represents the region of a texture. The bottom-left
 * corner of a texture is (0, 0), the top-right corner is (1, 1).
 */
public class TextureRegion {

    private final Texture texture;
    private final float left;
    private final float bottom;
    private final float right;
    private final float top;

    public TextureRegion(Texture texture, float left, float bottom, float right, float top) {
        this.texture = texture;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
    }

    public Texture getTexture() {
        return texture;
    }

    public float getLeft() {
        return left;
    }

    public float getBottom() {
        return bottom;
    }

    public float getRight() {
        return right;
    }

    public float getTop() {
        return top;
    }

}
