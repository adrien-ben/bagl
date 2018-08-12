package com.adrienben.games.bagl.deferred.shadow;

import java.util.List;

/**
 * Cascaded shadow map data.
 *
 * @author adrien
 */
public class CascadedShadowMap {

    public static final int CASCADE_COUNT = 4;
    public final static List<Float> SPLIT_VALUES = List.of(0.05f, 0.3f, 0.8f, 1.0f);
    public final static List<Integer> SPLIT_RESOLUTIONS = List.of(4096, 2048, 1024, 512);

    private final List<ShadowCascade> shadowCascades;

    public CascadedShadowMap(final List<ShadowCascade> shadowCascades) {
        if (shadowCascades.size() != CASCADE_COUNT) {
            throw new IllegalArgumentException(String.format("The cascaded shadow map requires %d cascades", CASCADE_COUNT));
        }
        this.shadowCascades = shadowCascades;
    }

    public ShadowCascade getShadowCascade(final int cascadeIndex) {
        return shadowCascades.get(cascadeIndex);
    }
}
