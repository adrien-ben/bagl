package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
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
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.adrienben.games.bagl.opengl.texture.Wrap;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.adrienben.games.bagl.deferred.shaders.ShadowShader.DIFFUSE_MAP_CHANNEL;
import static com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap.CASCADE_COUNT;
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

    private static final float POLYGON_OFFSET = 1.0f;

    private final int resolution;
    private final List<FrameBuffer> frameBuffers;
    private final ShadowShader shadowShader;
    private final CSMSplitsComputer csmSplitsComputer;
    private final MeshRenderer meshRenderer;
    private final Matrix4f wvpBuffer;

    private SceneRenderData sceneRenderData;
    private FrameBuffer currentFrameBuffer;
    private CSMSplit currentCSMSplit;
    private List<ShadowCascade> shadowCascades = new ArrayList<>();

    public CSMGenerator() {
        this.resolution = Configuration.getInstance().getShadowMapResolution();
        this.frameBuffers = CollectionUtils.createListWithDefaultValues(ArrayList::new, CASCADE_COUNT, this::createFrameBuffer);
        this.shadowShader = new ShadowShader();
        this.csmSplitsComputer = new CSMSplitsComputer();
        this.meshRenderer = new MeshRenderer();
        this.wvpBuffer = new Matrix4f();
    }

    private FrameBuffer createFrameBuffer() {
        final TextureParameters depthTextureParameters = TextureParameters.builder()
                .format(Format.DEPTH_32F)
                .compareFunction(CompareFunction.LESS)
                .sWrap(Wrap.CLAMP_TO_BORDER)
                .tWrap(Wrap.CLAMP_TO_BORDER)
                .borderColor(Color.WHITE)
                .build();
        return new FrameBuffer(resolution, resolution, FrameBufferParameters.builder().depthStencilTextureParameters(depthTextureParameters).build());
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
            return new CascadedShadowMap(csmSplitsComputer.getzNear(), csmSplitsComputer.getzFar(), Collections.unmodifiableList(shadowCascades));
        }
        return null;
    }

    private boolean shouldRenderShadow() {
        return !sceneRenderData.getDirectionalLights().isEmpty();
    }

    private void computeCSMViewProjections() {
        csmSplitsComputer.setSceneRenderData(sceneRenderData);
        csmSplitsComputer.computeCSMViewProjections();
    }

    private void prepareForRenderingAllMaps() {
        final var config = Configuration.getInstance();
        glCullFace(GL_FRONT);
        glEnable(GL_DEPTH_CLAMP);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(POLYGON_OFFSET, config.getShadowPolygonOffsetUnits());
        shadowShader.bind();
        shadowCascades.clear();
    }

    private void renderAllMaps() {
        for (int i = 0; i < CASCADE_COUNT; i++) {
            currentCSMSplit = csmSplitsComputer.getSplit(i);
            currentFrameBuffer = frameBuffers.get(i);
            generateShadowMap();
        }
    }

    private void generateShadowMap() {
        prepareForRenderingOneMap();
        sceneRenderData.getModels().forEach(this::renderModelShadow);
        shadowCascades.add(new ShadowCascade(currentCSMSplit.getFarDepth(), currentCSMSplit.getLightsViewProjection(), currentFrameBuffer.getDepthTexture()));
        cleanUpAfterRenderingOneMap();
    }

    private void prepareForRenderingOneMap() {
        glViewport(0, 0, resolution, resolution);
        currentFrameBuffer.bind();
        currentFrameBuffer.clear();
    }

    private void renderModelShadow(final Model model) {
        model.getNodes().forEach(this::renderModelNodeShadow);
    }

    private void renderModelNodeShadow(final ModelNode node) {
        final var nodeTransform = node.getTransform().getTransformMatrix();
        currentCSMSplit.getLightsViewProjection().mul(nodeTransform, wvpBuffer);
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
        material.getDiffuseMap().ifPresent(map -> map.unbind(DIFFUSE_MAP_CHANNEL));
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    private void cleanUpAfterRenderingOneMap() {
        currentFrameBuffer.unbind();
    }

    private void cleanUpAfterRenderingAllMaps() {
        final var config = Configuration.getInstance();
        Shader.unbind();
        glDisable(GL_POLYGON_OFFSET_FILL);
        glDisable(GL_DEPTH_CLAMP);
        glCullFace(GL_BACK);
        glViewport(0, 0, config.getXResolution(), config.getYResolution());
    }

    public void setSceneRenderData(final SceneRenderData sceneRenderData) {
        this.sceneRenderData = sceneRenderData;
    }

    public List<FrameBuffer> getFrameBuffers() {
        return frameBuffers;
    }
}
