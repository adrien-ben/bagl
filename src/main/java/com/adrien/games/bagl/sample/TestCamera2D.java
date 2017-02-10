package com.adrien.games.bagl.sample;

import java.io.File;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera2D;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionTexture;

/**
 * Checks that the 2d camera works properly.
 * @author Adrien
 */
public class TestCamera2D {

	private final static class TestGame implements Game {

		public final static String TITLE = "Camera2D";
		public final static int WIDTH = 640;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer;
		private Shader shader;
		private Texture texture;
		private Camera2D camera;
		
		@Override
		public void init() {
			
			this.texture = new Texture(new File(TestGame.class.getResource("/default.png").getFile()).getAbsolutePath());
			float xOffset = 0.5f / this.texture.getWidth();
			float yOffset = 0.5f / this.texture.getHeight();
			
			Vertex v0 = new VertexPositionTexture(new Vector3(-100, -100, 0), new Vector2(xOffset, yOffset));
			Vertex v1 = new VertexPositionTexture(new Vector3(100, -100, 0), new Vector2(1 - xOffset, yOffset));
			Vertex v2 = new VertexPositionTexture(new Vector3(100, 100, 0), new Vector2(1 - xOffset, 1 - yOffset));
			Vertex v3 = new VertexPositionTexture(new Vector3(-100, 100, 0), new Vector2(xOffset, 1 - yOffset));
			this.vertexBuffer = new VertexBuffer(VertexPositionTexture.DESCRIPTION, new Vertex[]{v0, v1, v2, v3});
			
			this.indexBuffer = new IndexBuffer(new int[]{0, 1, 3, 3, 1, 2});
			
			this.shader = new Shader();
			this.shader.addVertexShader("/base.vert");
			this.shader.addFragmentShader("/texture.frag");
			this.shader.compile();
			
			this.camera = new Camera2D(new Vector2(), WIDTH, HEIGHT);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {			
			this.shader.bind();
			this.shader.setUniform("uMVP", this.camera.getOrthographic());
			this.texture.bind();
			this.vertexBuffer.bind();
			this.indexBuffer.bind();
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.indexBuffer.getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.texture.destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
