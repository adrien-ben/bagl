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

    private final Shader shader;

    public GBufferShader() {
        this.shader = ShaderFactory.createGBufferShader();
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

        final var diffuseMap = material.getDiffuseMap();
        shader.setUniform("uMaterial.hasDiffuseMap", diffuseMap.isPresent());
        diffuseMap.ifPresent(map -> {
            shader.setUniform("uMaterial.diffuseMap", Material.DIFFUSE_MAP_CHANNEL);
            map.bind();
        });

        final var emissiveMap = material.getEmissiveMap();
        shader.setUniform("uMaterial.hasEmissiveMap", emissiveMap.isPresent());
        emissiveMap.ifPresent(map -> {
            shader.setUniform("uMaterial.emissiveMap", Material.EMISSIVE_MAP_CHANNEL);
            map.bind(Material.EMISSIVE_MAP_CHANNEL);
        });

        final var ormMap = material.getOrmMap();
        shader.setUniform("uMaterial.hasOrmMap", ormMap.isPresent());
        ormMap.ifPresent(map -> {
            shader.setUniform("uMaterial.ormMap", Material.ORM_MAP_CHANNEL);
            map.bind(Material.ORM_MAP_CHANNEL);
        });

        final var normalMap = material.getNormalMap();
        shader.setUniform("uMaterial.hasNormalMap", normalMap.isPresent());
        normalMap.ifPresent(map -> {
            shader.setUniform("uMaterial.normalMap", Material.NORMAL_MAP_CHANNEL);
            map.bind(Material.NORMAL_MAP_CHANNEL);
        });

        shader.setUniform("uMaterial.isOpaque", material.getAlphaMode() == AlphaMode.OPAQUE);
        shader.setUniform("uMaterial.alphaCutoff", material.getAlphaCutoff());
    }
}
