package com.adrienben.games.bagl.deferred;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.deferred.data.SceneRenderData;
import com.adrienben.games.bagl.deferred.data.SceneRenderDataCollector;
import com.adrienben.games.bagl.deferred.pbr.BrdfLookup;
import com.adrienben.games.bagl.deferred.shaders.DeferredShader;
import com.adrienben.games.bagl.deferred.shaders.GBufferShader;
import com.adrienben.games.bagl.deferred.shaders.ShaderFactory;
import com.adrienben.games.bagl.deferred.shadow.ShadowMapGenerator;
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

    private SceneRenderDataCollector sceneRenderDataCollector;
    private SceneRenderData sceneRenderData;

    private FrustumIntersection cameraFrustum;
    private AABBf aabbBuffer;

    private final Matrix4f wvpBuffer;

    private Mesh screenQuad;
    private Mesh cubeMapMesh;

    private FrameBuffer gBuffer;
    private FrameBuffer finalBuffer;

    private Shader skyboxShader;
    private GBufferShader gBufferShader;
    private DeferredShader deferredShader;

    private BrdfLookup brdfLookup;

    private Texture shadowMap;

    private ShadowMapGenerator shadowMapGenerator;
    private ParticleRenderer particleRenderer;
    private MeshRenderer meshRenderer;
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
        aabbBuffer = new AABBf();

        wvpBuffer = new Matrix4f();

        screenQuad = MeshFactory.createScreenQuad();
        cubeMapMesh = MeshFactory.createCubeMapMesh();

        brdfLookup = new BrdfLookup();

        shadowMapGenerator = new ShadowMapGenerator(config.getShadowMapResolution());
        particleRenderer = new ParticleRenderer();
        meshRenderer = new MeshRenderer();
        postProcessor = new PostProcessor(
                new BloomStep(xResolution, yResolution),
                new ToneMappingStep(xResolution, yResolution),
                new FxaaStep(xResolution, yResolution, config.getFxaaPresets())
        );

        initFrameBuffers();
        initShaders();
    }

    /**
     * Release resources
     */
    public void destroy() {
        skyboxShader.destroy();
        gBufferShader.destroy();
        deferredShader.destroy();
        brdfLookup.destroy();
        gBuffer.destroy();
        finalBuffer.destroy();
        screenQuad.destroy();
        cubeMapMesh.destroy();
        shadowMapGenerator.destroy();
        particleRenderer.destroy();
        postProcessor.destroy();
    }

    /**
     * Initializes the frame buffers
     */
    private void initFrameBuffers() {
        gBuffer = new FrameBuffer(xResolution, yResolution, FrameBufferParameters.builder()
                .colorOutputFormat(Format.RGBA8, Format.RGBA16F, Format.RGB16F)
                .depthStencilFormat(Format.DEPTH_32F)
                .build());
        finalBuffer = new FrameBuffer(xResolution, yResolution, FrameBufferParameters.builder()
                .colorOutputFormat(Format.RGBA32F)
                .depthStencilFormat(Format.DEPTH_32F)
                .build());
    }

    /**
     * Initializes the shaders
     */
    private void initShaders() {
        skyboxShader = ShaderFactory.createSkyboxShader();
        gBufferShader = new GBufferShader();
        deferredShader = new DeferredShader();
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
        performGeometryPass();
        performLightingPass();
        renderSkybox();
        renderParticles();

        postProcessor.process(finalBuffer.getColorTexture(0));
    }

    private void updateFrustum() {
        cameraFrustum.set(sceneRenderData.getCamera().getViewProj());
    }

    private void renderShadowMap() {
        shadowMap = shadowMapGenerator.generateShadowMap(sceneRenderData).orElse(null);
    }

    /**
     * Render the skybox from the environment map found in the scene if any
     */
    private void renderSkybox() {
        if (Objects.nonNull(sceneRenderData.getEnvironmentMap())) {
            finalBuffer.bind();
            sceneRenderData.getEnvironmentMap().bind();
            skyboxShader.bind();
            skyboxShader.setUniform("viewProj", sceneRenderData.getCamera().getViewProjAtOrigin());

            glDepthFunc(GL_LEQUAL);
            meshRenderer.render(cubeMapMesh);
            glDepthFunc(GL_LESS);

            Shader.unbind();
            Cubemap.unbind();
            finalBuffer.unbind();
        }
    }

    /**
     * Perform the GBuffer pass
     */
    private void performGeometryPass() {
        gBuffer.bind();
        gBuffer.clear();
        gBufferShader.bind();

        sceneRenderData.getModels().forEach(this::renderModelToGBuffer);

        Shader.unbind();
        gBuffer.unbind();
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
        sceneRenderData.getCamera().getViewProj().mul(nodeTransform, wvpBuffer);
        gBufferShader.setWorldUniform(nodeTransform);
        gBufferShader.setWorldViewProjectionUniform(wvpBuffer);
        node.getMeshes()
                .entrySet()
                .stream()
                .filter(entry -> isMeshVisibleFromCameraPOV(entry.getKey(), node.getTransform()))
                .forEach(entry -> renderMeshToGBuffer(entry.getKey(), entry.getValue()));
        node.getChildren().forEach(this::renderModelNodeToGBuffer);
    }

    private boolean isMeshVisibleFromCameraPOV(final Mesh mesh, final Transform meshTransform) {
        final var meshAabb = meshTransform.transformAABB(mesh.getAabb(), aabbBuffer);
        final var intersection = cameraFrustum.intersectAab(meshAabb.minX, meshAabb.minY, meshAabb.minZ,
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
        gBufferShader.setMaterialUniforms(material);
        meshRenderer.render(mesh);
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
        finalBuffer.bind();
        finalBuffer.clear();
        finalBuffer.copyFrom(gBuffer, true, false);

        gBuffer.getColorTexture(0).bind(0);
        gBuffer.getColorTexture(1).bind(1);
        gBuffer.getColorTexture(2).bind(2);
        gBuffer.getDepthTexture().bind(3);
        brdfLookup.getTexture().bind(7);

        final var hasShadow = Objects.nonNull(shadowMap);

        deferredShader.bind()
                .setCameraUniforms(sceneRenderData.getCamera())
                .setHasShadowUniform(hasShadow)
                .setDirectionalLightsUniforms(sceneRenderData.getDirectionalLights())
                .setPointLightsUniforms(sceneRenderData.getPointLights())
                .setSpotLightsUniforms(sceneRenderData.getSpotLights());

        if (Objects.nonNull(sceneRenderData.getIrradianceMap())) {
            sceneRenderData.getIrradianceMap().bind(5);
        }
        if (Objects.nonNull(sceneRenderData.getPreFilteredMap())) {
            sceneRenderData.getPreFilteredMap().bind(6);
        }
        if (hasShadow) {
            deferredShader.setShadowCasterViewProjectionMatrix(shadowMapGenerator.getCasterViewProjection());
            shadowMap.bind(4);
        }
    }

    private void renderLightingPass() {
        glDepthFunc(GL_NOTEQUAL);
        glDepthMask(false);
        meshRenderer.render(screenQuad);
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
        finalBuffer.unbind();
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

    public FrameBuffer getShadowBuffer() {
        return shadowMapGenerator.getShadowBuffer();
    }

    public FrameBuffer getGBuffer() {
        return gBuffer;
    }

    public FrameBuffer getFinalBuffer() {
        return finalBuffer;
    }
}
