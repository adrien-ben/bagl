package com.adrienben.games.bagl.deferred.shaders;

import com.adrienben.games.bagl.deferred.shaders.uniforms.SkinningUniformsSetter;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.material.MaterialUniformSetter;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.opengl.shader.Shader;
import org.joml.Matrix4fc;

import static com.adrienben.games.bagl.engine.rendering.material.MaterialUniformSetter.*;


/**
 * Wrapper for the gbuffer shader.
 *
 * @author adrien
 */
public class GBufferShader {

    private final Shader shader;
    private final SkinningUniformsSetter skinningUniformsSetter;
    private final MaterialUniformSetter materialUniformSetter;

    public GBufferShader() {
        this.shader = ShaderFactory.createGBufferShader();
        this.skinningUniformsSetter = new SkinningUniformsSetter(shader);
        this.materialUniformSetter = new MaterialUniformSetter(shader);
        setTextureChannelsUniforms();
    }

    private void setTextureChannelsUniforms() {
        bind();
        materialUniformSetter.setDiffuseMapChannelUniform();
        materialUniformSetter.setEmissiveMapChannelUniform();
        materialUniformSetter.setRoughnessMetallicMapChannelUniform();
        materialUniformSetter.setNormalMapChannelUniform();
        materialUniformSetter.setOcclusionMapChannelUniform();
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
}
