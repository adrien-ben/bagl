package com.adrienben.games.bagl.deferred.shaders;

import com.adrienben.games.bagl.deferred.shaders.uniforms.ShadowUniformSetter;
import com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap;
import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.LightUniformSetter;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.opengl.shader.Shader;

import java.util.List;

/**
 * Wrapper for the deferred shader.
 *
 * @author adrien
 */
public class DeferredShader {

    public static final int COLORS_TEXTURE_CHANNEL = 0;
    public static final int NORMALS_TEXTURE_CHANNEL = 1;
    public static final int EMISSIVE_TEXTURE_CHANNEL = 2;
    public static final int OCCLUSION_TEXTURE_CHANNEL = 3;
    public static final int DEPTH_TEXTURE_CHANNEL = 4;
    public static final int IRRADIANCE_MAP_CHANNEL = 5;
    public static final int PRE_FILTERED_MAP_CHANNEL = 6;
    public static final int BRDF_LOOKUP_CHANNEL = 7;

    private final Shader shader;
    private final ShadowUniformSetter shadowUniformSetter;
    private final LightUniformSetter lightUniformSetter;

    /**
     * Construct the deferred shader and sets the constant uniforms
     */
    public DeferredShader() {
        this.shader = ShaderFactory.createDeferredShader();
        this.shadowUniformSetter = new ShadowUniformSetter(shader);
        this.lightUniformSetter = new LightUniformSetter(shader);
        setTextureChannelsUniforms();
    }

    private void setTextureChannelsUniforms() {
        bind();
        shader.setUniform("uGBuffer.colors", COLORS_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.normals", NORMALS_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.emissive", EMISSIVE_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.occlusion", OCCLUSION_TEXTURE_CHANNEL)
                .setUniform("uGBuffer.depth", DEPTH_TEXTURE_CHANNEL)
                .setUniform("uEnvironment.irradiance", IRRADIANCE_MAP_CHANNEL)
                .setUniform("uEnvironment.preFilteredMap", PRE_FILTERED_MAP_CHANNEL)
                .setUniform("uEnvironment.brdf", BRDF_LOOKUP_CHANNEL);
        shadowUniformSetter.setShadowMapsChannelsUniforms();
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

    public DeferredShader setCSMUniforms(final CascadedShadowMap cascadedShadowMap) {
        shadowUniformSetter.setCSMUniforms(cascadedShadowMap);
        return this;
    }

    public DeferredShader setDirectionalLightsUniforms(final List<DirectionalLight> directionalLights) {
        lightUniformSetter.setDirectionalLightsUniforms(directionalLights);
        return this;
    }

    public DeferredShader setPointLightsUniforms(final List<PointLight> pointLights) {
        lightUniformSetter.setPointLightsUniforms(pointLights);
        return this;
    }

    public DeferredShader setSpotLightsUniforms(final List<SpotLight> spotLights) {
        lightUniformSetter.setSpotLightsUniforms(spotLights);
        return this;
    }
}
