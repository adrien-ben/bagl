package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.scene.Scene;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;

import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

/**
 * Deferred renderer.
 *
 */
public class Renderer {

    private static final String DEFERRED_FRAG_SHADER = "/deferred.frag";
    private static final String DEFERRED_VERT_SHADER = "/deferred.vert";
    private static final String SHADOW_FRAG_SHADER = "/shadow.frag";
    private static final String SHADOW_VERT_SHADER = "/shadow.vert";
    private static final String GBUFFER_FRAG_SHADER = "/gbuffer.frag";
    private static final String GBUFFER_VERT_SHADER = "/gbuffer.vert";
    private static final String SKYBOX_FRAG_SHADER = "/skybox.frag";
    private static final String SKYBOX_VERT_SHADER = "/skybox.vert";

    private static final int SHADOW_MAP_SIZE = 1024;

    private final int xResolution;
    private final int yResolution;

    private final Matrix4 wvpBuffer;
    private final Matrix4 lightViewProj;

    private VertexBuffer vertexBuffer;
    private final FrameBuffer gbuffer;

    private boolean renderShadow;
    private final FrameBuffer shadowBuffer;

    private Shader skyboxShader;
    private Shader shadowShader;
    private Shader gbufferShader;
    private Shader deferredShader;


    public Renderer() {
        final Configuration config = Configuration.getInstance();
        this.xResolution = config.getXResolution();
        this.yResolution = config.getYResolution();

        this.wvpBuffer = Matrix4.createZero();
        this.lightViewProj = Matrix4.createZero();

        this.initFullScreenQuad();
        this.gbuffer = new FrameBuffer(this.xResolution, this.yResolution, new FrameBufferParameters()
                .addColorOutput(Format.RGBA8).addColorOutput(Format.RGBA16F));
        this.shadowBuffer = new FrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);

