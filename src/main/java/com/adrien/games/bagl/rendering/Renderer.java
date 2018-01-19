package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.core.math.Vectors;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.model.Mesh;
import com.adrien.games.bagl.rendering.model.MeshFactory;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.postprocess.PostProcessor;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Deferred renderer
 * <p>
 * This renderer supports :
 * <li>HDR Skybox</li>
 * <li>Shadow mapping for one directional light</li>
 * <li>PBR rendering with IBL</li>
 * <li>Post processing pass (bloom/gamma correction/tone mapping from HDR to SDR</li>
 *
 * @author adrien
 */
public class Renderer {

    private static final int BRDF_RESOLUTION = 512;
    private final static byte UNIT_CUBE_POS_HALF_SIZE = (byte) 1;
    private final static byte UNIT_CUBE_NEG_HALF_SIZE = (byte) -1;

    private final int xResolution;
    private final int yResolution;
    private final int shadowMapResolution;

    private Camera camera;
    private final List<DirectionalLight> directionalLights;
    private final List<PointLight> pointLights;
    private final List<SpotLight> spotLights;
    private final Map<Model, Matrix4f> models;

    private final Matrix4f wvpBuffer;
    private final Matrix4f lightViewProj;

    private Mesh screenQuad;

    private VertexArray cubeVArray;
    private VertexBuffer cubeVBuffer;
    private IndexBuffer iBuffer;

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

    private PostProcessor postProcessor;

    /**
     * Constructs the renderer
     */
    public Renderer() {
        final Configuration config = Configuration.getInstance();
        this.xResolution = config.getXResolution();
        this.yResolution = config.getYResolution();
        this.shadowMapResolution = config.getShadowMapResolution();

        this.camera = null;
        this.directionalLights = new ArrayList<>();
        this.pointLights = new ArrayList<>();
        this.spotLights = new ArrayList<>();
        this.models = new LinkedHashMap<>();

        this.wvpBuffer = new Matrix4f();
        this.lightViewProj = new Matrix4f();

        this.screenQuad = MeshFactory.createScreenQuad();
        this.initUnitCube();
        this.initFrameBuffers();
        this.initShaders();
        this.bakeBRDFIntegration();

        this.postProcessor = new PostProcessor(this.xResolution, this.yResolution);
    }

    /**
     * Releases resources
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
        this.iBuffer.destroy();
        this.cubeVBuffer.destroy();
        this.cubeVArray.destroy();
        this.postProcessor.destroy();
    }

    private void initUnitCube() {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer indices = stack.bytes(
                    (byte) 1, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 2,
                    (byte) 5, (byte) 1, (byte) 7, (byte) 7, (byte) 1, (byte) 3,
                    (byte) 4, (byte) 5, (byte) 6, (byte) 6, (byte) 5, (byte) 7,
                    (byte) 0, (byte) 4, (byte) 2, (byte) 2, (byte) 4, (byte) 6,
                    (byte) 6, (byte) 7, (byte) 2, (byte) 2, (byte) 7, (byte) 3,
                    (byte) 0, (byte) 1, (byte) 4, (byte) 4, (byte) 1, (byte) 5
            );
            this.iBuffer = new IndexBuffer(indices, BufferUsage.STATIC_DRAW);
        }

        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer vertices = stack.bytes(
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE,
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE,
                    UNIT_CUBE_NEG_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE,
                    UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_POS_HALF_SIZE, UNIT_CUBE_NEG_HALF_SIZE);
            this.cubeVBuffer = new VertexBuffer(vertices, new VertexBufferParams()
                    .dataType(DataType.BYTE)
                    .element(new VertexElement(0, 3)));
        }

        this.cubeVArray = new VertexArray();
        this.cubeVArray.bind();
        this.cubeVArray.attachVertexBuffer(this.cubeVBuffer);
        this.cubeVArray.unbind();
    }

    private void initFrameBuffers() {
        this.gBuffer = new FrameBuffer(this.xResolution, this.yResolution,
                new FrameBufferParameters().addColorOutput(Format.RGBA8).addColorOutput(Format.RGBA16F).addColorOutput(Format.RGB16F));
        this.shadowBuffer = new FrameBuffer(this.shadowMapResolution, this.shadowMapResolution);
        this.finalBuffer = new FrameBuffer(this.xResolution, this.yResolution, new FrameBufferParameters().addColorOutput(Format.RGBA32F));
        this.brdfBuffer = new FrameBuffer(BRDF_RESOLUTION, BRDF_RESOLUTION, new FrameBufferParameters().hasDepth(false).addColorOutput(Format.RG16F));
    }

    private void initShaders() {
        this.skyboxShader = new Shader()
                .addVertexShader("/environment/environment.vert")
                .addFragmentShader("/environment/environment_cubemap_sample.frag")
                .compile();
        this.shadowShader = new Shader()
                .addVertexShader("/shadow/shadow.vert")
                .addFragmentShader("/shadow/shadow.frag")
                .compile();
        this.gBufferShader = new Shader()
                .addVertexShader("/deferred/gbuffer.vert")
                .addFragmentShader("/deferred/gbuffer.frag")
                .compile();
        this.deferredShader = new Shader()
                .addVertexShader("/deferred/deferred.vert")
                .addFragmentShader("/deferred/deferred.frag")
                .compile();
        this.brdfShader = new Shader()
                .addVertexShader("/post/post_process.vert")
                .addFragmentShader("/environment/brdf_integration.frag")
                .compile();
    }

    private void bakeBRDFIntegration() {
        this.brdfBuffer.bind();
        this.brdfShader.bind();

        glViewport(0, 0, BRDF_RESOLUTION, BRDF_RESOLUTION);
        this.renderMesh(this.screenQuad);
        glViewport(0, 0, this.xResolution, this.yResolution);

        Shader.unbind();
        this.brdfBuffer.unbind();
    }

    /**
     * Renders a scene from the camera point of view
     * <p>
     * First, if a skybox is present it is renderer to the default frame buffer.
     * Then, the shadow map is generated for the first available directional light
     * of the scene. After that, the scene is rendered to the gBuffer and finally,
     * the final scene is renderer
     *
     * @param scene The scene to render
     */
    public void render(final Scene scene) {
        this.camera = null;
        this.directionalLights.clear();
        this.pointLights.clear();
        this.spotLights.clear();
        this.models.clear();

        scene.getRoot().traverse(this);

        if (Objects.isNull(this.camera)) {
            throw new EngineException("Impossible to render a scene if no camera is set up");
        }
        scene.getEnvironmentMap().ifPresent(map -> this.renderSkybox(map, this.camera));
        this.renderShadowMap();
        this.renderScene(this.camera);
        this.renderDeferred(scene, this.camera);
        this.postProcessor.process(this.finalBuffer.getColorTexture(0));
    }

    private void renderSkybox(final Cubemap skybox, final Camera camera) {
        this.finalBuffer.bind();
        this.finalBuffer.clear();
        this.cubeVArray.bind();
        this.iBuffer.bind();
        skybox.bind();
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("viewProj", camera.getViewProjAtOrigin());

        glDepthMask(false);
        glDrawElements(GL_TRIANGLES, this.iBuffer.getSize(), this.iBuffer.getDataType().getGlCode(), 0);
        glDepthMask(true);

        Shader.unbind();
        Cubemap.unbind();
        this.iBuffer.unbind();
        this.cubeVArray.unbind();
        this.finalBuffer.unbind();
    }

    private void renderShadowMap() {
        this.renderShadow = !this.directionalLights.isEmpty();
        if (this.renderShadow) {
            final Vector3f position = new Vector3f(this.directionalLights.get(0).getDirection()).mul(-10f);

            this.lightViewProj.setOrtho(-10, 10, -10, 10, 0.1f, 20f)
                    .lookAt(position, Vectors.VEC3_ZERO, Vectors.VEC3_UP);

            glViewport(0, 0, this.shadowMapResolution, this.shadowMapResolution);
            this.shadowBuffer.bind();
            this.shadowBuffer.clear();
            this.shadowShader.bind();

            this.models.forEach((model, transform) -> this.renderNodeShadow(model, transform, this.lightViewProj));

            Shader.unbind();
            this.shadowBuffer.unbind();
            glViewport(0, 0, this.xResolution, this.yResolution);
        }
    }

    private void renderNodeShadow(final Model model, final Matrix4f transform, final Matrix4f lightViewProj) {
        lightViewProj.mul(transform, this.wvpBuffer);
        this.shadowShader.setUniform("wvp", this.wvpBuffer);
        model.getMeshes().keySet().forEach(this::renderMesh);
    }

    private void renderScene(final Camera camera) {
        this.gBuffer.bind();
        this.gBuffer.clear();
        this.gBufferShader.bind();

        this.models.forEach((model, transform) -> this.renderSceneNode(model, transform, camera));

        Shader.unbind();
        this.gBuffer.unbind();
    }

    private void renderSceneNode(final Model model, final Matrix4f transform, final Camera camera) {
        camera.getViewProj().mul(transform, this.wvpBuffer);

        this.gBufferShader.setUniform("uMatrices.world", transform);
        this.gBufferShader.setUniform("uMatrices.wvp", this.wvpBuffer);

        model.getMeshes().forEach(this::renderMeshToGBuffer);
    }

    private void renderMeshToGBuffer(final Mesh mesh, final Material material) {
        material.applyTo(this.gBufferShader);
        this.renderMesh(mesh);
        Texture.unbind();
        Texture.unbind(1);
        Texture.unbind(2);
    }

    private void renderMesh(final Mesh mesh) {
        mesh.getVertexArray().bind();
        final Optional<IndexBuffer> iBufferOpt = mesh.getIndexBuffer();
        if (iBufferOpt.isPresent()) {
            final IndexBuffer iBuffer = iBufferOpt.get();
            iBuffer.bind();
            glDrawElements(mesh.getPrimitiveType().getGlCode(), iBuffer.getSize(), iBuffer.getDataType().getGlCode(), 0);
            iBuffer.unbind();
        } else {
            glDrawArrays(mesh.getPrimitiveType().getGlCode(), 0, mesh.getVertexCount());
        }
        mesh.getVertexArray().unbind();
    }

    private void renderDeferred(final Scene scene, final Camera camera) {
        final Optional<Cubemap> irradiance = scene.getIrradianceMap();
        final Optional<Cubemap> preFilteredMap = scene.getPreFilteredMap();

        this.finalBuffer.bind();
        if (!scene.getEnvironmentMap().isPresent()) {
            this.finalBuffer.clear();
        }

        this.gBuffer.getColorTexture(0).bind(0);
        this.gBuffer.getColorTexture(1).bind(1);
        this.gBuffer.getColorTexture(2).bind(2);
        this.gBuffer.getDepthTexture().bind(3);

        this.deferredShader.bind();
        this.deferredShader.setUniform("uCamera.invertedViewProj", camera.getInvertedViewProj());
        this.deferredShader.setUniform("uCamera.position", camera.getPosition());
        irradiance.ifPresent(map -> {
            map.bind(5);
            this.deferredShader.setUniform("uLights.irradiance", 5);
        });
        preFilteredMap.ifPresent(map -> {
            map.bind(6);
            this.deferredShader.setUniform("uLights.preFilteredMap", 6);
        });
        this.brdfBuffer.getColorTexture(0).bind(7);
        this.deferredShader.setUniform("uLights.brdf", 7);
        this.deferredShader.setUniform("uShadow.hasShadow", this.renderShadow);
        if (this.renderShadow) {
            this.deferredShader.setUniform("uShadow.lightViewProj", this.lightViewProj);
            this.shadowBuffer.getDepthTexture().bind(4);
            this.deferredShader.setUniform("uShadow.shadowMap", 4);
        }

        this.deferredShader.setUniform("uLights.directionalCount", this.directionalLights.size());
        for (int i = 0; i < this.directionalLights.size(); i++) {
            this.setDirectionalLight(this.deferredShader, i, this.directionalLights.get(i));
        }

        this.deferredShader.setUniform("uLights.pointCount", this.pointLights.size());
        for (int i = 0; i < this.pointLights.size(); i++) {
            this.setPointLight(this.deferredShader, i, this.pointLights.get(i));
        }

        this.deferredShader.setUniform("uLights.spotCount", this.spotLights.size());
        for (int i = 0; i < this.spotLights.size(); i++) {
            this.setSpotLight(this.deferredShader, i, this.spotLights.get(i));
        }

        this.deferredShader.setUniform("uGBuffer.colors", 0);
        this.deferredShader.setUniform("uGBuffer.normals", 1);
        this.deferredShader.setUniform("uGBuffer.emissive", 2);
        this.deferredShader.setUniform("uGBuffer.depth", 3);

        this.renderMesh(this.screenQuad);

        Shader.unbind();
        Cubemap.unbind(4);
        Texture.unbind(0);
        Texture.unbind(1);
        Texture.unbind(2);
        Texture.unbind(3);
        this.finalBuffer.unbind();
    }

    private void setDirectionalLight(final Shader shader, final int index, final DirectionalLight light) {
        shader.setUniform("uLights.directionals[" + index + "].base.intensity", light.getIntensity());
        shader.setUniform("uLights.directionals[" + index + "].base.color", light.getColor());
        shader.setUniform("uLights.directionals[" + index + "].direction", light.getDirection());
    }

    private void setPointLight(Shader shader, int index, PointLight light) {
        shader.setUniform("uLights.points[" + index + "].base.intensity", light.getIntensity());
        shader.setUniform("uLights.points[" + index + "].base.color", light.getColor());
        shader.setUniform("uLights.points[" + index + "].position", light.getPosition());
        shader.setUniform("uLights.points[" + index + "].radius", light.getRadius());
    }

    private void setSpotLight(Shader shader, int index, SpotLight light) {
        shader.setUniform("uLights.spots[" + index + "].point.base.intensity", light.getIntensity());
        shader.setUniform("uLights.spots[" + index + "].point.base.color", light.getColor());
        shader.setUniform("uLights.spots[" + index + "].point.position", light.getPosition());
        shader.setUniform("uLights.spots[" + index + "].point.radius", light.getRadius());
        shader.setUniform("uLights.spots[" + index + "].direction", light.getDirection());
        shader.setUniform("uLights.spots[" + index + "].cutOff", light.getCutOff());
        shader.setUniform("uLights.spots[" + index + "].outerCutOff", light.getOuterCutOff());
    }

    public void addDirectionalLight(final DirectionalLight light) {
        this.directionalLights.add(light);
    }

    public void addPointLight(final PointLight light) {
        this.pointLights.add(light);
    }

    public void addSpotLight(final SpotLight light) {
        this.spotLights.add(light);
    }

    public void addModel(final Matrix4f transform, final Model model) {
        this.models.put(model, transform);
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

    public void setCamera(final Camera camera) {
        this.camera = camera;
    }
}
