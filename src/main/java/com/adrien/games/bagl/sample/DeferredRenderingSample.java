package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Input;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Skybox;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.light.Attenuation;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.scene.SceneNode;
import com.adrien.games.bagl.rendering.texture.Cubemap;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionColor;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MeshFactory;

public class DeferredRenderingSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Deferred Rendering";
		
		private int width;
		private int height;
		
		private FrameBuffer gbuffer;
		
		private Skybox skybox;
		private Mesh floor;
		private Mesh sphere;
		private SceneNode<Mesh> scene;
		
		private Matrix4 wvp;
		
		private Light ambient;
		private List<DirectionalLight> directionals = new ArrayList<>();
		private List<PointLight> points = new ArrayList<>();
		private List<SpotLight> spots = new ArrayList<>();
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		
		private Shader skyboxShader;
		private Shader gbufferShader;
		private Shader deferredShader;
		private Shader debugShader;
		private Camera camera;
		
		private Spritebatch spritebatch;
		
		private boolean isKeyPressed = false;
		private boolean displayGbuffer = false;
		
		private VertexBuffer lightPositions;
		
		@Override
		public void init() {
			this.width = Configuration.getInstance().getXResolution();
			this.height = Configuration.getInstance().getYResolution();
			this.wvp = new Matrix4();
			
			this.loadMeshes();
			this.initSceneGraph();
			this.initShaders();
			this.setUpLights();
			this.initQuad();
			this.initLightsPosition();

			this.gbuffer = new FrameBuffer(this.width, this.height, 2);

			this.camera = new Camera(new Vector3(0f, 2f, 6f), new Vector3(0f, -2f, -6f), Vector3.UP, 
					(float)Math.toRadians(60f), (float)this.width/(float)this.height, 1, 1000);		
			
			this.spritebatch = new Spritebatch(1024, this.width, this.height);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
			glPointSize(6);
		}
		
		private void loadMeshes() {
			this.skybox = new Skybox(FileUtils.getResourceAbsolutePath("/skybox/left.png"),
					FileUtils.getResourceAbsolutePath("/skybox/right.png"),
					FileUtils.getResourceAbsolutePath("/skybox/bottom.png"),
					FileUtils.getResourceAbsolutePath("/skybox/top.png"),
					FileUtils.getResourceAbsolutePath("/skybox/back.png"),
					FileUtils.getResourceAbsolutePath("/skybox/front.png"));
			this.floor = MeshFactory.fromResourceFile("/models/floor/floor.obj");
			this.sphere = MeshFactory.fromResourceFile("/models/sphere/sphere.obj");
		}
		
		private void initSceneGraph() {
			this.scene = new SceneNode<Mesh>(this.floor);			
			final SceneNode<Mesh> sphereNode = new SceneNode<Mesh>(this.sphere);
			sphereNode.getLocalTransform().setTranslation(new Vector3(0, 0.5f, 0));
			this.scene.addChild(sphereNode);		
		}
		
		private void initShaders() {
			this.skyboxShader = new Shader().addVertexShader("/skybox.vert").addFragmentShader("/skybox.frag").compile();
			this.gbufferShader = new Shader().addVertexShader("/gbuffer.vert").addFragmentShader("/gbuffer.frag").compile();
			this.deferredShader = new Shader().addVertexShader("/deferred.vert").addFragmentShader("/deferred.frag").compile();
			this.debugShader = new Shader().addVertexShader("/debug.vert").addFragmentShader("/debug.frag").compile();
		}
		
		private void initLightsPosition() {
			Vertex[] vertices = new Vertex[this.points.size() + this.spots.size()];
			for(int i = 0; i < this.points.size(); i++) {
				PointLight light = this.points.get(i);
				vertices[i] = new VertexPositionColor(light.getPosition(), light.getColor());
			}
			for(int i = 0; i < this.spots.size(); i++) {
				SpotLight light = this.spots.get(i);
				vertices[i + this.points.size()] = new VertexPositionColor(light.getPosition(), light.getColor());
			}
			this.lightPositions = new VertexBuffer(VertexPositionColor.DESCRIPTION, vertices);
		}

		private void setUpLights() {
			this.ambient = new Light(0.1f);
			this.directionals.add(new DirectionalLight(0.2f, Color.WHITE, new Vector3(0.5f, -2, 4)));
			this.directionals.add(new DirectionalLight(0.2f, Color.TURQUOISE, new Vector3(0.5f, -3, -4)));
			this.points.add(new PointLight(1f, Color.GREEN, new Vector3(4f, 0.5f, 2f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.YELLOW, new Vector3(-4f, 0.2f, 2f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.BLUE, new Vector3(0f, 0.5f, 3f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.PURPLE, new Vector3(0f, 3f, 0f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(2f, Color.TURQUOISE, new Vector3(-1f, 0.1f, 1f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.CYAN, new Vector3(3f, 0.6f, -3f), 7f, Attenuation.CLOSE));
			this.spots.add(new SpotLight(10f, Color.RED, new Vector3(-2f, 0.5f, -3f), 7f, Attenuation.CLOSE, 
					new Vector3(0f, -1f, 0.8f), 20f, 5f));
			this.spots.add(new SpotLight(2f, Color.WHITE, new Vector3(2f, 2f, 2f), 7f, Attenuation.CLOSE, 
					new Vector3(0f, -1f, -0f), 10f, 5f));
			this.spots.add(new SpotLight(1f, Color.ORANGE, new Vector3(-0.5f, 0.5f, 0.5f), 7f, Attenuation.CLOSE, 
					new Vector3(2f, 0.7f, -1f), 20f, 5f));
		}

		private void initQuad() {
			float uOffset = 1/this.width;
			float vOffset = 1/this.height;
			
			Vertex[] vertices = new Vertex[4];
			vertices[0] = new VertexPositionTexture(new Vector3(-1, -1, 0), new Vector2(uOffset, vOffset));
			vertices[1] = new VertexPositionTexture(new Vector3(1, -1, 0), new Vector2(1 - uOffset, vOffset));
			vertices[2] = new VertexPositionTexture(new Vector3(-1, 1, 0), new Vector2(uOffset, 1 - vOffset));
			vertices[3] = new VertexPositionTexture(new Vector3(1, 1, 0), new Vector2(1 - uOffset, 1 - vOffset));

			int[] indices = new int[]{0, 1, 2, 2, 1, 3};
			
			this.indexBuffer = new IndexBuffer(indices);
			this.vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
		}
		
		@Override
		public void update(Time time) {
			//rotating the first child if any
			final Optional<SceneNode<Mesh>> node = this.scene.getChildren().stream().findFirst();
			if(node.isPresent()) {
				node.get().getLocalTransform().getRotation().mul(Quaternion.fromAngleAndVector(
						(float)Math.toRadians(10*time.getElapsedTime()), Vector3.UP));	
			}
			
			if(Input.isKeyPressed(GLFW.GLFW_KEY_SPACE) && !this.isKeyPressed) {
				this.displayGbuffer = !this.displayGbuffer;
				this.isKeyPressed = true;
			}
			if(!Input.isKeyPressed(GLFW.GLFW_KEY_SPACE) && this.isKeyPressed) {
				this.isKeyPressed = false;
			}
		}

		@Override
		public void render() {
			this.renderSkybox();
			this.renderScene();
			this.renderDeferred();
			this.renderLightsPositions();
			
			if(this.displayGbuffer) {
				this.spritebatch.start();
				this.spritebatch.draw(this.gbuffer.getDepthTexture(), new Vector2(0, 2*this.height/3), this.width/3, this.height/3);
				this.spritebatch.draw(this.gbuffer.getColorTexture(0), Vector2.ZERO, this.width/3, this.height/3);
				this.spritebatch.draw(this.gbuffer.getColorTexture(1), new Vector2(0, this.height/3), this.width/3, this.height/3);
				this.spritebatch.end();
			}
		}
		
		private void renderSkybox() {
			this.skybox.getVertexBuffer().bind();
			this.skybox.getIndexBuffer().bind();
			this.skybox.getCubemap().bind();
			this.skyboxShader.bind();
			this.skyboxShader.setUniform("viewProj", this.camera.getViewProjAtOrigin());
			
			glDisable(GL_DEPTH_TEST);
			glDrawElements(GL_TRIANGLES, this.skybox.getIndexBuffer().getSize(), GL_UNSIGNED_INT, 0);
			glEnable(GL_DEPTH_TEST);
			
			Shader.unbind();
			Cubemap.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
		}
		
		private void renderScene() {
			this.gbuffer.bind();
			FrameBuffer.clear();
			this.gbufferShader.bind();			
			this.scene.apply(this::renderSceneNode);
			Shader.unbind();
			FrameBuffer.unbind();
		}
		
		private void renderSceneNode(SceneNode<Mesh> node) {
			if(node.isEmpty()) {
				return;
			}
			
			final Matrix4 world = node.getTransform().getTransformMatrix();
			final Mesh mesh = node.get();
			
			Matrix4.mul(this.camera.getViewProj(), world, this.wvp);
			
			mesh.getVertices().bind();
			mesh.getIndices().bind();
			mesh.getMaterial().applyTo(this.gbufferShader);
			this.gbufferShader.setUniform("uMatrices.world", world);
			this.gbufferShader.setUniform("uMatrices.wvp", this.wvp);
			
			glDrawElements(GL_TRIANGLES, mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Texture.unbind(1);
			Texture.unbind(2);
		}
		
		private void renderDeferred() {
			
			this.gbuffer.getColorTexture(0).bind(0);
			this.gbuffer.getColorTexture(1).bind(1);
			this.gbuffer.getDepthTexture().bind(2);
			this.vertexBuffer.bind();
			this.indexBuffer.bind();
			this.deferredShader.bind();
			this.deferredShader.setUniform("uCamera.vp", this.camera.getViewProj());
			this.deferredShader.setUniform("uCamera.position", this.camera.getPosition());
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
		
		public void renderLightsPositions() {
			this.lightPositions.bind();
			this.debugShader.bind();
			this.debugShader.setUniform("viewProj", this.camera.getViewProj());
			glDrawArrays(GL_POINTS, 0, this.points.size() + this.spots.size());
			Shader.unbind();
			VertexBuffer.unbind();
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
		
		@Override
		public void destroy() {
			this.skybox.destroy();
			this.floor.destroy();
			this.sphere.destroy();
			this.skyboxShader.destroy();
			this.debugShader.destroy();
			this.gbufferShader.destroy();
			this.deferredShader.destroy();
			this.gbuffer.destroy();
			this.indexBuffer.destroy();
			this.vertexBuffer.destroy();
			this.lightPositions.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE).start();
	}
	
}
