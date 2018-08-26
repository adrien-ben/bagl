package com.adrienben.games.bagl.deferred.shaders;

import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.engine.rendering.model.Joint;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.opengl.shader.Shader;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * Wrapper for the shadow map shader.
 *
 * @author adrien
 */
public class ShadowShader {

    public static final int DIFFUSE_MAP_CHANNEL = 0;

    private final Shader shader;

    public ShadowShader() {
        this.shader = ShaderFactory.createShadowShader();
        setTextureChannelsUniforms();
    }

    private void setTextureChannelsUniforms() {
        bind();
        shader.setUniform("uMaterial.diffuseMap", DIFFUSE_MAP_CHANNEL);
        Shader.unbind();
    }

    public void destroy() {
        shader.destroy();
    }

    public void bind() {
        shader.bind();
    }

    public void setModelNodeUniforms(final ModelNode modelNode) {
        final var nodeTransform = modelNode.getTransform();
        shader.setUniform("uMatrices.world", nodeTransform.getTransformMatrix());
        modelNode.getJoints().ifPresentOrElse(
                joints -> setJointsUniforms(joints, nodeTransform),
                () -> setIsSkinnedUniform(false));
    }

    private void setJointsUniforms(final List<Joint> joints, final Transform globalMeshTransform) {
        setIsSkinnedUniform(true);
        for (int i = 0; i < joints.size(); i++) {
            shader.setUniform("uJoints[" + i + "].jointMatrix", joints.get(i).computeAndGetJointMatrix(globalMeshTransform));
        }
    }

    private void setIsSkinnedUniform(final boolean isSkinned) {
        shader.setUniform("uIsSkinned", isSkinned);
    }

    public void setViewProjectionUniform(final Matrix4fc viewProjectionMatrix) {
        shader.setUniform("uMatrices.viewProjection", viewProjectionMatrix);
    }

    public void setMaterialUniforms(final Material material) {
        shader.setUniform("uMaterial.diffuseColor", material.getDiffuseColor());

        final var diffuseMap = material.getDiffuseMap();
        shader.setUniform("uMaterial.hasDiffuseMap", diffuseMap.isPresent());
        diffuseMap.ifPresent(map -> map.bind(DIFFUSE_MAP_CHANNEL));

        shader.setUniform("uMaterial.isOpaque", material.getAlphaMode() == AlphaMode.OPAQUE);
        shader.setUniform("uMaterial.alphaCutoff", material.getAlphaCutoff());
    }
}
