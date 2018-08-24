package com.adrienben.games.bagl.deferred.shadow;

import java.util.List;

/**
 * Cascaded shadow map data.
 *
 * @author adrien
 */
public class CascadedShadowMap {

    public static final int CASCADE_COUNT = 4;

    private final List<ShadowCascade> shadowCascades;
    private final float zNear;
    private final float zFar;

    public CascadedShadowMap(final float zNear, final float zFar, final List<ShadowCascade> shadowCascades) {
        if (shadowCascades.size() != CASCADE_COUNT) {
            throw new IllegalArgumentException(String.format("The cascaded shadow map requires %d cascades", CASCADE_COUNT));
        }
        this.zNear = zNear;
        this.zFar = zFar;
        this.shadowCascades = shadowCascades;
    }

    public ShadowCascade getShadowCascade(final int cascadeIndex) {
        return shadowCascades.get(cascadeIndex);
    }

    public float getzNear() {
        return zNear;
    }

    public float getzFar() {
        return zFar;
    }
}
