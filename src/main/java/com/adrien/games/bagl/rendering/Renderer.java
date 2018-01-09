package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.environment.EnvironmentMap;
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
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;

import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

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

    private final int xResolution;
    private final int yResolution;
    private final int shadowMapResolution;

    private final Matrix4 wvpBuffer;
    private final Matrix4 lightViewProj;

    private VertexBuffer vertexBuffer;
    private final FrameBuffer gBuffer;

    private boolean renderShadow;
    private final FrameBuffer shadowBuffer;

    private final FrameBuffer finalBuffer;

    private Shader skyboxShader;
    private Shader shadowShader;
    private Shader gBufferShader;
    private Shader deferredShader;

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
        this.gBuffer = new FrameBuffer(this.xResolution, this.yResolution, new FrameBufferParameters().addColorOutput(Format.RGBA8)
                .addColorOutput(Format.RGBA16F));

        this.shadowBuffer = new FrameBuffer(this.shadowMapResolution, this.shadowMapResolution);
        this.finalBuffer = new FrameBuffer(this.xResolution, this.yResolution, new FrameBufferParameters().addColorOutput(Format.RGBA32F));

        this.initShaders();

        this.postProcessor = new PostProcessor(this.xResolution, this.yResolution);
    }

    /**
     * Releases resources.
     */
    public void destroy() {
        this.skyboxShader.destroy();
        this.shadowShader.destroy();
        this.gBufferShader.destroy();
        this.deferredShader.destroy();
        this.gBuffer.destroy();
        this.shadowBuffer.destroy();
        this.vertexBuffer.destroy();
        this.finalBuffer.destroy();
        this.postProcessor.destroy();
    }

    private void initFullScreenQuad() {
        final Vertex[] vertices = new Vertex[4];
        vertices[0] = new VertexPositionTexture(new Vector3(-1, -1, 0), new Vector2(0, 0));
        vertices[1] = new VertexPositionTexture(new Vector3(1, -1, 0), new Vector2(1, 0));
        vertices[2] = new VertexPositionTexture(new Vector3(-1, 1, 0), new Vector2(0, 1));
        vertices[3] = new VertexPositionTexture(new Vector3(1, 1, 0), new Vector2(1, 1));

        this.vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, BufferUsage.STATIC_DRAW, vertices);
    }

    private void initShaders() {
        this.skyboxShader = new Shader().addVertexShader("/environment/environment.vert")
                .addFragmentShader("/environment/environment_cubemap_sample.frag").compile();
        this.shadowShader = new Shader().addVertexShader("/shadow/shadow.vert").addFragmentShader("/shadow/shadow.frag").compile();
        this.gBufferShader = new Shader().addVertexShader("/deferred/gbuffer.vert").addFragmentShader("/deferred/gbuffer.frag").compile();
        this.deferredShader = new Shader().addVertexShader("/deferred/deferred.vert").addFragmentShader("/deferred/deferred.frag").compile();
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
        scene.getEnvironmentMap().ifPresent(map -> this.renderSkybox(map, camera));
        this.renderShadowMap(scene);
        this.renderScene(scene.getRoot(), camera);
        this.renderDeferred(scene, camera);
        this.postProcessor.process(this.finalBuffer.getColorTexture(0));
    }

    private void renderSkybox(final EnvironmentMap skybox, final Camera camera) {
        this.finalBuffer.bind();
        this.finalBuffer.clear();
        glBindVertexArray(skybox.getVaoId());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, skybox.getIboId());
        skybox.getCubemap().bind();
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("viewProj", camera.getViewProjAtOrigin());

        glDisable(GL_DEPTH_TEST);
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, 0);
        glEnable(GL_DEPTH_TEST);

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
        final Optional<EnvironmentMap> irradiance = scene.getIrradianceMap();
        final Light ambient = scene.getAmbient();
        final List<DirectionalLight> directionals = scene.getDirectionals();
        final List<PointLight> points = scene.getPoints();
        final List<SpotLight> spots = scene.getSpots();

        this.finalBuffer.bind();

        this.gBuffer.getColorTexture(0).bind(0);
        this.gBuffer.getColorTexture(1).bind(1);
        this.gBuffer.getDepthTexture().bind(2);

        this.vertexBuffer.bind();
        this.deferredShader.bind();
        this.deferredShader.setUniform("uCamera.vp", camera.getViewProj());
        this.deferredShader.setUniform("uCamera.position", camera.getPosition());
        irradiance.ifPresent(map -> {
            map.getCubemap().bind(4);
            this.deferredShader.setUniform("uLights.irradiance", 4);
        });
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

        glDrawArrays(GL_TRIANGLE_STRIP, 0, this.vertexBuffer.getVertexCount());

        Shader.unbind();
        VertexBuffer.unbind();
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

}
