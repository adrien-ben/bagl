package com.adrienben.games.bagl.renderer.shadow;

import com.adrienben.games.bagl.opengl.texture.Texture2D;
import org.joml.Matrix4fc;

/**
 * Shadow cascade data for cascaded shadow mapping.
 *
 * @author adrien
 */
public record ShadowCascade(
        float splitValue,
        Matrix4fc lightViewProjection,
        Texture2D shadowMap
) {
}
