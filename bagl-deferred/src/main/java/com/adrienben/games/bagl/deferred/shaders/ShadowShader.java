package com.adrienben.games.bagl.deferred.shaders;

import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.opengl.shader.Shader;
import org.joml.Matrix4fc;

/**
 * Wrapper for the shadow map shader.
 *
 * @author adrien
 */
public class ShadowShader {

    private final Shader shader;

    public ShadowShader() {
        this.shader = ShaderFactory.createShadowShader();
    }

    public void destroy() {
        shader.destroy();
    }

    public void bind() {
        shader.bind();
    }

    public void setWorldViewProjectionUniform(final Matrix4fc worldViewProjectionMatrix) {
        shader.setUniform("wvp", worldViewProjectionMatrix);
    }

    public void setMaterialUniforms(final Material material) {
        shader.setUniform("uMaterial.diffuseColor", material.getDiffuseColor());

        final var diffuseMap = material.getDiffuseMap();
        shader.setUniform("uMaterial.hasDiffuseMap", diffuseMap.isPresent());
        diffuseMap.ifPresent(map -> {
            shader.setUniform("uMaterial.diffuseMap", Material.DIFFUSE_MAP_CHANNEL);
            map.bind();
        });

        shader.setUniform("uMaterial.isOpaque", material.getAlphaMode() == AlphaMode.OPAQUE);
        shader.setUniform("uMaterial.alphaCutoff", material.getAlphaCutoff());
    }
}
