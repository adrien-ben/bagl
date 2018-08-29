package com.adrienben.games.bagl.deferred.gbuffer.generator;

import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import com.adrienben.games.bagl.deferred.gbuffer.GBuffer;
import com.adrienben.games.bagl.deferred.shaders.GBufferShader;
import com.adrienben.games.bagl.engine.rendering.material.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.opengl.shader.Shader;

import static com.adrienben.games.bagl.engine.rendering.material.MaterialUniformSetter.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * {@link GBuffer} generator.
 * <p>
 * This class is responsible to render {@link SceneRenderData} into a geometry buffer.
 * Only mesh with {@link AlphaMode#OPAQUE} and {@link AlphaMode#MASK} will be rendered to
 * the gbuffer.
 *
 * @author adrien.
 */
public class GBufferGenerator {

    private final GBufferShader gBufferShader;
    private final GBuffer gBuffer;
    private final MeshRenderer meshRenderer;

    private SceneRenderData sceneRenderData;

    public GBufferGenerator(final int xResolution, final int yResolution) {
        this.gBufferShader = new GBufferShader();
        this.gBuffer = new GBuffer(xResolution, yResolution);
        this.meshRenderer = new MeshRenderer();
    }

    /**
     * Release resources.
     */
    public void destroy() {
        gBufferShader.destroy();
        gBuffer.destroy();
    }

    /**
     * Render the scene data into a {@link GBuffer}.
     * <p>
     * This method will always return a reference to the same {@link GBuffer} instance.
     *
     * @param sceneRenderData The data of the scene to render.
     * @return A reference to the underlying {@link GBuffer}.
     */
    public GBuffer generateGBuffer(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
        gBuffer.bind();
        gBuffer.clear();
        gBufferShader.bind();

        sceneRenderData.getModels().forEach(this::renderModelToGBuffer);

        Shader.unbind();
        gBuffer.unbind();
        return gBuffer;
    }

    /**
     * Render a model to the GBuffer
     *
     * @param model The model to render
     */
    private void renderModelToGBuffer(final Model model) {
        model.getNodes().forEach(this::renderModelNodeToGBuffer);
    }

    /**
     * Render a model node to the GBuffer
     *
     * @param node The node to render
     */
    private void renderModelNodeToGBuffer(final ModelNode node) {
        if (CollectionUtils.isNotEmpty(node.getMeshes())) {
            gBufferShader.setModelNodeUniforms(node);
            gBufferShader.setViewProjectionUniform(sceneRenderData.getCamera().getViewProj());
            node.getMeshes().forEach(this::renderMeshToGBuffer);
        }
        node.getChildren().forEach(this::renderModelNodeToGBuffer);
    }

    /**
     * Render a mesh to the GBuffer
     *
     * @param mesh     The mesh to render
     * @param material The material to apply
     */
    private void renderMeshToGBuffer(final Mesh mesh, final Material material) {
        if (material.getAlphaMode() == AlphaMode.BLEND) {
            return;
        }
        if (material.isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        }
        gBufferShader.setMaterialUniforms(material);
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

    public GBuffer getGBuffer() {
        return gBuffer;
    }
}
