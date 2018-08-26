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
 * Wrapper for the gbuffer shader.
 *
 * @author adrien
 */
public class GBufferShader {

    public static final int DIFFUSE_MAP_CHANNEL = 0;
    public static final int EMISSIVE_MAP_CHANNEL = 1;
    public static final int ROUGHNESS_METALLIC_MAP_CHANNEL = 2;
    public static final int NORMAL_MAP_CHANNEL = 3;
    public static final int OCCLUSION_MAP_CHANNEL = 4;

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
