package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.postprocess.PostProcessor;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

/**
 * Deferred renderer
 * <p>
 * This renderer supports :
 * <li>one or no skybox</li>
 * <li>one shadow map for one directional light</li>
 * <li>pbr rendering (no IBL yet)</li>
 * <li>small post processing pass (bloom/gamma correction/tone mapping from HDR to SDR</li>
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

    private final Matrix4 wvpBuffer;
    private final Matrix4 lightViewProj;

    private int quadVboId;
    private int quadVaoId;

    private int cubeVboId;
    private int cubeVaoId;
    private int cubeIboId;

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

        this.wvpBuffer = Matrix4.createZero();
        this.lightViewProj = Matrix4.createZero();

        this.initFullScreenQuad();
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
        glDeleteBuffers(this.quadVboId);
        glDeleteVertexArrays(this.quadVaoId);
        glDeleteBuffers(this.cubeIboId);
        glDeleteBuffers(this.cubeVboId);
        glDeleteVertexArrays(this.cubeVaoId);
        this.postProcessor.destroy();
    }

    private void initFullScreenQuad() {
        this.quadVaoId = glGenVertexArrays();
        this.quadVboId = glGenBuffers();
        glBindVertexArray(this.quadVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.quadVboId);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer vertices = stack.floats(
                    -1, -1, 0, 0, 0,
                    1, -1, 0, 1, 0,
                    -1, 1, 0, 0, 1,
                    1, 1, 0, 1, 1);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        }
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.SIZE / 8, 0);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 5 * Float.SIZE / 8, 3 * Float.SIZE / 8);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void initUnitCube() {
        this.cubeIboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cubeIboId);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer indices = stack.bytes(
                    (byte) 1, (byte) 0, (byte) 3, (byte) 3, (byte) 0, (byte) 2,
                    (byte) 5, (byte) 1, (byte) 7, (byte) 7, (byte) 1, (byte) 3,
                    (byte) 4, (byte) 5, (byte) 6, (byte) 6, (byte) 5, (byte) 7,
                    (byte) 0, (byte) 4, (byte) 2, (byte) 2, (byte) 4, (byte) 6,
                    (byte) 6, (byte) 7, (byte) 2, (byte) 2, (byte) 7, (byte) 3,
                    (byte) 0, (byte) 1, (byte) 4, (byte) 4, (byte) 1, (byte) 5
            );
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        this.cubeVaoId = glGenVertexArrays();
        this.cubeVboId = glGenBuffers();
        glBindVertexArray(this.cubeVaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.cubeVboId);
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
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        }
        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(0, 3, GL_BYTE, 3, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void initFrameBuffers() {
        this.gBuffer = new FrameBuffer(this.xResolution, this.yResolution, new FrameBufferParameters().addColorOutput(Format.RGBA8).addColorOutput(Format.RGBA16F));
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
        glBindVertexArray(this.quadVaoId);
        this.brdfShader.bind();

        glViewport(0, 0, BRDF_RESOLUTION, BRDF_RESOLUTION);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glViewport(0, 0, this.xResolution, this.yResolution);

        Shader.unbind();
        glBindVertexArray(0);
        this.brdfBuffer.unbind();
    }

    /**
     * <p>Renders a scene from the camera point of view.
     * <p>First, if a skybox is present it is renderer to the default frame buffer.
     * Then, the shadow map is generated for the first available directional light
     * of the scene. After that, the scene is rendered to the gBuffer and finally,
     * the final scene is renderer.
     *
     * @param scene  The scene to render.
     * @param camera The camera view the scene.
     */
    public void render(final Scene scene, final Camera camera) {
        // FIXME : nothing get renderer when there is no environment map to render ... (check depth test)
        scene.getEnvironmentMap().ifPresent(map -> this.renderSkybox(map, camera));
        this.renderShadowMap(scene);
        this.renderScene(scene.getRoot(), camera);
        this.renderDeferred(scene, camera);
        this.postProcessor.process(this.finalBuffer.getColorTexture(0));
    }

    private void renderSkybox(final Cubemap skybox, final Camera camera) {
        this.finalBuffer.bind();
        this.finalBuffer.clear();
        glBindVertexArray(this.cubeVaoId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.cubeIboId);
        skybox.bind();
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("viewProj", camera.getViewProjAtOrigin());

        glDepthMask(false);
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, 0);
        glDepthMask(true);

        Shader.unbind();
        Cubemap.unbind();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        this.finalBuffer.unbind();
    }

    private void renderShadowMap(final Scene scene) {
        this.renderShadow = !scene.getDirectionals().isEmpty();
        if (this.renderShadow) {
            final Vector3 position = new Vector3(scene.getDirectionals().get(0).getDirection()).scale(-1);
            Matrix4.mul(Matrix4.createOrthographic(-10, 10, -10, 10, 0.1f, 20),
                    Matrix4.createLookAt(position, new Vector3(), Vector3.UP), this.lightViewProj);

            glViewport(0, 0, this.shadowMapResolution, this.shadowMapResolution);
            this.shadowBuffer.bind();
            this.shadowBuffer.clear();
            this.shadowShader.bind();

            scene.getRoot().apply(node -> this.renderNodeShadow(node, this.lightViewProj));

            Shader.unbind();
            this.shadowBuffer.unbind();
            glViewport(0, 0, this.xResolution, this.yResolution);
        }
    }

    private void renderNodeShadow(final SceneNode<Model> node, final Matrix4 lightViewProj) {
        final Model model = node.get();
        final Matrix4 world = node.getTransform().getTransformMatrix();

        Matrix4.mul(lightViewProj, world, this.wvpBuffer);
        this.shadowShader.setUniform("wvp", this.wvpBuffer);

        model.getMeshes().forEach(this::renderMesh);
    }

    private void renderScene(final SceneNode<Model> scene, final Camera camera) {
        this.gBuffer.bind();
        this.gBuffer.clear();
        this.gBufferShader.bind();
        scene.apply(node -> this.renderSceneNode(node, camera));
        Shader.unbind();
        this.gBuffer.unbind();
    }

    private void renderSceneNode(final SceneNode<Model> node, final Camera camera) {
        if (node.isEmpty()) {
            return;
        }

        final Matrix4 world = node.getTransform().getTransformMatrix();
        final Model model = node.get();

        Matrix4.mul(camera.getViewProj(), world, this.wvpBuffer);

        this.gBufferShader.setUniform("uMatrices.world", world);
        this.gBufferShader.setUniform("uMatrices.wvp", this.wvpBuffer);

        model.getMeshes().forEach(this::renderMeshToGBuffer);
    }

    private void renderMeshToGBuffer(final Mesh mesh) {
        mesh.getMaterial().applyTo(this.gBufferShader);
        this.renderMesh(mesh);
        Texture.unbind();
        Texture.unbind(1);
        Texture.unbind(2);
    }

    private void renderMesh(final Mesh mesh) {
        mesh.getVertices().bind();
        mesh.getIndices().bind();
        glDrawElements(GL_TRIANGLES, mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
        IndexBuffer.unbind();
        VertexBuffer.unbind();
    }

    private void renderDeferred(final Scene scene, final Camera camera) {
        final Optional<Cubemap> irradiance = scene.getIrradianceMap();
        final Optional<Cubemap> preFilteredMap = scene.getPreFilteredMap();
        final Light ambient = scene.getAmbient();
        final List<DirectionalLight> directionals = scene.getDirectionals();
        final List<PointLight> points = scene.getPoints();
        final List<SpotLight> spots = scene.getSpots();

        this.finalBuffer.bind();

        this.gBuffer.getColorTexture(0).bind(0);
        this.gBuffer.getColorTexture(1).bind(1);
        this.gBuffer.getDepthTexture().bind(2);

        glBindVertexArray(this.quadVaoId);
        this.deferredShader.bind();
        this.deferredShader.setUniform("uCamera.vp", camera.getViewProj());
        this.deferredShader.setUniform("uCamera.position", camera.getPosition());
        irradiance.ifPresent(map -> {
            map.bind(4);
            this.deferredShader.setUniform("uLights.irradiance", 4);
        });
        preFilteredMap.ifPresent(map -> {
            map.bind(5);
            this.deferredShader.setUniform("uLights.preFilteredMap", 5);
        });
        this.getBrdfBuffer().getColorTexture(0).bind(6);
        this.deferredShader.setUniform("uLights.brdf", 6);
        this.deferredShader.setUniform("uLights.ambient.intensity", ambient.getIntensity());
        this.deferredShader.setUniform("uLights.ambient.color", ambient.getColor());
        this.deferredShader.setUniform("uShadow.hasShadow", this.renderShadow);
        if (this.renderShadow) {
            this.deferredShader.setUniform("uShadow.lightViewProj", this.lightViewProj);
            this.shadowBuffer.getDepthTexture().bind(3);
            this.deferredShader.setUniform("uShadow.shadowMap", 3);
        }

        this.deferredShader.setUniform("uLights.directionalCount", directionals.size());
        for (int i = 0; i < directionals.size(); i++) {
            this.setDirectionalLight(this.deferredShader, i, directionals.get(i));
        }

        this.deferredShader.setUniform("uLights.pointCount", points.size());
        for (int i = 0; i < points.size(); i++) {
            this.setPointLight(this.deferredShader, i, points.get(i));
        }

        this.deferredShader.setUniform("uLights.spotCount", spots.size());
        for (int i = 0; i < spots.size(); i++) {
            this.setSpotLight(this.deferredShader, i, spots.get(i));
        }

        this.deferredShader.setUniform("uGBuffer.colors", 0);
        this.deferredShader.setUniform("uGBuffer.normals", 1);
        this.deferredShader.setUniform("uGBuffer.depth", 2);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        Shader.unbind();
        glBindVertexArray(0);
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

    public FrameBuffer getShadowBuffer() {
        return this.shadowBuffer;
    }

    public FrameBuffer getGBuffer() {
        return this.gBuffer;
    }

    public FrameBuffer getFinalBuffer() {
        return this.finalBuffer;
    }

    public FrameBuffer getBrdfBuffer() {
        return this.brdfBuffer;
    }
}
