package com.adrienben.games.bagl.deferred.shaders;

import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.opengl.shader.Shader;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * Wrapper for the deferred shader.
 *
 * @author adrien
 */
public class DeferredShader {

    private static final int COLORS_TEXTURE_CHANNEL = 0;
    private static final int NORMALS_TEXTURE_CHANNEL = 1;
    private static final int EMISSIVE_TEXTURE_CHANNEL = 2;
    private static final int DEPTH_TEXTURE_CHANNEL = 3;
    private static final int SHADOW_MAP_CHANNEL = 4;
    private static final int IRRADIANCE_MAP_CHANNEL = 5;
    private static final int PRE_FILTERED_MAP_CHANNEL = 6;
    private static final int BRDF_LOOKUP_CHANNEL = 7;

    private final Shader shader;

    /**
     * Construct the deferred shader and sets the constant uniforms
     */
    public DeferredShader() {
        this.shader = ShaderFactory.createDeferredShader();
        bind();
        this.shader.setUniform("uGBuffer.colors", COLORS_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.normals", NORMALS_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.emissive", EMISSIVE_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.depth", DEPTH_TEXTURE_CHANNEL)
                .setUniform("uShadow.shadowMap", SHADOW_MAP_CHANNEL)
                .setUniform("uLights.irradiance", IRRADIANCE_MAP_CHANNEL)
                .setUniform("uLights.preFilteredMap", PRE_FILTERED_MAP_CHANNEL)
                .setUniform("uLights.brdf", BRDF_LOOKUP_CHANNEL);
        Shader.unbind();
    }

    public void destroy() {
        shader.destroy();
    }

    public DeferredShader bind() {
        shader.bind();
        return this;
    }

    public DeferredShader setCameraUniforms(final Camera camera) {
        shader.setUniform("uCamera.invertedViewProj", camera.getInvertedViewProj())
                .setUniform("uCamera.position", camera.getPosition());
        return this;
    }

    public DeferredShader setHasShadowUniform(final boolean hasShadow) {
        shader.setUniform("uShadow.hasShadow", hasShadow);
        return this;
    }

    public DeferredShader setShadowCasterViewProjectionMatrix(final Matrix4fc shadowCasterViewProjectionMatrix) {
        shader.setUniform("uShadow.lightViewProj", shadowCasterViewProjectionMatrix);
        return this;
    }

    public DeferredShader setDirectionalLightsUniforms(final List<DirectionalLight> directionalLights) {
        shader.setUniform("uLights.directionalCount", directionalLights.size());
        for (var i = 0; i < directionalLights.size(); i++) {
            setDirectionalLightUniforms(i, directionalLights.get(i));
        }
        return this;
    }

    private void setDirectionalLightUniforms(final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.directionals[" + index + "].base.color", light.getColor())
                .setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    public DeferredShader setPointLightsUniforms(final List<PointLight> pointLights) {
        shader.setUniform("uLights.pointCount", pointLights.size());
        for (var i = 0; i < pointLights.size(); i++) {
            setPointLightUniforms(i, pointLights.get(i));
        }
        return this;
    }

    private void setPointLightUniforms(final int index, final PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.points[" + index + "].base.color", light.getColor())
                .setUniform("uLights.points[" + index + "].position", light.getPosition())
                .setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    public DeferredShader setSpotLightsUniforms(final List<SpotLight> spotLights) {
        shader.setUniform("uLights.spotCount", spotLights.size());
        for (var i = 0; i < spotLights.size(); i++) {
            setSpotLightUniforms(i, spotLights.get(i));
        }
        return this;
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
