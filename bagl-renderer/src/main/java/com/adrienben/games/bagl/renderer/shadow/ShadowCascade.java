package com.adrienben.games.bagl.renderer.shadow;

import com.adrienben.games.bagl.opengl.texture.Texture2D;
import org.joml.Matrix4fc;

/**
 * Shadow cascade data for cascaded shadow mapping.
 *
 * @author adrien
 */
public class ShadowCascade {

    private final float splitValue;
    private final Matrix4fc lightViewProjection;
    private final Texture2D shadowMap;

    public ShadowCascade(final float splitValue, final Matrix4fc lightViewProjection, final Texture2D shadowMap) {
        this.splitValue = splitValue;
        this.lightViewProjection = lightViewProjection;
        this.shadowMap = shadowMap;
    }

    public float getSplitValue() {
        return splitValue;
    }

    public Matrix4fc getLightViewProjection() {
        return lightViewProjection;
    }

    public Texture2D getShadowMap() {
        return shadowMap;
    }
}
