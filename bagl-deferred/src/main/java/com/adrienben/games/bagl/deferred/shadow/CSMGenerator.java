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
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.CompareFunction;
import com.adrienben.games.bagl.opengl.texture.Texture;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap.CASCADE_COUNT;
import static com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap.SPLIT_RESOLUTIONS;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP;

/**
 * Cascaded shadow maps (CSM) generator.
 * <p>
 * This class generated the CSM from {@link SceneRenderData}.
 *
 * @author adrien
 */
public class CSMGenerator {

    private final List<FrameBuffer> frameBuffers;
    private final ShadowShader shadowShader;
    private final CSMViewProjectionComputer csmViewProjectionComputer;
    private final MeshRenderer meshRenderer;
    private final Matrix4f wvpBuffer;

    private SceneRenderData sceneRenderData;
    private float currentSplitValue;
    private int currentResolution;
    private FrameBuffer currentFrameBuffer;
    private Matrix4f currentCasterViewProjection;
    private List<ShadowCascade> shadowCascades = new ArrayList<>();

    public CSMGenerator() {
        this.frameBuffers = SPLIT_RESOLUTIONS.stream().map(this::createFrameBuffer).collect(Collectors.toList());
        this.shadowShader = new ShadowShader();
        this.csmViewProjectionComputer = new CSMViewProjectionComputer();
        this.meshRenderer = new MeshRenderer();
        this.wvpBuffer = new Matrix4f();
    }

    private FrameBuffer createFrameBuffer(final int resolution) {
        return new FrameBuffer(resolution, resolution, FrameBufferParameters.builder().compareFunction(CompareFunction.LESS).build());
    }

    /**
     * Release all resources
     */
    public void destroy() {
        frameBuffers.forEach(FrameBuffer::destroy);
        shadowShader.destroy();
    }

    /**
     * Generate the CSM for the provided {@link SceneRenderData}.
     * <p>
     * To generate shadow maps the scene must contain at least on directional light.
     *
     * @return true if the shadow maps were generated, false otherwise.
     */
    public CascadedShadowMap generateShadowMaps() {
        if (shouldRenderShadow()) {
            computeCSMViewProjections();
            prepareForRenderingAllMaps();
            renderAllMaps();
            cleanUpAfterRenderingAllMaps();
            return new CascadedShadowMap(Collections.unmodifiableList(shadowCascades));
        }
        return null;
    }

    private boolean shouldRenderShadow() {
        return !sceneRenderData.getDirectionalLights().isEmpty();
    }

    private void computeCSMViewProjections() {
        csmViewProjectionComputer.setSceneRenderData(sceneRenderData);
        csmViewProjectionComputer.computeCSMViewProjections();
    }

    private void prepareForRenderingAllMaps() {
        glCullFace(GL_FRONT);
        glEnable(GL_DEPTH_CLAMP);
        shadowShader.bind();
        shadowCascades.clear();
    }

    private void renderAllMaps() {
        for (int i = 0; i < CASCADE_COUNT; i++) {
            currentSplitValue = csmViewProjectionComputer.getSplitValueForSplit(i);
            currentResolution = SPLIT_RESOLUTIONS.get(i);
            currentFrameBuffer = frameBuffers.get(i);
            currentCasterViewProjection = csmViewProjectionComputer.getViewProjectionForSplit(i);
            generateShadowMap();
        }
    }

    private void generateShadowMap() {
        prepareForRenderingOneMap();
        sceneRenderData.getModels().forEach(this::renderModelShadow);
        shadowCascades.add(new ShadowCascade(currentSplitValue, currentCasterViewProjection, currentFrameBuffer.getDepthTexture()));
        cleanUpAfterRenderingOneMap();
    }

    private void prepareForRenderingOneMap() {
        glViewport(0, 0, currentResolution, currentResolution);
        currentFrameBuffer.bind();
        currentFrameBuffer.clear();
    }

    private void renderModelShadow(final Model model) {
        model.getNodes().forEach(this::renderModelNodeShadow);
    }

    private void renderModelNodeShadow(final ModelNode node) {
        final var nodeTransform = node.getTransform().getTransformMatrix();
        currentCasterViewProjection.mul(nodeTransform, wvpBuffer);
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
        material.getDiffuseMap().ifPresent(Texture::unbind);
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    private void cleanUpAfterRenderingOneMap() {
        currentFrameBuffer.unbind();
    }

    private void cleanUpAfterRenderingAllMaps() {
        Shader.unbind();
        glDisable(GL_DEPTH_CLAMP);
        glCullFace(GL_BACK);
        final var config = Configuration.getInstance();
        glViewport(0, 0, config.getXResolution(), config.getYResolution());
    }

    public void setSceneRenderData(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
    }

    public List<FrameBuffer> getFrameBuffers() {
        return frameBuffers;
    }
}
