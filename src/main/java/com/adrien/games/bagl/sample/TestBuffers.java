package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.utils.MeshFactory;

public final class TestBuffers {
	
	private final static class TestGame implements Game {

		public final static String TITLE = "Buffers";
		public final static int WIDTH = 512;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private Mesh mesh;
		private Shader shader;
		private Camera camera;
		
		@Override
		public void init() {
			this.mesh = MeshFactory.createPlane(20, 20);
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/texture.frag");
			this.shader.compile();			
			this.camera = new Camera(new Vector3(-10, 5, 20), new Vector3(10, -5, -20), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);
		}

		@Override
		public void update(Time time) {
			
		}

		@Override
		public void render() {			
			this.shader.bind();
			this.shader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			
			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.mesh.getIndices().getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.mesh.destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
