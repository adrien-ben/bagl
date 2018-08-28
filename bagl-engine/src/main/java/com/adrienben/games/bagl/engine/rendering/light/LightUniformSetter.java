package com.adrienben.games.bagl.engine.rendering.light;

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
        shader.setUniform("uLights.directionalCount", directionalLights.size());
        for (var i = 0; i < directionalLights.size(); i++) {
            setDirectionalLightUniforms(i, directionalLights.get(i));
        }
    }

    private void setDirectionalLightUniforms(final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.directionals[" + index + "].base.color", light.getColor())
                .setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    public void setPointLightsUniforms(final List<PointLight> pointLights) {
        shader.setUniform("uLights.pointCount", pointLights.size());
        for (var i = 0; i < pointLights.size(); i++) {
            setPointLightUniforms(i, pointLights.get(i));
        }
    }

    private void setPointLightUniforms(final int index, final PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.points[" + index + "].base.color", light.getColor())
                .setUniform("uLights.points[" + index + "].position", light.getPosition())
                .setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    public void setSpotLightsUniforms(final List<SpotLight> spotLights) {
        shader.setUniform("uLights.spotCount", spotLights.size());
        for (var i = 0; i < spotLights.size(); i++) {
            setSpotLightUniforms(i, spotLights.get(i));
        }
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
