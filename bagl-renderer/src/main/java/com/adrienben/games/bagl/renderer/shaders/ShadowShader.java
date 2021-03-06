package com.adrienben.games.bagl.renderer.shaders;

import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.rendering.shaders.MaterialUniformSetter;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.shader.ShaderWrapper;
import com.adrienben.games.bagl.renderer.shaders.uniforms.SkinningUniformsSetter;
import org.joml.Matrix4fc;

import static com.adrienben.games.bagl.engine.rendering.shaders.MaterialUniformSetter.DIFFUSE_MAP_CHANNEL;

/**
 * Wrapper for the shadow map shader.
 *
 * @author adrien
 */
public class ShadowShader extends ShaderWrapper {

    private final SkinningUniformsSetter skinningUniformsSetter;
    private final MaterialUniformSetter materialUniformSetter;

    public ShadowShader() {
        super(ShaderFactory.createShadowShader());
        this.skinningUniformsSetter = new SkinningUniformsSetter(shader);
        this.materialUniformSetter = new MaterialUniformSetter(shader);
        setTextureChannelsUniforms();
    }

    private void setTextureChannelsUniforms() {
        bind();
        materialUniformSetter.setDiffuseMapChannelUniform();
        Shader.unbind();
    }

    public void setModelNodeUniforms(final ModelNode modelNode) {
        skinningUniformsSetter.setModelNodeUniforms(modelNode);
    }

    public void setViewProjectionUniform(final Matrix4fc viewProjectionMatrix) {
        skinningUniformsSetter.setViewProjectionUniform(viewProjectionMatrix);
    }

    public void setMaterialUniforms(final Material material) {
        materialUniformSetter.setDiffuseColorUniform(material.getDiffuseColor());

        final var diffuseMap = material.getDiffuseMap();
        materialUniformSetter.setHasDiffuseMapUniform(diffuseMap.isPresent());
        diffuseMap.ifPresent(map -> map.bind(DIFFUSE_MAP_CHANNEL));

        materialUniformSetter.setAlphaMode(material.getAlphaMode());
        materialUniformSetter.setAlphaCutoffUniform(material.getAlphaCutoff());
    }
}
