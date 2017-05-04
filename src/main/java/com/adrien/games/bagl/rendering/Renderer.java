package com.adrien.games.bagl.rendering;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;

/**
 * Deferred renderer.
 *
 */
public class Renderer {
	
	private static final String DEFERRED_FRAG_SHADER = "/deferred.frag";
	private static final String DEFERRED_VERT_SHADER = "/deferred.vert";
	private static final String GBUFFER_FRAG_SHADER = "/gbuffer.frag";
	private static final String GBUFFER_VERT_SHADER = "/gbuffer.vert";
	private static final String SKYBOX_FRAG_SHADER = "/skybox.frag";
	private static final String SKYBOX_VERT_SHADER = "/skybox.vert";
	
	private final int xResolution;
	private final int yResolution;
	
	private final Matrix4 wvpBuffer;
	
	private Light ambient;
	private final List<DirectionalLight> directionals;
	private final List<PointLight> points;
	private final List<SpotLight> spots;
	
	private VertexBuffer vertexBuffer;
	private IndexBuffer indexBuffer;
	private final FrameBuffer gbuffer;
	
	private Shader skyboxShader;
	private Shader gbufferShader;
	private Shader deferredShader;
	
	private Skybox skybox;
	
	public Renderer() {
		final Configuration config = Configuration.getInstance();
		this.xResolution = config.getXResolution();
		this.yResolution = config.getYResolution();
		
		this.wvpBuffer = Matrix4.createZero();
		
		this.ambient = new Light(1f, Color.WHITE);
		this.directionals = new ArrayList<>();
		this.points = new ArrayList<>();
		this.spots = new ArrayList<>();

		this.initFullScreenQuad();
		this.gbuffer = new FrameBuffer(this.xResolution, this.yResolution, 2);
		
		this.initShaders();
	}
	
	private void initFullScreenQuad() {
		
		final Vertex[] vertices = new Vertex[4];
		vertices[0] = new VertexPositionTexture(new Vector3(-1, -1, 0), new Vector2(0, 0));
		vertices[1] = new VertexPositionTexture(new Vector3(1, -1, 0), new Vector2(1, 0));
		vertices[2] = new VertexPositionTexture(new Vector3(-1, 1, 0), new Vector2(0, 1));
		vertices[3] = new VertexPositionTexture(new Vector3(1, 1, 0), new Vector2(1, 1));

		final int[] indices = new int[]{0, 1, 2, 2, 1, 3};
		
		this.indexBuffer = new IndexBuffer(indices);
		this.vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
	}
	
	private void initShaders() {
		this.skyboxShader = new Shader().addVertexShader(SKYBOX_VERT_SHADER).addFragmentShader(SKYBOX_FRAG_SHADER).compile();
		this.gbufferShader = new Shader().addVertexShader(GBUFFER_VERT_SHADER).addFragmentShader(GBUFFER_FRAG_SHADER).compile();
		this.deferredShader = new Shader().addVertexShader(DEFERRED_VERT_SHADER).addFragmentShader(DEFERRED_FRAG_SHADER).compile();
	}
	
	public void render(SceneNode<Mesh> scene, Camera camera) {
		if(Objects.nonNull(this.skybox)) {			
			this.renderSkybox(camera);
		}
		this.renderScene(scene, camera);
		this.renderDeferred(camera);
	}
	
	private void renderSkybox(Camera camera) {
		this.skybox.getVertexBuffer().bind();
		this.skybox.getIndexBuffer().bind();
		this.skybox.getCubemap().bind();
		this.skyboxShader.bind();
		this.skyboxShader.setUniform("viewProj", camera.getViewProjAtOrigin());
		
		glDisable(GL_DEPTH_TEST);
		glDrawElements(GL_TRIANGLES, this.skybox.getIndexBuffer().getSize(), GL_UNSIGNED_INT, 0);
		glEnable(GL_DEPTH_TEST);
		
		Shader.unbind();
		Cubemap.unbind();
		IndexBuffer.unbind();
		VertexBuffer.unbind();
	}
	
	private void renderScene(SceneNode<Mesh> scene, Camera camera) {
		this.gbuffer.bind();
		FrameBuffer.clear();
		this.gbufferShader.bind();			
		scene.apply(node -> this.renderSceneNode(node, camera));
		Shader.unbind();
		FrameBuffer.unbind();
	}
	
