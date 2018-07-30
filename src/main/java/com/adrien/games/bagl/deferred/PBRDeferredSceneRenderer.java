package com.adrien.games.bagl.deferred;

import com.adrien.games.bagl.core.exception.EngineException;
import com.adrien.games.bagl.core.io.ResourcePath;
import com.adrien.games.bagl.core.math.Vectors;
import com.adrien.games.bagl.engine.Configuration;
import com.adrien.games.bagl.engine.camera.Camera;
import com.adrien.games.bagl.engine.rendering.Material;
import com.adrien.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrien.games.bagl.engine.rendering.light.PointLight;
import com.adrien.games.bagl.engine.rendering.light.SpotLight;
import com.adrien.games.bagl.engine.rendering.model.Mesh;
import com.adrien.games.bagl.engine.rendering.model.MeshFactory;
import com.adrien.games.bagl.engine.rendering.model.Model;
import com.adrien.games.bagl.engine.rendering.model.ModelNode;
import com.adrien.games.bagl.engine.rendering.particles.ParticleEmitter;
import com.adrien.games.bagl.engine.rendering.particles.ParticleRenderer;
import com.adrien.games.bagl.engine.rendering.postprocess.PostProcessor;
import com.adrien.games.bagl.engine.rendering.postprocess.steps.BloomStep;
import com.adrien.games.bagl.engine.rendering.postprocess.steps.FxaaStep;
import com.adrien.games.bagl.engine.rendering.postprocess.steps.ToneMappingStep;
import com.adrien.games.bagl.engine.rendering.renderer.MeshRenderer;
import com.adrien.games.bagl.engine.rendering.renderer.Renderer;
import com.adrien.games.bagl.engine.scene.ComponentVisitor;
import com.adrien.games.bagl.engine.scene.Scene;
import com.adrien.games.bagl.engine.scene.components.*;
import com.adrien.games.bagl.opengl.FrameBuffer;
import com.adrien.games.bagl.opengl.FrameBufferParameters;
import com.adrien.games.bagl.opengl.shader.Shader;
import com.adrien.games.bagl.opengl.texture.Cubemap;
import com.adrien.games.bagl.opengl.texture.Format;
import com.adrien.games.bagl.opengl.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
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
 * When {@link PBRDeferredSceneRenderer#render(Scene)} is called, the renderer visits the scene and collects
 * scene data required for rendering
 *
 * @author adrien
 */
public class PBRDeferredSceneRenderer implements Renderer<Scene>, ComponentVisitor {

    private static final int BRDF_RESOLUTION = 512;

    private final int xResolution;
    private final int yResolution;
    private final int shadowMapResolution;

    private Camera camera;
    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;
    private final List<DirectionalLight> directionalLights;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;
    private final List<Model> models;
    private final List<ParticleEmitter> particleEmitters;

    private final Matrix4f wvpBuffer;
    private final Matrix4f lightViewProj;

    private Mesh screenQuad;
    private Mesh cubeMapMesh;

    private boolean renderShadow;

    private FrameBuffer gBuffer;
    private FrameBuffer shadowBuffer;
    private FrameBuffer finalBuffer;
    private FrameBuffer brdfBuffer;

    private Shader skyboxShader;
    private Shader shadowShader;
    private Shader gBufferShader;
    private Shader deferredShader;
    private Shader brdfShader;

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

        this.camera = null;
        this.directionalLights = new ArrayList<>();
        this.pointLights = new ArrayList<>();
        this.spotLights = new ArrayList<>();
        this.models = new ArrayList<>();
        this.particleEmitters = new ArrayList<>();

        this.wvpBuffer = new Matrix4f();
        this.lightViewProj = new Matrix4f();

        this.screenQuad = MeshFactory.createScreenQuad();
        this.cubeMapMesh = MeshFactory.createCubeMapMesh();

        this.particleRenderer = new ParticleRenderer();
        this.meshRenderer = new MeshRenderer();
        this.postProcessor = new PostProcessor(
                new BloomStep(xResolution, yResolution),
                new ToneMappingStep(xResolution, yResolution),
                new FxaaStep(xResolution, yResolution, config.getFxaaPresets())
        );

        this.initFrameBuffers();
        this.initShaders();
        this.bakeBRDFIntegration();
    }

    /**
     * Release resources
     */
    public void destroy() {
        this.skyboxShader.destroy();
        this.shadowShader.destroy();
        this.gBufferShader.destroy();
        this.deferredShader.destroy();
        this.brdfShader.destroy();
        this.gBuffer.destroy();
        this.shadowBuffer.destroy();
        this.finalBuffer.destroy();
        this.brdfBuffer.destroy();
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
        this.brdfBuffer = new FrameBuffer(BRDF_RESOLUTION, BRDF_RESOLUTION, FrameBufferParameters.builder()
                .hasDepthStencil(false)
                .colorOutputFormat(Format.RG16F)
                .build());
    }

    /**
     * Initializes the shaders
     */
    private void initShaders() {
        this.skyboxShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/environment/environment.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/environment_cubemap_sample.frag"))
                .build();
        this.shadowShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/shadow/shadow.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/shadow/shadow.frag"))
                .build();
        this.gBufferShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/deferred/gbuffer.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/deferred/gbuffer.frag"))
                .build();
        this.deferredShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/deferred/deferred.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/deferred/deferred.frag"))
                .build();
        this.brdfShader = Shader.builder()
                .vertexPath(ResourcePath.get("classpath:/shaders/post/post_process.vert"))
                .fragmentPath(ResourcePath.get("classpath:/shaders/environment/brdf_integration.frag"))
                .build();
    }

    /**
     * Bake the BRDF lookup texture for specular IBL
     */
    private void bakeBRDFIntegration() {
        this.brdfBuffer.bind();
        this.brdfShader.bind();

        glViewport(0, 0, BRDF_RESOLUTION, BRDF_RESOLUTION);
        this.meshRenderer.render(this.screenQuad);
        glViewport(0, 0, this.xResolution, this.yResolution);

        Shader.unbind();
        this.brdfBuffer.unbind();
    }

    /**
     * Render a scene from the camera point of view
     * <p>
     * The method first visit {@code scene} to collect data to use for rendering.
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
        this.preRenderCleanup();

        scene.accept(this);

        if (Objects.isNull(this.camera)) {
            throw new EngineException("Impossible to render a scene if no camera is set up");
        }

        this.renderShadowMap();
        this.performGeometryPass();
        this.performLightingPass();
        this.renderSkybox();
        this.renderParticles();

        this.postProcessor.process(this.finalBuffer.getColorTexture(0));
    }

    /**
     * Clear data before rendering in case the scene change since last frame
     */
    private void preRenderCleanup() {
        this.camera = null;
        this.environmentMap = null;
        this.irradianceMap = null;
        this.preFilteredMap = null;
        this.directionalLights.clear();
        this.pointLights.clear();
        this.spotLights.clear();
        this.models.clear();
        this.particleEmitters.clear();
    }

    /**
     * Render the skybox from the environment map found in the scene if any
     */
    private void renderSkybox() {
        if (Objects.nonNull(this.environmentMap)) {
            this.finalBuffer.bind();
            this.environmentMap.bind();
            this.skyboxShader.bind();
            this.skyboxShader.setUniform("viewProj", this.camera.getViewProjAtOrigin());

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
        this.renderShadow = !this.directionalLights.isEmpty();
        if (this.renderShadow) {
            final var position = new Vector3f(this.directionalLights.get(0).getDirection()).mul(-10f);

            this.lightViewProj.setOrtho(-10, 10, -10, 10, 0.1f, 20f)
                    .lookAt(position, Vectors.VEC3_ZERO, Vectors.VEC3_UP);

            glViewport(0, 0, this.shadowMapResolution, this.shadowMapResolution);
            this.shadowBuffer.bind();
            this.shadowBuffer.clear();
            this.shadowShader.bind();

            this.models.forEach(this::renderModelShadow);

            Shader.unbind();
            this.shadowBuffer.unbind();
            glViewport(0, 0, this.xResolution, this.yResolution);
        }
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
        this.shadowShader.setUniform("wvp", this.wvpBuffer);
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
        if (material.isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        }
        this.meshRenderer.render(mesh);
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

        this.models.forEach(this::renderModelToGBuffer);

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
        this.camera.getViewProj().mul(nodeTransform, this.wvpBuffer);
        this.gBufferShader
                .setUniform("uMatrices.world", nodeTransform)
                .setUniform("uMatrices.wvp", this.wvpBuffer);
        node.getMeshes().forEach(this::renderMeshToGBuffer);
        node.getChildren().forEach(this::renderModelNodeToGBuffer);
    }

    /**
     * Render a mesh to the GBuffer
     *
     * @param mesh     The mesh to render
     * @param material The material to apply
     */
    private void renderMeshToGBuffer(final Mesh mesh, final Material material) {
        if (material.isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        }
        material.applyTo(this.gBufferShader);
        this.meshRenderer.render(mesh);
        Texture.unbind();
        Texture.unbind(1);
        Texture.unbind(2);
        if (material.isDoubleSided()) {
            glEnable(GL_CULL_FACE);
        }
    }

    /**
     * Perform the lighting pass using data from the GBuffer, environment maps if any and
     * analytical light found in the scene
     */
    private void performLightingPass() {
        this.finalBuffer.bind();
        this.finalBuffer.clear();
        this.finalBuffer.copyFrom(this.gBuffer, true, false);

        this.gBuffer.getColorTexture(0).bind(0);
        this.gBuffer.getColorTexture(1).bind(1);
        this.gBuffer.getColorTexture(2).bind(2);
        this.gBuffer.getDepthTexture().bind(3);
        this.brdfBuffer.getColorTexture(0).bind(7);

        this.deferredShader.bind()
                .setUniform("uCamera.invertedViewProj", this.camera.getInvertedViewProj())
                .setUniform("uCamera.position", this.camera.getPosition())
                .setUniform("uShadow.hasShadow", this.renderShadow)
                .setUniform("uLights.directionalCount", this.directionalLights.size())
                .setUniform("uLights.pointCount", this.pointLights.size())
                .setUniform("uLights.spotCount", this.spotLights.size())
                .setUniform("uGBuffer.colors", 0)
                .setUniform("uGBuffer.normals", 1)
                .setUniform("uGBuffer.emissive", 2)
                .setUniform("uGBuffer.depth", 3)
                .setUniform("uShadow.shadowMap", 4)
                .setUniform("uLights.irradiance", 5)
                .setUniform("uLights.preFilteredMap", 6)
                .setUniform("uLights.brdf", 7);

        if (Objects.nonNull(this.irradianceMap)) {
            this.irradianceMap.bind(5);
        }
        if (Objects.nonNull(this.preFilteredMap)) {
            this.preFilteredMap.bind(6);
        }
        if (this.renderShadow) {
            this.deferredShader.setUniform("uShadow.lightViewProj", this.lightViewProj);
            this.shadowBuffer.getDepthTexture().bind(4);
        }
        for (var i = 0; i < this.directionalLights.size(); i++) {
            this.setDirectionalLight(this.deferredShader, i, this.directionalLights.get(i));
        }
        for (var i = 0; i < this.pointLights.size(); i++) {
            this.setPointLight(this.deferredShader, i, this.pointLights.get(i));
        }
        for (var i = 0; i < this.spotLights.size(); i++) {
            this.setSpotLight(this.deferredShader, i, this.spotLights.get(i));
        }

        glDepthFunc(GL_NOTEQUAL);
        glDepthMask(false);
        this.meshRenderer.render(this.screenQuad);
        glDepthMask(true);
        glDepthFunc(GL_LESS);

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

    private void setDirectionalLight(final Shader shader, final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.directionals[" + index + "].base.color", light.getColor())
                .setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    private void setPointLight(Shader shader, int index, PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity())
                .setUniform("uLights.points[" + index + "].base.color", light.getColor())
                .setUniform("uLights.points[" + index + "].position", light.getPosition())
                .setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    private void setSpotLight(Shader shader, int index, SpotLight light) {
        shader.setUniform("uLights.spots[" + index + "].point.base.intensity", light.getIntensity())
                .setUniform("uLights.spots[" + index + "].point.base.color", light.getColor())
                .setUniform("uLights.spots[" + index + "].point.position", light.getPosition())
                .setUniform("uLights.spots[" + index + "].point.radius", light.getRadius())
                .setUniform("uLights.spots[" + index + "].direction", light.getDirection())
                .setUniform("uLights.spots[" + index + "].cutOff", light.getCutOff())
                .setUniform("uLights.spots[" + index + "].outerCutOff", light.getOuterCutOff());
    }

    private void renderParticles() {

        this.finalBuffer.bind();
        this.particleEmitters.forEach(emitter -> {
            this.particleRenderer.setCamera(this.camera);
            this.particleRenderer.setDirectionalLights(this.directionalLights);
            this.particleRenderer.setPointLights(this.pointLights);
            this.particleRenderer.setSpotLights(this.spotLights);
            this.particleRenderer.render(emitter);
        });
        this.finalBuffer.unbind();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the model contained in component to the list of models to render
     *
     * @see ComponentVisitor#visit(ModelComponent)
     */
    @Override
    public void visit(final ModelComponent component) {
        this.models.add(component.getModel());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the camera contained in component as the camera to use when rendering.
     * Take care if your scene contains several camera then the last visited camera will
     * be the one used
     *
     * @see ComponentVisitor#visit(CameraComponent)
     */
    @Override
    public void visit(final CameraComponent component) {
        this.camera = component.getCamera();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the environment maps to render. Take care, for now only one set of
     * maps can be used. If several {@link EnvironmentComponent} are present
     * in the scene then only the last will be used
     *
     * @param component The component to visit
     */
    @Override
    public void visit(final EnvironmentComponent component) {
        this.environmentMap = component.getEnvironmentMap().orElse(null);
        this.irradianceMap = component.getIrradianceMap().orElse(null);
        this.preFilteredMap = component.getPreFilteredMap().orElse(null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the light contained in component to the list of light to take
     * into account when rendering
     *
     * @see ComponentVisitor#visit(DirectionalLightComponent)
     */
    @Override
    public void visit(final DirectionalLightComponent component) {
        this.directionalLights.add(component.getLight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the light contained in component to the list of light to take
     * into account when rendering
     *
     * @see ComponentVisitor#visit(PointLightComponent)
     */
    @Override
    public void visit(final PointLightComponent component) {
        this.pointLights.add(component.getLight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the light contained in component to the list of light to take
     * into account when rendering
     *
     * @see ComponentVisitor#visit(SpotLightComponent)
     */
    @Override
    public void visit(final SpotLightComponent component) {
        this.spotLights.add(component.getLight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Add the particle emitter contained in component to the list of emitter
     * to render.
     *
     * @see ComponentVisitor#visit(SpotLightComponent)
     */
    @Override
    public void visit(final ParticleComponent component) {
        this.particleEmitters.add(component.getEmitter());
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
