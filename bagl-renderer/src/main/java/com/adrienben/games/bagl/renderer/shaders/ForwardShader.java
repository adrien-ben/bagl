package com.adrienben.games.bagl.renderer.shaders;

import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.LightUniformSetter;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.material.MaterialUniformSetter;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.renderer.shaders.uniforms.ShadowUniformSetter;
import com.adrienben.games.bagl.renderer.shaders.uniforms.SkinningUniformsSetter;
import com.adrienben.games.bagl.renderer.shadow.CascadedShadowMap;
import org.joml.Matrix4fc;

import java.util.List;

import static com.adrienben.games.bagl.engine.rendering.material.MaterialUniformSetter.*;
import static com.adrienben.games.bagl.renderer.shaders.DeferredShader.*;

/**
 * Wrapper class for the forward rendering shader.
 *
 * @author adrien.
 */
public class ForwardShader {

    private final Shader shader;
    private final SkinningUniformsSetter skinningUniformsSetter;
    private final MaterialUniformSetter materialUniformSetter;
    private final ShadowUniformSetter shadowUniformSetter;
    private final LightUniformSetter lightUniformSetter;

    public ForwardShader() {
        this.shader = ShaderFactory.createForwardShader();
        this.skinningUniformsSetter = new SkinningUniformsSetter(shader);
        this.materialUniformSetter = new MaterialUniformSetter(shader);
        this.shadowUniformSetter = new ShadowUniformSetter(shader);
        this.lightUniformSetter = new LightUniformSetter(shader);
        setTextureChannelsUniforms();
    }

    private void setTextureChannelsUniforms() {
        bind();
        materialUniformSetter.setDiffuseMapChannelUniform();
        materialUniformSetter.setEmissiveMapChannelUniform();
        materialUniformSetter.setRoughnessMetallicMapChannelUniform();
        materialUniformSetter.setNormalMapChannelUniform();
        materialUniformSetter.setOcclusionMapChannelUniform();
        shader.setUniform("uEnvironment.irradiance", IRRADIANCE_MAP_CHANNEL)
                .setUniform("uEnvironment.preFilteredMap", PRE_FILTERED_MAP_CHANNEL)
                .setUniform("uEnvironment.brdf", BRDF_LOOKUP_CHANNEL);
        shadowUniformSetter.setShadowMapsChannelsUniforms();
        Shader.unbind();
    }

    public void destroy() {
        shader.destroy();
    }

    public void bind() {
        shader.bind();
    }

    public void setModelNodeUniforms(final ModelNode modelNode) {
        skinningUniformsSetter.setModelNodeUniforms(modelNode);
    }

    public void setViewProjectionUniform(final Matrix4fc viewProjectionMatrix) {
        skinningUniformsSetter.setViewProjectionUniform(viewProjectionMatrix);
    }

    public void setMaterialUniforms(final Material material) {
        materialUniformSetter.setDiffuseColorUniform(material.getDiffuseColor());
        materialUniformSetter.setEmissiveColorUniform(material.getEmissiveColor());
        materialUniformSetter.setEmissiveIntensityUniform(material.getEmissiveIntensity());
        materialUniformSetter.setRoughnessUniform(material.getRoughness());
        materialUniformSetter.setMetallicUniform(material.getMetallic());
        materialUniformSetter.setOcclusionStrengthUniform(material.getOcclusionStrength());

        final var diffuseMap = material.getDiffuseMap();
        materialUniformSetter.setHasDiffuseMapUniform(diffuseMap.isPresent());
        diffuseMap.ifPresent(map -> map.bind(DIFFUSE_MAP_CHANNEL));

        final var emissiveMap = material.getEmissiveMap();
        materialUniformSetter.setHasEmissiveMapUniform(emissiveMap.isPresent());
        emissiveMap.ifPresent(map -> map.bind(EMISSIVE_MAP_CHANNEL));

        final var roughnessMetallicMap = material.getRoughnessMetallicMap();
        materialUniformSetter.setHasRoughnessMetallicMapUniform(roughnessMetallicMap.isPresent());
        roughnessMetallicMap.ifPresent(map -> map.bind(ROUGHNESS_METALLIC_MAP_CHANNEL));

        final var normalMap = material.getNormalMap();
        materialUniformSetter.setHasNormalMapUniform(normalMap.isPresent());
        normalMap.ifPresent(map -> map.bind(NORMAL_MAP_CHANNEL));

        final var occlusionMap = material.getOcclusionMap();
        materialUniformSetter.setHasOcclusionMapUniform(occlusionMap.isPresent());
        occlusionMap.ifPresent(map -> map.bind(OCCLUSION_MAP_CHANNEL));

        materialUniformSetter.setAlphaMode(material.getAlphaMode());
        materialUniformSetter.setAlphaCutoffUniform(material.getAlphaCutoff());
    }

    public void setCameraUniforms(final Camera camera) {
        shader.setUniform("uCamera.position", camera.getPosition());
    }

    public void setDirectionalLightsUniforms(final List<DirectionalLight> directionalLights) {
        lightUniformSetter.setDirectionalLightsUniforms(directionalLights);
    }

    public void setPointLightsUniforms(final List<PointLight> pointLights) {
        lightUniformSetter.setPointLightsUniforms(pointLights);
    }

    public void setSpotLightsUniforms(final List<SpotLight> spotLights) {
        lightUniformSetter.setSpotLightsUniforms(spotLights);
    }

    public void setCSMUniforms(final CascadedShadowMap cascadedShadowMap) {
        shadowUniformSetter.setCSMUniforms(cascadedShadowMap);
    }
}
