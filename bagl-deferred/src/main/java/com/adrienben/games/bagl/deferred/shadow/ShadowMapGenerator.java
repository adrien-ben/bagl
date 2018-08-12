package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import com.adrienben.games.bagl.deferred.shaders.ShadowShader;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.AlphaMode;
import com.adrienben.games.bagl.engine.rendering.model.Mesh;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelNode;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Texture;
import org.joml.Matrix4f;

import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;

/**
 * Generate shadow maps from scene data.
 *
 * @author adrien
 */
public class ShadowMapGenerator {

    private final int shadowMapResolution;
    private final FrameBuffer shadowBuffer;
    private final ShadowShader shadowShader;
    private final MeshRenderer meshRenderer;
    private final ShadowMapViewProjectionComputer shadowMapViewProjectionComputer;
    private final Matrix4f wvpBuffer;
    private final Matrix4f casterViewProjection;

    private SceneRenderData sceneRenderData;

    public ShadowMapGenerator(final int shadowMapResolution) {
        this.shadowMapResolution = shadowMapResolution;
        this.shadowBuffer = new FrameBuffer(shadowMapResolution, shadowMapResolution);
        this.shadowShader = new ShadowShader();
        this.meshRenderer = new MeshRenderer();
        this.shadowMapViewProjectionComputer = new ShadowMapViewProjectionComputer();
        this.wvpBuffer = new Matrix4f();
        this.casterViewProjection = new Matrix4f();
    }

    /**
     * Release resources.
     */
    public void destroy() {
        shadowShader.destroy();
        shadowBuffer.destroy();
    }

    /**
     * Render the shadow map if the scene contains a directional light.
     *
     * @param sceneRenderData The scene data.
     */
    public Optional<Texture> generateShadowMap(final SceneRenderData sceneRenderData) {
        if (sceneRenderData.getDirectionalLights().isEmpty()) {
            return Optional.empty();
        }
        this.sceneRenderData = sceneRenderData;
        updateLightViewProjectionMatrix();
        prepareForRendering();
        sceneRenderData.getModels().forEach(this::renderModelShadow);
        cleanUpAfterRendering();
        return Optional.of(shadowBuffer.getDepthTexture());
    }

    private void updateLightViewProjectionMatrix() {
        final var caster = sceneRenderData.getDirectionalLights().get(0);
        final var sceneAABB = sceneRenderData.getSceneAABB();
        shadowMapViewProjectionComputer.computeViewProjectionFromCameraAndLight(caster, sceneAABB, casterViewProjection);
    }

    private void prepareForRendering() {
        glViewport(0, 0, shadowMapResolution, shadowMapResolution);
        glCullFace(GL_FRONT);
        shadowBuffer.bind();
        shadowBuffer.clear();
        shadowShader.bind();
    }

    private void renderModelShadow(final Model model) {
        model.getNodes().forEach(this::renderModelNodeShadow);
    }

    private void renderModelNodeShadow(final ModelNode node) {
        final var nodeTransform = node.getTransform().getTransformMatrix();
        casterViewProjection.mul(nodeTransform, wvpBuffer);
        shadowShader.setWorldViewProjectionUniform(wvpBuffer);
        node.getMeshes().forEach(this::renderMeshShadow);
        node.getChildren().forEach(this::renderModelNodeShadow);
    }

    private void renderMeshShadow(final Mesh mesh, final Material material) {
        if (material.getAlphaMode() == AlphaMode.BLEND) {
            return;
        }
        if (material.isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        }
        shadowShader.setMaterialUniforms(material);
        meshRenderer.render(mesh);
        Texture.unbind();
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    private void cleanUpAfterRendering() {
        Shader.unbind();
        shadowBuffer.unbind();
        final var config = Configuration.getInstance();
        glViewport(0, 0, config.getXResolution(), config.getYResolution());
        glCullFace(GL_BACK);
    }

    public Matrix4f getCasterViewProjection() {
        return casterViewProjection;
    }

    public FrameBuffer getShadowBuffer() {
        return shadowBuffer;
    }
}
