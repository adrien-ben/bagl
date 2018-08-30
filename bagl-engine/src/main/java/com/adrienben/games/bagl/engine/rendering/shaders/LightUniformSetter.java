package com.adrienben.games.bagl.engine.rendering.shaders;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.opengl.shader.Shader;

import java.util.List;

/**
 * Class responsible for setting shader uniforms related to lights.
 *
 * @author adrien
 */
public class LightUniformSetter {

    private final Shader shader;

    public LightUniformSetter(final Shader shader) {
        this.shader = shader;
    }

    public void setDirectionalLightsUniforms(final List<DirectionalLight> directionalLights) {
        if (CollectionUtils.isNotEmpty(directionalLights)) {
            setDirectionalLightsCount(directionalLights.size());
            for (var i = 0; i < directionalLights.size(); i++) {
                setDirectionalLightUniforms(i, directionalLights.get(i));
            }
        } else {
            setDirectionalLightsCount(0);
        }
    }

    private void setDirectionalLightsCount(final int count) {
        shader.setUniform("uLights.directionalCount", count);
    }

    private void setDirectionalLightUniforms(final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.directionals[" + index + "].base.color", light.getColor())
                .setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    public void setPointLightsUniforms(final List<PointLight> pointLights) {
        if (CollectionUtils.isNotEmpty(pointLights)) {
            setPointLightsCount(pointLights.size());
            for (var i = 0; i < pointLights.size(); i++) {
                setPointLightUniforms(i, pointLights.get(i));
            }
        } else {
            setPointLightsCount(0);
        }
    }

    private void setPointLightsCount(final int count) {
        shader.setUniform("uLights.pointCount", count);
    }

    private void setPointLightUniforms(final int index, final PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.points[" + index + "].base.color", light.getColor())
                .setUniform("uLights.points[" + index + "].position", light.getPosition())
                .setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    public void setSpotLightsUniforms(final List<SpotLight> spotLights) {
        if (CollectionUtils.isNotEmpty(spotLights)) {
            setSpotLightsCount(spotLights.size());
            for (var i = 0; i < spotLights.size(); i++) {
                setSpotLightUniforms(i, spotLights.get(i));
            }
        } else {
            setSpotLightsCount(0);
        }
    }

    private void setSpotLightsCount(final int count) {
        shader.setUniform("uLights.spotCount", count);
    }

    private void setSpotLightUniforms(final int index, final SpotLight light) {
        shader.setUniform("uLights.spots[" + index + "].point.base.intensity", light.getIntensity())
                .setUniform("uLights.spots[" + index + "].point.base.color", light.getColor())
                .setUniform("uLights.spots[" + index + "].point.position", light.getPosition())
                .setUniform("uLights.spots[" + index + "].point.radius", light.getRadius())
                .setUniform("uLights.spots[" + index + "].direction", light.getDirection())
                .setUniform("uLights.spots[" + index + "].cutOff", light.getCutOff())
                .setUniform("uLights.spots[" + index + "].outerCutOff", light.getOuterCutOff());
    }
}
