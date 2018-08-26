package com.adrienben.games.bagl.deferred.shaders.uniforms;

import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.model.Joint;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.opengl.shader.Shader;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * This class is responsible for setting shader uniforms related to mesh skinning.
 *
 * @author adrien
 */
public class SkinningUniformsSetter {

    private Shader targetShader;

    public SkinningUniformsSetter(final Shader targetShader) {
        this.targetShader = targetShader;
    }

    public void setModelNodeUniforms(final ModelNode modelNode) {
        final var nodeTransform = modelNode.getTransform();
        targetShader.setUniform("uMatrices.world", nodeTransform.getTransformMatrix());
        modelNode.getJoints().ifPresentOrElse(
                joints -> setJointsUniforms(joints, nodeTransform),
                () -> setIsSkinnedUniform(false));
    }

    private void setJointsUniforms(final List<Joint> joints, final Transform globalMeshTransform) {
        setIsSkinnedUniform(true);
        for (int i = 0; i < joints.size(); i++) {
            targetShader.setUniform("uJoints[" + i + "].jointMatrix", joints.get(i).computeAndGetJointMatrix(globalMeshTransform));
        }
    }

    private void setIsSkinnedUniform(final boolean isSkinned) {
        targetShader.setUniform("uIsSkinned", isSkinned);
    }

    public void setViewProjectionUniform(final Matrix4fc viewProjectionMatrix) {
        targetShader.setUniform("uMatrices.viewProjection", viewProjectionMatrix);
    }

}
