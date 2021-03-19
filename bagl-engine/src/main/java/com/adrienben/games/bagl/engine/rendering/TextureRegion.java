package com.adrienben.games.bagl.engine.rendering;

import com.adrienben.games.bagl.opengl.texture.Texture2D;

/**
 * This class represents the region of a texture. The bottom-left
 * corner of a texture is (0, 0), the top-right corner is (1, 1).
 */
public record TextureRegion(
        Texture2D texture,
        float left,
        float bottom,
        float right,
        float top
) {
}