        this.initShaders();
    }

    /**
     * Releases resources.
     */
    public void destroy() {
        this.skyboxShader.destroy();
        this.gbufferShader.destroy();
        this.deferredShader.destroy();
        this.gbuffer.destroy();
        this.shadowBuffer.destroy();
        this.vertexBuffer.destroy();
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
        this.skyboxShader = new Shader().addVertexShader(SKYBOX_VERT_SHADER).addFragmentShader(SKYBOX_FRAG_SHADER).compile();
        this.shadowShader = new Shader().addVertexShader(SHADOW_VERT_SHADER).addFragmentShader(SHADOW_FRAG_SHADER).compile();
        this.gbufferShader = new Shader().addVertexShader(GBUFFER_VERT_SHADER).addFragmentShader(GBUFFER_FRAG_SHADER).compile();
        this.deferredShader = new Shader().addVertexShader(DEFERRED_VERT_SHADER).addFragmentShader(DEFERRED_FRAG_SHADER).compile();
    }

    /**
     * <p>Renders a scene from the camera point of view.
     * <p>First, if a skybox is present it is renderer the the default frame buffer.
     * Then, the shadow map is generated for the first available directional light
     * of the scene. After that, the scene is rendered to the gbuffer and finally,
     * the final scene is renderer.
     * @param scene The scene to render.
     * @param camera The camera view the scene.
     */
    public void render(Scene scene, Camera camera) {
        this.renderSkybox(scene.getSkybox(), camera);
        this.renderShadowMap(scene);
        this.renderScene(scene.getRoot(), camera);
        this.renderDeferred(scene, camera);
    }

    private void renderSkybox(Skybox skybox, Camera camera) {
        if(Objects.isNull(skybox)) {
            return;
        }
        skybox.getVertexBuffer().bind();
        skybox.getIndexBuffer().bind();
        skybox.getCubemap().bind();
        this.skyboxShader.bind();
        this.skyboxShader.setUniform("viewProj", camera.getViewProjAtOrigin());

        glDisable(GL_DEPTH_TEST);
        glDrawElements(GL_TRIANGLES, skybox.getIndexBuffer().getSize(), GL_UNSIGNED_INT, 0);
        glEnable(GL_DEPTH_TEST);

        Shader.unbind();
        Cubemap.unbind();
        IndexBuffer.unbind();
        VertexBuffer.unbind();
    }

    private void renderShadowMap(Scene scene) {
        this.renderShadow = !scene.getDirectionals().isEmpty();
        if(this.renderShadow) {
            final Vector3 position = new Vector3(scene.getDirectionals().get(0).getDirection()).scale(-1);
            Matrix4.mul(Matrix4.createOrthographic( -10, 10, -10, 10, 0.1f, 20),
                    Matrix4.createLookAt(position, new Vector3(), Vector3.UP), this.lightViewProj);

            glViewport(0, 0, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
            this.shadowBuffer.bind();
            FrameBuffer.clear();
            this.shadowShader.bind();

            scene.getRoot().apply(node -> this.renderNodeShadow(node, this.lightViewProj));

            Shader.unbind();
            FrameBuffer.unbind();
            glViewport(0, 0, this.xResolution, this.yResolution);

            this.renderShadow = true;
        }
    }

    private void renderNodeShadow(SceneNode<Model> node, Matrix4 lightViewProj) {
        final Model model = node.get();
        final Matrix4 world = node.getTransform().getTransformMatrix();

        Matrix4.mul(lightViewProj, world, this.wvpBuffer);
        this.shadowShader.setUniform("wvp", this.wvpBuffer);

        model.getMeshes().forEach(this::renderMesh);
    }

    private void renderScene(SceneNode<Model> scene, Camera camera) {
        this.gbuffer.bind();
        FrameBuffer.clear();
        this.gbufferShader.bind();
        scene.apply(node -> this.renderSceneNode(node, camera));
        Shader.unbind();
        FrameBuffer.unbind();
    }

    private void renderSceneNode(SceneNode<Model> node, Camera camera) {
        if(node.isEmpty()) {
            return;
        }

        final Matrix4 world = node.getTransform().getTransformMatrix();
        final Model model = node.get();

        Matrix4.mul(camera.getViewProj(), world, this.wvpBuffer);

        this.gbufferShader.setUniform("uMatrices.world", world);
        this.gbufferShader.setUniform("uMatrices.wvp", this.wvpBuffer);

        model.getMeshes().forEach(this::renderMeshToGBuffer);
    }

    private void renderMeshToGBuffer(Mesh mesh) {
        mesh.getMaterial().applyTo(this.gbufferShader);
        this.renderMesh(mesh);
        Texture.unbind();
        Texture.unbind(1);
        Texture.unbind(2);
    }

    private void renderMesh(Mesh mesh) {
        mesh.getVertices().bind();
        mesh.getIndices().bind();
        glDrawElements(GL_TRIANGLES, mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
        IndexBuffer.unbind();
        VertexBuffer.unbind();
    }

    private void renderDeferred(Scene scene, Camera camera) {

        final Light ambient = scene.getAmbient();
        final List<DirectionalLight> directionals = scene.getDirectionals();
        final List<PointLight> points = scene.getPoints();
        final List<SpotLight> spots = scene.getSpots();

        this.gbuffer.getColorTexture(0).bind(0);
        this.gbuffer.getColorTexture(1).bind(1);
        this.gbuffer.getDepthTexture().bind(2);
        this.vertexBuffer.bind();
        this.deferredShader.bind();
        this.deferredShader.setUniform("uCamera.vp", camera.getViewProj());
        this.deferredShader.setUniform("uCamera.position", camera.getPosition());
        this.deferredShader.setUniform("uLights.ambient.intensity", ambient.getIntensity());
        this.deferredShader.setUniform("uLights.ambient.color", ambient.getColor());
        this.deferredShader.setUniform("uShadow.hasShadow", this.renderShadow);
        if(this.renderShadow) {
            this.deferredShader.setUniform("uShadow.lightViewProj", this.lightViewProj);
            this.shadowBuffer.getDepthTexture().bind(3);
            this.deferredShader.setUniform("uShadow.shadowMap", 3);
        }

        this.deferredShader.setUniform("uLights.directionalCount", directionals.size());
        for(int i = 0; i < directionals.size(); i++) {
            this.setDirectionalLight(this.deferredShader, i, directionals.get(i));
        }

        this.deferredShader.setUniform("uLights.pointCount", points.size());
        for(int i = 0; i < points.size(); i++) {
            this.setPointLight(this.deferredShader, i, points.get(i));
        }

        this.deferredShader.setUniform("uLights.spotCount", spots.size());
        for(int i = 0; i < spots.size(); i++) {
            this.setSpotLight(this.deferredShader, i, spots.get(i));
        }

        this.deferredShader.setUniform("uGBuffer.colors", 0);
        this.deferredShader.setUniform("uGBuffer.normals", 1);
        this.deferredShader.setUniform("uGBuffer.depth", 2);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, this.vertexBuffer.getVertexCount());

        Shader.unbind();
        VertexBuffer.unbind();
        Texture.unbind(0);
        Texture.unbind(1);
        Texture.unbind(2);
        Texture.unbind(3);
    }

    private void setDirectionalLight(Shader shader, int index, DirectionalLight light) {
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
        return this.gbuffer;
    }

}
