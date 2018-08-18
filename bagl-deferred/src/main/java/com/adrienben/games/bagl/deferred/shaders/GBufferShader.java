package com.adrienben.games.bagl.deferred.shaders;

import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.opengl.shader.Shader;
import org.joml.Matrix4fc;

/**
 * Wrapper for the gbuffer shader.
 *
 * @author adrien
 */
public class GBufferShader {

    private static final int DIFFUSE_MAP_CHANNEL = 0;
    private static final int EMISSIVE_MAP_CHANNEL = 1;
    private static final int ROUGHNESS_METALLIC_MAP_CHANNEL = 2;
    private static final int NORMAL_MAP_CHANNEL = 3;
    private static final int OCCLUSION_MAP_CHANNEL = 4;

    private final Shader shader;

    public GBufferShader() {
        this.shader = ShaderFactory.createGBufferShader();
        setTextureChannelsUniforms();
    }

    private void setTextureChannelsUniforms() {
        bind();
        shader.setUniform("uMaterial.diffuseMap", DIFFUSE_MAP_CHANNEL);
        shader.setUniform("uMaterial.emissiveMap", EMISSIVE_MAP_CHANNEL);
        shader.setUniform("uMaterial.roughnessMetallicMap", ROUGHNESS_METALLIC_MAP_CHANNEL);
        shader.setUniform("uMaterial.normalMap", NORMAL_MAP_CHANNEL);
        shader.setUniform("uMaterial.occlusionMap", OCCLUSION_MAP_CHANNEL);
        Shader.unbind();
    }

    public void destroy() {
        shader.destroy();
    }

    public void bind() {
        shader.bind();
    }

    public void setWorldUniform(final Matrix4fc worldMatrix) {
        shader.setUniform("uMatrices.world", worldMatrix);
    }

    public void setWorldViewProjectionUniform(final Matrix4fc worldViewProjectionMatrix) {
        shader.setUniform("uMatrices.wvp", worldViewProjectionMatrix);
    }

    public void setMaterialUniforms(final Material material) {
        shader.setUniform("uMaterial.diffuseColor", material.getDiffuseColor());
        shader.setUniform("uMaterial.emissiveColor", material.getEmissiveColor());
        shader.setUniform("uMaterial.emissiveIntensity", material.getEmissiveIntensity());
        shader.setUniform("uMaterial.roughness", material.getRoughness());
        shader.setUniform("uMaterial.metallic", material.getMetallic());
        shader.setUniform("uMaterial.occlusionStrength", material.getOcclusionStrength());

        final var diffuseMap = material.getDiffuseMap();
        shader.setUniform("uMaterial.hasDiffuseMap", diffuseMap.isPresent());
        diffuseMap.ifPresent(map -> map.bind(DIFFUSE_MAP_CHANNEL));

        final var emissiveMap = material.getEmissiveMap();
        shader.setUniform("uMaterial.hasEmissiveMap", emissiveMap.isPresent());
        emissiveMap.ifPresent(map -> map.bind(EMISSIVE_MAP_CHANNEL));

        final var roughnessMetallicMap = material.getRoughnessMetallicMap();
        shader.setUniform("uMaterial.hasRoughnessMetallicMap", roughnessMetallicMap.isPresent());
        roughnessMetallicMap.ifPresent(map -> map.bind(ROUGHNESS_METALLIC_MAP_CHANNEL));

        final var normalMap = material.getNormalMap();
        shader.setUniform("uMaterial.hasNormalMap", normalMap.isPresent());
        normalMap.ifPresent(map -> map.bind(NORMAL_MAP_CHANNEL));

        final var occlusionMap = material.getOcclusionMap();
        shader.setUniform("uMaterial.hasOcclusionMap", occlusionMap.isPresent());
        occlusionMap.ifPresent(map -> map.bind(OCCLUSION_MAP_CHANNEL));

        shader.setUniform("uMaterial.isOpaque", material.getAlphaMode() == AlphaMode.OPAQUE);
        shader.setUniform("uMaterial.alphaCutoff", material.getAlphaCutoff());
    }
}
