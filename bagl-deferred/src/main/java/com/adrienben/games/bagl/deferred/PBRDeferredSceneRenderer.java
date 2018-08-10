package com.adrienben.games.bagl.deferred;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.deferred.collector.SceneRenderDataCollector;
import com.adrienben.games.bagl.deferred.pbr.BrdfLookup;
import com.adrienben.games.bagl.deferred.shaders.DeferredShader;
import com.adrienben.games.bagl.deferred.shaders.GBufferShader;
import com.adrienben.games.bagl.deferred.shaders.ShaderFactory;
import com.adrienben.games.bagl.deferred.shaders.ShadowShader;
import com.adrienben.games.bagl.deferred.shadow.ShadowMapViewProjectionComputer;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.rendering.Material;
import com.adrienben.games.bagl.engine.rendering.model.*;
import com.adrienben.games.bagl.engine.rendering.particles.ParticleRenderer;
import com.adrienben.games.bagl.engine.rendering.postprocess.PostProcessor;
import com.adrienben.games.bagl.engine.rendering.postprocess.steps.BloomStep;
import com.adrienben.games.bagl.engine.rendering.postprocess.steps.FxaaStep;
import com.adrienben.games.bagl.engine.rendering.postprocess.steps.ToneMappingStep;
import com.adrienben.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrienben.games.bagl.engine.rendering.renderer.Renderer;
import com.adrienben.games.bagl.engine.scene.Scene;
import com.adrienben.games.bagl.engine.scene.components.DirectionalLightComponent;
import com.adrienben.games.bagl.opengl.FrameBuffer;
import com.adrienben.games.bagl.opengl.FrameBufferParameters;
import com.adrienben.games.bagl.opengl.shader.Shader;
import com.adrienben.games.bagl.opengl.texture.Cubemap;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture;
import org.joml.AABBf;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

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
    private final int shadowMapResolution;

    private SceneRenderDataCollector sceneRenderDataCollector;

    private FrustumIntersection cameraFrustum;
    private AABBf aabbBuffer;

    private final Matrix4f wvpBuffer;
    private final Matrix4f lightViewProj;
    private final ShadowMapViewProjectionComputer shadowMapViewProjectionComputer;

    private Mesh screenQuad;
    private Mesh cubeMapMesh;

    private boolean renderShadow;

    private FrameBuffer gBuffer;
    private FrameBuffer shadowBuffer;
    private FrameBuffer finalBuffer;

    private Shader skyboxShader;
    private ShadowShader shadowShader;
    private GBufferShader gBufferShader;
    private DeferredShader deferredShader;

    private BrdfLookup brdfLookup;

    private ParticleRenderer particleRenderer;
    private MeshRenderer meshRenderer;
    private PostProcessor postProcessor;

    /**
     * Construct the renderer
     */
    public PBRDeferredSceneRenderer() {
        final var config = Configuration.getInstance();
        this.xResolution = config.getXResolution();
        this.yResolution = config.getYResolution();
        this.shadowMapResolution = config.getShadowMapResolution();

        this.sceneRenderDataCollector = new SceneRenderDataCollector();

        this.cameraFrustum = new FrustumIntersection();
        this.aabbBuffer = new AABBf();

        this.wvpBuffer = new Matrix4f();
        this.lightViewProj = new Matrix4f();
        this.shadowMapViewProjectionComputer = new ShadowMapViewProjectionComputer();

        this.screenQuad = MeshFactory.createScreenQuad();
        this.cubeMapMesh = MeshFactory.createCubeMapMesh();

        this.brdfLookup = new BrdfLookup();

        this.particleRenderer = new ParticleRenderer();
        this.meshRenderer = new MeshRenderer();
        this.postProcessor = new PostProcessor(
                new BloomStep(xResolution, yResolution),
                new ToneMappingStep(xResolution, yResolution),
                new FxaaStep(xResolution, yResolution, config.getFxaaPresets())
        );

        this.initFrameBuffers();
        this.initShaders();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.skyboxShader.destroy();
        this.shadowShader.destroy();
        this.gBufferShader.destroy();
        this.deferredShader.destroy();
        this.brdfLookup.destroy();
        this.gBuffer.destroy();
        this.shadowBuffer.destroy();
        this.finalBuffer.destroy();
        this.screenQuad.destroy();
        this.cubeMapMesh.destroy();
        this.particleRenderer.destroy();
        this.postProcessor.destroy();
    }

    /**
     * Initializes the frame buffers
     */
    private void initFrameBuffers() {
        this.gBuffer = new FrameBuffer(this.xResolution, this.yResolution, FrameBufferParameters.builder()
                .colorOutputFormat(Format.RGBA8, Format.RGBA16F, Format.RGB16F)
                .depthStencilFormat(Format.DEPTH_32F)
                .build());
        this.shadowBuffer = new FrameBuffer(this.shadowMapResolution, this.shadowMapResolution);
        this.finalBuffer = new FrameBuffer(this.xResolution, this.yResolution, FrameBufferParameters.builder()
                .colorOutputFormat(Format.RGBA32F)
                .depthStencilFormat(Format.DEPTH_32F)
                .build());
    }

    /**
     * Initializes the shaders
     */
    private void initShaders() {
        this.skyboxShader = ShaderFactory.createSkyboxShader();
        this.shadowShader = new ShadowShader();
        this.gBufferShader = new GBufferShader();
        this.deferredShader = new DeferredShader();
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
        sceneRenderDataCollector.collectDataForRendering(scene);

        if (Objects.isNull(sceneRenderDataCollector.getCamera())) {
            throw new EngineException("Impossible to render a scene if no camera is set up");
        }

        this.updateFrustum();
        this.renderShadowMap();
        this.performGeometryPass();
        this.performLightingPass();
        this.renderSkybox();
        this.renderParticles();

        this.postProcessor.process(this.finalBuffer.getColorTexture(0));
    }

    private void updateFrustum() {
        cameraFrustum.set(sceneRenderDataCollector.getCamera().getViewProj());
    }

    /**
     * Render the skybox from the environment map found in the scene if any
     */
    private void renderSkybox() {
        if (Objects.nonNull(sceneRenderDataCollector.getEnvironmentMap())) {
            this.finalBuffer.bind();
            sceneRenderDataCollector.getEnvironmentMap().bind();
            this.skyboxShader.bind();
            this.skyboxShader.setUniform("viewProj", sceneRenderDataCollector.getCamera().getViewProjAtOrigin());

            glDepthFunc(GL_LEQUAL);
            this.meshRenderer.render(this.cubeMapMesh);
            glDepthFunc(GL_LESS);

            Shader.unbind();
            Cubemap.unbind();
            this.finalBuffer.unbind();
        }
    }

    /**
     * Render the shadow map if the scene contains a directional light
     */
    private void renderShadowMap() {
        this.renderShadow = !sceneRenderDataCollector.getDirectionalLights().isEmpty();
        if (this.renderShadow) {
            updateLightViewProjectionMatrix();

            glViewport(0, 0, this.shadowMapResolution, this.shadowMapResolution);
            glCullFace(GL_FRONT);
            this.shadowBuffer.bind();
            this.shadowBuffer.clear();
            this.shadowShader.bind();

            sceneRenderDataCollector.getModels().forEach(this::renderModelShadow);

            Shader.unbind();
            this.shadowBuffer.unbind();
            glViewport(0, 0, this.xResolution, this.yResolution);
            glCullFace(GL_BACK);
        }
    }

    private void updateLightViewProjectionMatrix() {
        final var caster = sceneRenderDataCollector.getDirectionalLights().get(0);
        final var sceneAABB = sceneRenderDataCollector.getSceneAABB();
        shadowMapViewProjectionComputer.computeViewProjectionFromCameraAndLight(caster, sceneAABB, lightViewProj);
    }

    /**
     * Render a model in the shadow map
     *
     * @param model The model to render
     */
    private void renderModelShadow(final Model model) {
        model.getNodes().forEach(this::renderModelNodeShadow);
    }

    /**
     * Render a model node in the shadow map
     *
     * @param node The node to render
     */
    private void renderModelNodeShadow(final ModelNode node) {
        final var nodeTransform = node.getTransform().getTransformMatrix();
        this.lightViewProj.mul(nodeTransform, this.wvpBuffer);
        this.shadowShader.setWorldViewProjectionUniform(this.wvpBuffer);
        node.getMeshes().forEach(this::renderMeshShadow);
        node.getChildren().forEach(this::renderModelNodeShadow);
    }

    /**
     * Render a mesh to the shadow map
     *
     * @param mesh     The mesh to render
     * @param material The material to use
     */
    private void renderMeshShadow(final Mesh mesh, final Material material) {
        if (material.getAlphaMode() == AlphaMode.BLEND) {
            return;
        }
        if (material.isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        }
        this.shadowShader.setMaterialUniforms(material);
        this.meshRenderer.render(mesh);
        Texture.unbind();
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    /**
     * Perform the GBuffer pass
     */
    private void performGeometryPass() {
        this.gBuffer.bind();
        this.gBuffer.clear();
        this.gBufferShader.bind();

        sceneRenderDataCollector.getModels().forEach(this::renderModelToGBuffer);

        Shader.unbind();
        this.gBuffer.unbind();
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
        final var nodeTransform = node.getTransform().getTransformMatrix();
        sceneRenderDataCollector.getCamera().getViewProj().mul(nodeTransform, this.wvpBuffer);
        this.gBufferShader.setWorldUniform(nodeTransform);
        this.gBufferShader.setWorldViewProjectionUniform(this.wvpBuffer);
        node.getMeshes()
                .entrySet()
                .stream()
                .filter(entry -> isMeshVisibleFromCameraPOV(entry.getKey(), node.getTransform()))
                .forEach(entry -> renderMeshToGBuffer(entry.getKey(), entry.getValue()));
        node.getChildren().forEach(this::renderModelNodeToGBuffer);
    }

    private boolean isMeshVisibleFromCameraPOV(final Mesh mesh, final Transform meshTransform) {
        final var meshAabb = meshTransform.transformAABB(mesh.getAabb(), aabbBuffer);
        final int intersection = cameraFrustum.intersectAab(meshAabb.minX, meshAabb.minY, meshAabb.minZ,
                meshAabb.maxX, meshAabb.maxY, meshAabb.maxZ);
        return intersection == FrustumIntersection.INSIDE || intersection == FrustumIntersection.INTERSECT;
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
        this.gBufferShader.setMaterialUniforms(material);
        this.meshRenderer.render(mesh);
        Texture.unbind();
        Texture.unbind(1);
        Texture.unbind(2);
        Texture.unbind(3);
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    /**
     * Perform the lighting pass using data from the GBuffer, environment maps if any and
     * analytical light found in the scene
     */
    private void performLightingPass() {
        prepareResourcesForLightingPass();
        renderLightingPass();
        unbindResourcesPostLightingPass();
    }

    private void prepareResourcesForLightingPass() {
        this.finalBuffer.bind();
        this.finalBuffer.clear();
        this.finalBuffer.copyFrom(this.gBuffer, true, false);

        this.gBuffer.getColorTexture(0).bind(0);
        this.gBuffer.getColorTexture(1).bind(1);
        this.gBuffer.getColorTexture(2).bind(2);
        this.gBuffer.getDepthTexture().bind(3);
        this.brdfLookup.getTexture().bind(7);

        this.deferredShader.bind()
                .setCameraUniforms(sceneRenderDataCollector.getCamera())
                .setHasShadowUniform(this.renderShadow)
                .setDirectionalLightsUniforms(sceneRenderDataCollector.getDirectionalLights())
                .setPointLightsUniforms(sceneRenderDataCollector.getPointLights())
                .setSpotLightsUniforms(sceneRenderDataCollector.getSpotLights());

        if (Objects.nonNull(sceneRenderDataCollector.getIrradianceMap())) {
            sceneRenderDataCollector.getIrradianceMap().bind(5);
        }
        if (Objects.nonNull(sceneRenderDataCollector.getPreFilteredMap())) {
            sceneRenderDataCollector.getPreFilteredMap().bind(6);
        }
        if (this.renderShadow) {
            this.deferredShader.setShadowCasterViewProjectionMatrix(this.lightViewProj);
            this.shadowBuffer.getDepthTexture().bind(4);
        }
    }

    private void renderLightingPass() {
        glDepthFunc(GL_NOTEQUAL);
        glDepthMask(false);
        this.meshRenderer.render(this.screenQuad);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }

    private void unbindResourcesPostLightingPass() {
        Shader.unbind();
        Cubemap.unbind(5);
        Cubemap.unbind(6);
        Cubemap.unbind(7);
        Texture.unbind(0);
        Texture.unbind(1);
        Texture.unbind(2);
        Texture.unbind(3);
        Texture.unbind(4);
        this.finalBuffer.unbind();
    }

    private void renderParticles() {
        this.finalBuffer.bind();
        sceneRenderDataCollector.getParticleEmitters().forEach(emitter -> {
            this.particleRenderer.setCamera(sceneRenderDataCollector.getCamera());
            this.particleRenderer.setDirectionalLights(sceneRenderDataCollector.getDirectionalLights());
            this.particleRenderer.setPointLights(sceneRenderDataCollector.getPointLights());
            this.particleRenderer.setSpotLights(sceneRenderDataCollector.getSpotLights());
            this.particleRenderer.render(emitter);
        });
        this.finalBuffer.unbind();
    }

    public FrameBuffer getShadowBuffer() {
        return this.shadowBuffer;
    }

    public FrameBuffer getGBuffer() {
        return this.gBuffer;
    }

    public FrameBuffer getFinalBuffer() {
        return this.finalBuffer;
    }
}