	private void renderSceneNode(SceneNode<Mesh> node, Camera camera) {
		if(node.isEmpty()) {
			return;
		}
		
		final Matrix4 world = node.getTransform().getTransformMatrix();
		final Mesh mesh = node.get();
		
		Matrix4.mul(camera.getViewProj(), world, this.wvpBuffer);
		
		mesh.getVertices().bind();
		mesh.getIndices().bind();
		mesh.getMaterial().applyTo(this.gbufferShader);
		this.gbufferShader.setUniform("uMatrices.world", world);
		this.gbufferShader.setUniform("uMatrices.wvp", this.wvpBuffer);
		
		glDrawElements(GL_TRIANGLES, mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
		
		IndexBuffer.unbind();
		VertexBuffer.unbind();
		Texture.unbind();
		Texture.unbind(1);
		Texture.unbind(2);
	}
	
	private void renderDeferred(Camera camera) {
		
		this.gbuffer.getColorTexture(0).bind(0);
		this.gbuffer.getColorTexture(1).bind(1);
		this.gbuffer.getDepthTexture().bind(2);
		this.vertexBuffer.bind();
		this.indexBuffer.bind();
		this.deferredShader.bind();
		this.deferredShader.setUniform("uCamera.vp", camera.getViewProj());
		this.deferredShader.setUniform("uCamera.position", camera.getPosition());
		this.deferredShader.setUniform("uAmbient.intensity", this.ambient.getIntensity());
		this.deferredShader.setUniform("uAmbient.color", this.ambient.getColor());
		for(int i = 0; i < this.directionals.size(); i++) {				
			this.setDirectionalLight(this.deferredShader, i, this.directionals.get(i));
		}
		for(int i = 0; i < this.points.size(); i++) {
			this.setPointLight(this.deferredShader, i, this.points.get(i));
		}
		for(int i = 0; i < this.spots.size(); i++) {
			this.setSpotLight(this.deferredShader, i, this.spots.get(i));
		}
		this.deferredShader.setUniform("uGBuffer.colors", 0);
		this.deferredShader.setUniform("uGBuffer.normals", 1);
		this.deferredShader.setUniform("uGBuffer.depth", 2);
		
		glDrawElements(GL_TRIANGLES, this.indexBuffer.getSize(), GL_UNSIGNED_INT, 0);
		
		Shader.unbind();
		IndexBuffer.unbind();
		VertexBuffer.unbind();
		Texture.unbind(0);
		Texture.unbind(1);
		Texture.unbind(2);

	}
	
	private void setDirectionalLight(Shader shader, int index, DirectionalLight light) {
		shader.setUniform("uDirectionals[" + index + "].base.intensity", light.getIntensity());
		shader.setUniform("uDirectionals[" + index + "].base.color", light.getColor());
		shader.setUniform("uDirectionals[" + index + "].direction", light.getDirection());
	}
	
	private void setPointLight(Shader shader, int index, PointLight light) {
		shader.setUniform("uPoints[" + index + "].base.intensity", light.getIntensity());
		shader.setUniform("uPoints[" + index + "].base.color", light.getColor());
		shader.setUniform("uPoints[" + index + "].position", light.getPosition());
		shader.setUniform("uPoints[" + index + "].radius", light.getRadius());
		shader.setUniform("uPoints[" + index + "].attenuation.constant", light.getAttenuation().getConstant());
		shader.setUniform("uPoints[" + index + "].attenuation.linear", light.getAttenuation().getLinear());
		shader.setUniform("uPoints[" + index + "].attenuation.quadratic", light.getAttenuation().getQuadratic());
	}
	
	private void setSpotLight(Shader shader, int index, SpotLight light) {
		shader.setUniform("uSpots[" + index + "].point.base.intensity", light.getIntensity());
		shader.setUniform("uSpots[" + index + "].point.base.color", light.getColor());
		shader.setUniform("uSpots[" + index + "].point.position", light.getPosition());
		shader.setUniform("uSpots[" + index + "].point.radius", light.getRadius());
		shader.setUniform("uSpots[" + index + "].point.attenuation.constant", light.getAttenuation().getConstant());
		shader.setUniform("uSpots[" + index + "].point.attenuation.linear", light.getAttenuation().getLinear());
		shader.setUniform("uSpots[" + index + "].point.attenuation.quadratic", light.getAttenuation().getQuadratic());
		shader.setUniform("uSpots[" + index + "].direction", light.getDirection());
		shader.setUniform("uSpots[" + index + "].cutOff", light.getCutOff());
		shader.setUniform("uSpots[" + index + "].outerCutOff", light.getOuterCutOff());
	}
	
	public void destroy() {
		this.skybox.destroy();
		this.skyboxShader.destroy();
		this.gbufferShader.destroy();
		this.deferredShader.destroy();
		this.gbuffer.destroy();
		this.indexBuffer.destroy();
		this.vertexBuffer.destroy();
	}
	
	public void setSkybox(Skybox skybox) {
		this.skybox = skybox;
	}
	
	public void setAmbientLight(Light ambient) {
		this.ambient = ambient;
	}
	
	public FrameBuffer getGBuffer() {
		return this.gbuffer;
	}
	
	public List<DirectionalLight> getDirectionals() {
		return directionals;
	}

	public List<PointLight> getPoints() {
		return points;
	}

	public List<SpotLight> getSpots() {
		return spots;
	}
	
}
