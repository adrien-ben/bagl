package com.adrienben.games.bagl.deferred.shaders.uniforms;

import com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap;
import com.adrienben.games.bagl.deferred.shadow.ShadowCascade;
import com.adrienben.games.bagl.opengl.shader.Shader;

import java.util.Objects;

/**
 * Class responsible for setting shader uniforms related to shadows.
 *
 * @author adrien
 */
public class ShadowUniformSetter {

    public static final int SHADOW_MAP_0_CHANNEL = 8;

    private final Shader shader;

    public ShadowUniformSetter(final Shader shader) {
        this.shader = shader;
    }

    public void setShadowMapsChannelsUniforms() {
        for (int i = 0; i < CascadedShadowMap.CASCADE_COUNT; i++) {
            shader.setUniform(String.format("uShadow.shadowCascades[%d].shadowMap", i), SHADOW_MAP_0_CHANNEL + i);
        }
    }

    public void setCSMUniforms(final CascadedShadowMap cascadedShadowMap) {
        shader.setUniform("uShadow.hasShadow", Objects.nonNull(cascadedShadowMap));
        if (Objects.nonNull(cascadedShadowMap)) {
            shader.setUniform("uShadow.zNear", cascadedShadowMap.getzNear())
                    .setUniform("uShadow.zFar", cascadedShadowMap.getzFar());
            for (int i = 0; i < CascadedShadowMap.CASCADE_COUNT; i++) {
                setShadowCascadeUniforms(i, cascadedShadowMap.getShadowCascade(i));
            }
        }
    }

    private void setShadowCascadeUniforms(final int shadowCascadeIndex, final ShadowCascade shadowCascade) {
        final var shadowCascadeUniformBaseName = String.format("uShadow.shadowCascades[%d]", shadowCascadeIndex);
        shader.setUniform(shadowCascadeUniformBaseName + ".lightViewProj", shadowCascade.getLightViewProjection());
        shader.setUniform(shadowCascadeUniformBaseName + ".splitValue", shadowCascade.getSplitValue());
        shadowCascade.getShadowMap().bind(SHADOW_MAP_0_CHANNEL + shadowCascadeIndex);
    }
}
