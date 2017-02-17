package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.CubeMap;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPosition;

public class SkyboxSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Skybox";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer; 
		
		private Shader shader;
		private Camera camera;
		private CubeMap cubemap;
		
		@Override
		public void init() {

			this.initMesh();
			
			this.shader = new Shader();
			this.shader.addVertexShader("skybox.vert");
			this.shader.addFragmentShader("skybox.frag");
			this.shader.compile();
			
			this.camera = new Camera(Vector3.ZERO, new Vector3(0, -0.5f, -1), Vector3.UP, (float)Math.toRadians(60), 
					(float)WIDTH/HEIGHT, 1, 100);
			
			this.cubemap = new CubeMap(this.getResourcePath("/skybox/left.png"),
					this.getResourcePath("/skybox/right.png"),
					this.getResourcePath("/skybox/bottom.png"),
					this.getResourcePath("/skybox/top.png"),
					this.getResourcePath("/skybox/back.png"),
					this.getResourcePath("/skybox/front.png"));
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}
		
		private String getResourcePath(String resource) {
			return new File(TestGame.class.getResource(resource).getFile()).getAbsolutePath();
		}
		
		private void initMesh() {
			Vertex[] vertices = new VertexPosition[] {
				new VertexPosition(new Vector3(-2f, -2f, 2f)),
				new VertexPosition(new Vector3(2f, -2f, 2f)),
				new VertexPosition(new Vector3(-2f, 2f, 2f)),
				new VertexPosition(new Vector3(2f, 2f, 2f)),
				new VertexPosition(new Vector3(-2f, -2f, -2f)),
				new VertexPosition(new Vector3(2f, -2f, -2f)),
				new VertexPosition(new Vector3(-2f, 2f, -2f)),
				new VertexPosition(new Vector3(2f, 2f, -2f))
			};
			
			int[] indices = new int[] {
				1, 0, 3, 3, 0, 2,
				5, 1, 7, 7, 1, 3,
				4, 5, 6, 6, 5, 7,
				0, 4, 2, 2, 4, 6,
				6, 7, 2, 2, 7, 3,
				0, 1, 4, 4, 1, 5
			};
			
			this.vertexBuffer = new VertexBuffer(VertexPosition.DESCRIPTION, vertices);
			this.indexBuffer = new IndexBuffer(indices);
		}
		
		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			
			this.vertexBuffer.bind();
			this.indexBuffer.bind();
			this.cubemap.bind();
			this.shader.bind();
			Matrix4.mul(this.camera.getViewProj(), Matrix4.createRotation(Vector3.UP, 
					(float)Math.toRadians(0.05)), this.camera.getViewProj());
			this.shader.setUniform("viewProj", this.camera.getViewProj());

			glDrawElements(GL_TRIANGLES, this.indexBuffer.getSize(), GL_UNSIGNED_INT, 0);
			
			Shader.unbind();
			CubeMap.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.vertexBuffer.destroy();
			this.indexBuffer.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
