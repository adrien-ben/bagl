package com.adrienben.games.bagl.deferred;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.utils.ObjectUtils;
import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import com.adrienben.games.bagl.deferred.data.SceneRenderDataCollector;
import com.adrienben.games.bagl.deferred.gbuffer.GBuffer;
import com.adrienben.games.bagl.deferred.paths.DeferredPath;
import com.adrienben.games.bagl.deferred.paths.ForwardPath;
import com.adrienben.games.bagl.deferred.shadow.CSMGenerator;
import com.adrienben.games.bagl.deferred.shadow.CascadedShadowMap;
import com.adrienben.games.bagl.deferred.skybox.SkyboxRenderer;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.rendering.particles.ParticleRenderer;
import com.adrienben.games.bagl.engine.rendering.postprocess.PostProcessor;
import com.adrienben.games.bagl.engine.rendering.postprocess.fxaa.FxaaPresets;
import com.adrienben.games.bagl.engine.rendering.postprocess.steps.BloomStep;
import com.adrienben.games.bagl.engine.rendering.postprocess.steps.FxaaStep;
import com.adrienben.games.bagl.engine.rendering.postprocess.steps.ToneMappingStep;
import com.adrienben.games.bagl.engine.rendering.renderer.Renderer;
import com.adrienben.games.bagl.engine.scene.Scene;
import com.adrienben.games.bagl.engine.scene.components.DirectionalLightComponent;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.texture.Format;
import org.joml.FrustumIntersection;

import java.util.List;
import java.util.Objects;

/**
 * PBR Deferred Scene Renderer
 * <p>
 * It is responsible for rendering a {@link Scene}.
 * <p>
 * This renderer supports :
 * <li>HDR Skybox</li>
 * <li>Shadow mapping for one directional light</li>
 * <li>PBR rendering with IBL</li>
 * <li>Post processing pass (bloom/gamma correction/tone mapping from HDR to SDR</li>
 * <p>
 * When {@link PBRDeferredSceneRenderer#render(Scene)} is called, the data required for rendering is
 * gathered from the scene before the actual rendering takes place.
 *
 * @author adrien
 */
public class PBRDeferredSceneRenderer implements Renderer<Scene> {

    private final int xResolution;
    private final int yResolution;

    private SceneRenderDataCollector sceneRenderDataCollector;
    private SceneRenderData sceneRenderData;
    private FrustumIntersection cameraFrustum;

    private FrameBuffer finalBuffer;

    private CSMGenerator csmGenerator;
    private CascadedShadowMap cascadedShadowMap;
    private DeferredPath deferredPath;
    private ForwardPath forwardPath;
    private SkyboxRenderer skyboxRenderer;
    private ParticleRenderer particleRenderer;
    private PostProcessor postProcessor;

    /**
     * Construct the renderer
     */
    public PBRDeferredSceneRenderer() {
        final var config = Configuration.getInstance();
        xResolution = config.getXResolution();
        yResolution = config.getYResolution();

        sceneRenderDataCollector = new SceneRenderDataCollector();
        cameraFrustum = new FrustumIntersection();

        finalBuffer = new FrameBuffer(xResolution, yResolution, createFinalFrameBufferParameters());

        csmGenerator = new CSMGenerator();
        deferredPath = new DeferredPath(finalBuffer);
        forwardPath = new ForwardPath(finalBuffer);
        skyboxRenderer = new SkyboxRenderer();
        particleRenderer = new ParticleRenderer();
        postProcessor = new PostProcessor();
        setUpPostProcessor(config);
    }

    private void setUpPostProcessor(final Configuration config) {
        if (config.isBloomEnabled()) {
            postProcessor.addStep(new BloomStep(xResolution, yResolution));
        }
        postProcessor.addStep(new ToneMappingStep(xResolution, yResolution));
        if (config.getFxaaPresets() != FxaaPresets.DISABLED) {
            postProcessor.addStep(new FxaaStep(xResolution, yResolution, config.getFxaaPresets()));
        }
    }

    private FrameBufferParameters createFinalFrameBufferParameters() {
        return FrameBufferParameters.builder().colorOutputFormat(Format.RGBA32F).build();
    }

    /**
     * Release resources
     */
    public void destroy() {
        finalBuffer.destroy();
        csmGenerator.destroy();
        deferredPath.destroy();
        forwardPath.destroy();
        skyboxRenderer.destroy();
        particleRenderer.destroy();
        postProcessor.destroy();
    }

    /**
     * Render a scene from the camera point of view
     * <p>
     * The method first collect data to use for rendering.
     * <p>
     * Then if the scene contains at least one {@link DirectionalLightComponent}, it
     * will generate the shadow map for the point of view of the first light found.
     * <p>
     * Then it will perform the actual scene rendering by first generating the GBuffer
     * and then by computing the scene lighting.
     * <p>
     * After that it render the skybox and finally the particles.
     * <p>
     * Once the final image is generated, it passes it through a {@link PostProcessor}.
     * <p>
     * An {@link EngineException} will be thrown if the camera has no scene set up.
     *
     * @param scene The scene to render
     */
    public void render(final Scene scene) {
        sceneRenderData = sceneRenderDataCollector.collectDataForRendering(scene);
        if (Objects.isNull(sceneRenderData.getCamera())) {
            throw new EngineException("Impossible to render a scene if no camera is set up");
        }

        updateFrustum();
        renderShadowMap();
        renderSceneDataUsingForwardPath();
//        renderSceneDataUsingDeferredPath();
        renderSkybox();
        renderParticles();
        applyPostProcess();
    }

    private void updateFrustum() {
        cameraFrustum.set(sceneRenderData.getCamera().getViewProj());
    }

    private void renderShadowMap() {
        csmGenerator.setSceneRenderData(sceneRenderData);
        cascadedShadowMap = csmGenerator.generateShadowMaps();
    }

    private void renderSceneDataUsingDeferredPath() {
        deferredPath.setSceneRenderData(sceneRenderData);
        deferredPath.setCascadedShadowMap(cascadedShadowMap);
        deferredPath.renderSceneData();
    }

    private void renderSceneDataUsingForwardPath() {
        forwardPath.setSceneRenderData(sceneRenderData);
        forwardPath.setCascadedShadowMap(cascadedShadowMap);
        forwardPath.renderSceneData();
    }

    private void renderSkybox() {
        ObjectUtils.consumeIfPresent(sceneRenderData.getEnvironmentMap(), environmentMap -> {
            finalBuffer.bind();
            skyboxRenderer.renderSkybox(environmentMap, sceneRenderData.getCamera());
            finalBuffer.unbind();
        });
    }

    private void renderParticles() {
        finalBuffer.bind();
        sceneRenderData.getParticleEmitters().forEach(emitter -> {
            particleRenderer.setCamera(sceneRenderData.getCamera());
            particleRenderer.setDirectionalLights(sceneRenderData.getDirectionalLights());
            particleRenderer.setPointLights(sceneRenderData.getPointLights());
            particleRenderer.setSpotLights(sceneRenderData.getSpotLights());
            particleRenderer.render(emitter);
        });
        finalBuffer.unbind();
    }

    private void applyPostProcess() {
        postProcessor.process(finalBuffer.getColorTexture(0));
    }

    public List<FrameBuffer> getCSMBuffer() {
        return csmGenerator.getFrameBuffers();
    }

    public GBuffer getGBuffer() {
        return deferredPath.getGBuffer();
    }

    public FrameBuffer getFinalBuffer() {
        return finalBuffer;
    }
}
