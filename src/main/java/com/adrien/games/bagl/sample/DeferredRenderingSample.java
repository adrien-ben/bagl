package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Input;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.light.Attenuation;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexPositionTexture;
import com.adrien.games.bagl.utils.MeshFactory;

public class DeferredRenderingSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Deferred Rendering";
		private static final int WIDTH = 1024;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private FrameBuffer gbuffer;
		
		private Mesh plane;
		private Matrix4 world;
		private Mesh cube;
		private Matrix4 cubeWorld;
		
		private Matrix4 wvp;
		
		private Light ambient;
		private List<DirectionalLight> directionals = new ArrayList<>();
		private List<PointLight> points = new ArrayList<>();
		private List<SpotLight> spots = new ArrayList<>();
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		
		private Shader gbufferShader;
		private Shader deferredShader;
		private Camera camera;
		
		private Spritebatch spritebatch;
		
		private boolean isKeyPressed = false;
		private boolean displayGbuffer = false;
		
		@Override
		public void init() {

			this.gbuffer = new FrameBuffer(WIDTH, HEIGHT, 2);
			
			this.plane = MeshFactory.createPlane(10, 10);
			this.world = new Matrix4();
			this.cube = MeshFactory.createBox(1, 1, 1);
			this.cubeWorld = Matrix4.createTranslation(new Vector3(0, 0.5f, 0));
			
			this.wvp = new Matrix4();
			
			this.ambient = new Light(0.1f);
			this.directionals.add(new DirectionalLight(0.2f, Color.WHITE, new Vector3(0.5f, -2, 4)));
			this.directionals.add(new DirectionalLight(0.2f, Color.TURQUOISE, new Vector3(0.5f, -3, -4)));
			this.points.add(new PointLight(1f, Color.GREEN, new Vector3(4f, 0.5f, 2f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.YELLOW, new Vector3(-4f, 0.2f, 2f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.BLUE, new Vector3(0f, 0.5f, 3f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(1f, Color.PURPLE, new Vector3(0f, 2.5f, 1f), 7f, Attenuation.CLOSE));
			this.points.add(new PointLight(2f, Color.TURQUOISE, new Vector3(-1f, 0.1f, 1f), 7f, Attenuation.CLOSE));
			this.spots.add(new SpotLight(10f, Color.RED, new Vector3(-1f, 0.5f, -3f), 7f, Attenuation.CLOSE, 
					new Vector3(0f, -1f, 0.8f), 20f, 5f));
			this.spots.add(new SpotLight(2f, Color.WHITE, new Vector3(2f, 2f, 2f), 7f, Attenuation.CLOSE, 
					new Vector3(0f, -1f, -0f), 10f, 5f));
			this.spots.add(new SpotLight(1f, Color.ORANGE, new Vector3(-0.5f, 0.5f, 0.5f), 7f, Attenuation.CLOSE, 
					new Vector3(2f, 0.7f, -1f), 20f, 5f));
			
			this.initQuad();

			this.gbufferShader = new Shader();
			this.gbufferShader.addVertexShader("/gbuffer.vert");
			this.gbufferShader.addFragmentShader("/gbuffer.frag");
			this.gbufferShader.compile();
			
			this.deferredShader = new Shader();
			this.deferredShader.addVertexShader("/deferred.vert");
			this.deferredShader.addFragmentShader("/deferred.frag");
			this.deferredShader.compile();

			this.camera = new Camera(new Vector3(0f, 2f, 6f), new Vector3(0f, -2f, -6f), Vector3.UP, 
					(float)Math.toRadians(60f), (float)WIDTH/(float)HEIGHT, 1, 1000);		
			
			this.spritebatch = new Spritebatch(1024, WIDTH, HEIGHT);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}
		
		private void initQuad() {
			float uOffset = 1/WIDTH;
			float vOffset = 1/HEIGHT;
			
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
			Matrix4.mul(this.cubeWorld, Matrix4.createRotation(Vector3.UP, (float)Math.toRadians(10*time.getElapsedTime())), this.cubeWorld);
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
			this.renderGbuffer();			
			this.renderDeferred();
			
			if(this.displayGbuffer) {
				this.spritebatch.start();
				this.spritebatch.draw(this.gbuffer.getColorTexture(0), Vector2.ZERO, WIDTH/3, HEIGHT/3);
				this.spritebatch.draw(this.gbuffer.getColorTexture(1), new Vector2(0, HEIGHT/3), WIDTH/3, HEIGHT/3);
				this.spritebatch.draw(this.gbuffer.getDepthTexture(), new Vector2(0, 2*HEIGHT/3), WIDTH/3, HEIGHT/3);
				this.spritebatch.end();
			}
		}

		private void renderGbuffer() {
			
			Matrix4.mul(this.camera.getViewProj(), this.world, this.wvp);
			
			this.plane.getMaterial().getDiffuseTexture().bind();
			this.plane.getVertices().bind();
			this.plane.getIndices().bind();
			this.gbufferShader.bind();
			this.gbufferShader.setUniform("uMatrices.world", this.world);
			this.gbufferShader.setUniform("uMatrices.wvp", this.wvp);
			this.gbuffer.bind();
			FrameBuffer.clear();
			
			glDrawElements(GL_TRIANGLES, this.plane.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			
			Matrix4.mul(this.camera.getViewProj(), this.cubeWorld, this.wvp);
			this.cube.getMaterial().getDiffuseTexture().bind();
			this.cube.getVertices().bind();
			this.cube.getIndices().bind();
			this.gbufferShader.setUniform("uMatrices.world", this.cubeWorld);
			this.gbufferShader.setUniform("uMatrices.wvp", this.wvp);
			
			glDrawElements(GL_TRIANGLES, this.cube.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			
			FrameBuffer.unbind();
			Shader.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
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
			this.deferredShader.setUniform("colors", 0);
			this.deferredShader.setUniform("normals", 1);
			this.deferredShader.setUniform("depth", 2);
			
			glDrawElements(GL_TRIANGLES, this.indexBuffer.getSize(), GL_UNSIGNED_INT, 0);
			
			Shader.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind(0);
			Texture.unbind(1);
			Texture.unbind(2);
		}
		
		private void setDirectionalLight(Shader shader, int index, DirectionalLight light) {
			this.deferredShader.setUniform("uDirectionals[" + index + "].base.intensity", light.getIntensity());
			this.deferredShader.setUniform("uDirectionals[" + index + "].base.color", light.getColor());
			this.deferredShader.setUniform("uDirectionals[" + index + "].direction", light.getDirection());
		}
		
		private void setPointLight(Shader shader, int index, PointLight light) {
			this.deferredShader.setUniform("uPoints[" + index + "].base.intensity", light.getIntensity());
			this.deferredShader.setUniform("uPoints[" + index + "].base.color", light.getColor());
			this.deferredShader.setUniform("uPoints[" + index + "].position", light.getPosition());
			this.deferredShader.setUniform("uPoints[" + index + "].radius", light.getRadius());
			this.deferredShader.setUniform("uPoints[" + index + "].attenuation.constant", light.getAttenuation().getConstant());
			this.deferredShader.setUniform("uPoints[" + index + "].attenuation.linear", light.getAttenuation().getLinear());
			this.deferredShader.setUniform("uPoints[" + index + "].attenuation.quadratic", light.getAttenuation().getQuadratic());
		}
		
		private void setSpotLight(Shader shader, int index, SpotLight light) {
			this.deferredShader.setUniform("uSpots[" + index + "].point.base.intensity", light.getIntensity());
			this.deferredShader.setUniform("uSpots[" + index + "].point.base.color", light.getColor());
			this.deferredShader.setUniform("uSpots[" + index + "].point.position", light.getPosition());
			this.deferredShader.setUniform("uSpots[" + index + "].point.radius", light.getRadius());
			this.deferredShader.setUniform("uSpots[" + index + "].point.attenuation.constant", light.getAttenuation().getConstant());
			this.deferredShader.setUniform("uSpots[" + index + "].point.attenuation.linear", light.getAttenuation().getLinear());
			this.deferredShader.setUniform("uSpots[" + index + "].point.attenuation.quadratic", light.getAttenuation().getQuadratic());
			this.deferredShader.setUniform("uSpots[" + index + "].direction", light.getDirection());
			this.deferredShader.setUniform("uSpots[" + index + "].cutOff", light.getCutOff());
			this.deferredShader.setUniform("uSpots[" + index + "].outerCutOff", light.getOuterCutOff());
		}
		
		@Override
		public void destroy() {
			this.gbufferShader.destroy();
			this.deferredShader.destroy();
			this.plane.destroy();
			this.gbuffer.destroy();
			this.indexBuffer.destroy();
			this.vertexBuffer.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
