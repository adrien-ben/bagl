package com.adrienben.games.bagl.renderer.paths;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.core.utils.ObjectUtils;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.opengl.BlendMode;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.OpenGL;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.renderer.pbr.BrdfLookup;
import com.adrienben.games.bagl.renderer.shaders.ForwardShader;
import com.adrienben.games.bagl.renderer.shadow.CascadedShadowMap;

import java.util.Objects;

import static com.adrienben.games.bagl.engine.rendering.material.MaterialUniformSetter.*;
import static com.adrienben.games.bagl.renderer.shaders.DeferredShader.*;
import static com.adrienben.games.bagl.renderer.shaders.uniforms.ShadowUniformSetter.SHADOW_MAP_0_CHANNEL;
import static org.lwjgl.opengl.GL11.*;

/**
 * Forward rendering path.
 * <p>
 * This path only renders transparent meshes.
 *
 * @author adrien.
 */
public class ForwardPath extends AbstractRenderingPath {

    private final ForwardShader forwardShader;
    private final MeshRenderer meshRenderer;
    private final BrdfLookup brdfLookup;

    public ForwardPath(final FrameBuffer targetBuffer) {
        super(targetBuffer);
        this.forwardShader = new ForwardShader();
        this.meshRenderer = new MeshRenderer();
        this.brdfLookup = new BrdfLookup();
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractRenderingPath#destroy()
     */
    @Override
    public void destroy() {
        forwardShader.destroy();
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractRenderingPath#renderSceneData()
     */
    @Override
    public void renderSceneData() {
        targetBuffer.bind();
        forwardShader.bind();
        setUpShaderUniforms();

        OpenGL.setBlendMode(BlendMode.TRANSPARENCY);
        OpenGL.disableDepthWrite();
        sceneRenderData.getModels().forEach(this::renderModel);
        OpenGL.enableDepthWrite();
        OpenGL.setBlendMode(BlendMode.NONE);

        cleanUp();
        Shader.unbind();
        targetBuffer.unbind();
    }

    private void setUpShaderUniforms() {
        forwardShader.setCameraUniforms(sceneRenderData.getCamera());
        forwardShader.setDirectionalLightsUniforms(sceneRenderData.getDirectionalLights());
        forwardShader.setPointLightsUniforms(sceneRenderData.getPointLights());
        forwardShader.setSpotLightsUniforms(sceneRenderData.getSpotLights());
        brdfLookup.getTexture().bind(BRDF_LOOKUP_CHANNEL);
        if (Objects.nonNull(sceneRenderData.getIrradianceMap())) {
            sceneRenderData.getIrradianceMap().bind(IRRADIANCE_MAP_CHANNEL);
        }
        if (Objects.nonNull(sceneRenderData.getPreFilteredMap())) {
            sceneRenderData.getPreFilteredMap().bind(PRE_FILTERED_MAP_CHANNEL);
        }
        forwardShader.setCSMUniforms(cascadedShadowMap);
    }

    private void renderModel(final Model model) {
        model.getNodes().forEach(this::renderModelNode);
    }

    private void renderModelNode(final ModelNode node) {
        if (CollectionUtils.isNotEmpty(node.getMeshes())) {
            forwardShader.setModelNodeUniforms(node);
            forwardShader.setViewProjectionUniform(sceneRenderData.getCamera().getViewProj());
            node.getMeshes().forEach(this::renderMesh);
        }
        node.getChildren().forEach(this::renderModelNode);
    }

    private void renderMesh(final Mesh mesh, final Material material) {
        if (material.getAlphaMode() != AlphaMode.BLEND) {
            return;
        }
        if (material.isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        }
        forwardShader.setMaterialUniforms(material);
        meshRenderer.render(mesh);
        material.getDiffuseMap().ifPresent(map -> map.unbind(DIFFUSE_MAP_CHANNEL));
        material.getEmissiveMap().ifPresent(map -> map.unbind(EMISSIVE_MAP_CHANNEL));
        material.getRoughnessMetallicMap().ifPresent(map -> map.unbind(ROUGHNESS_METALLIC_MAP_CHANNEL));
        material.getNormalMap().ifPresent(map -> map.unbind(NORMAL_MAP_CHANNEL));
        material.getOcclusionMap().ifPresent(map -> map.unbind(OCCLUSION_MAP_CHANNEL));
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    private void cleanUp() {
        ObjectUtils.consumeIfPresent(sceneRenderData.getIrradianceMap(), map -> map.unbind(IRRADIANCE_MAP_CHANNEL));
        ObjectUtils.consumeIfPresent(sceneRenderData.getPreFilteredMap(), map -> map.unbind(PRE_FILTERED_MAP_CHANNEL));
        brdfLookup.getTexture().unbind(BRDF_LOOKUP_CHANNEL);
        if (Objects.nonNull(cascadedShadowMap)) {
            for (int i = 0; i < CascadedShadowMap.CASCADE_COUNT; i++) {
                cascadedShadowMap.getShadowCascade(i).getShadowMap().unbind(SHADOW_MAP_0_CHANNEL + i);
            }
        }
    }
}
