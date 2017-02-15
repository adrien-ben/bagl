package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import com.adrien.games.bagl.core.Camera;import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.FrameBuffer;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.utils.MeshFactory;

public class FrameBufferSample {

	private final static class TestGame implements Game {
		
		public final static String TITLE = "FrameBuffer";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private final static Color BLUEISH = new Color(100f/255, 149f/255, 237f/255);
		private final static Color GREENISH = new Color(178f/255, 226f/255, 120f/255);

		private Mesh mesh;
		private Matrix4 model;
		private Shader normalShader;
		private Shader colorShader;
		private Shader depthShader;
		private Camera camera;

		private Vector3 color = new Vector3(1, 1, 1);
		
		private Spritebatch spritebatch;
		
		private FrameBuffer normalBuffer;
		private FrameBuffer colorBuffer;
		private FrameBuffer depthBuffer;
		
		@Override
		public void init() {
			this.normalBuffer = new FrameBuffer(WIDTH, HEIGHT);
			this.colorBuffer = new FrameBuffer(WIDTH, HEIGHT);
			this.depthBuffer = new FrameBuffer(WIDTH, HEIGHT);

			this.mesh = MeshFactory.createBox(5, 5, 5);

			this.normalShader = new Shader();
			this.normalShader.addVertexShader("/model.vert");
			this.normalShader.addFragmentShader("/normal.frag");
			this.normalShader.compile();
			
			this.colorShader = new Shader();
			this.colorShader.addVertexShader("/model.vert");
			this.colorShader.addFragmentShader("/ambient.frag");
			this.colorShader.compile();
			
			this.depthShader = new Shader();
			this.depthShader.addVertexShader("/depth.vert");
			this.depthShader.addFragmentShader("/depth.frag");
			this.depthShader.compile();

			this.camera = new Camera(new Vector3(-5, 3, 8), new Vector3(5, -3, -8), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 1, 1000);		

			this.model = new Matrix4();
			
			this.spritebatch = new Spritebatch(1024, WIDTH, HEIGHT);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {

			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();

			this.renderNormals();
			this.renderColors();
			this.renderDepth();
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		
			this.spritebatch.start();
			this.spritebatch.draw(this.colorBuffer.getColorTexture(), Vector2.ZERO);
			this.spritebatch.draw(this.depthBuffer.getColorTexture(), Vector2.ZERO, WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.normalBuffer.getColorTexture(), new Vector2(0, HEIGHT/3), WIDTH/3, HEIGHT/3);
			this.spritebatch.end();
		}
		
		private void renderNormals() {
			this.normalShader.bind();
			this.normalShader.setUniform("uMatrices.model", this.model);
			this.normalShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			
			this.normalBuffer.bind();
			FrameBuffer.clear(GREENISH);
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			FrameBuffer.unbind();
		}

		private void renderColors() {
			this.colorShader.bind();
			this.colorShader.setUniform("uMatrices.model", this.model);
			this.colorShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.colorShader.setUniform("uBaseLight.color", this.color);
			this.colorShader.setUniform("uBaseLight.intensity", 1);
			this.colorShader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			
			this.colorBuffer.bind();
			FrameBuffer.clear(BLUEISH);
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);			
			FrameBuffer.unbind();
		}
		
		private void renderDepth() {
			this.depthShader.bind();
			this.depthShader.setUniform("uMatrices.viewProj", this.camera.getViewProj());
			
			this.depthBuffer.bind();
			FrameBuffer.clear();
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);			
			FrameBuffer.unbind();
		}
		
		@Override
		public void destroy() {
			this.normalShader.destroy();
			this.mesh.destroy();
			this.normalBuffer.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}

}
