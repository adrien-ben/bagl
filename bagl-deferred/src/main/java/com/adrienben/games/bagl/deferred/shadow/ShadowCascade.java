package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.opengl.texture.Texture;
import org.joml.Matrix4fc;

/**
 * Shadow cascade data for cascaded shadow mapping.
 *
 * @author adrien
 */
public class ShadowCascade {

    private final float splitValue;
    private final Matrix4fc lightViewProjection;
    private final Texture shadowMap;

    public ShadowCascade(final float splitValue, final Matrix4fc lightViewProjection, final Texture shadowMap) {
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

    public Texture getShadowMap() {
        return shadowMap;
    }
}
