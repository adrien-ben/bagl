package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import com.adrien.games.bagl.core.Camera;
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
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.MeshFactory;

public class MultipleRenderTargetSample {

	private static final class TestGame implements Game {
				
		private static final String TITLE = "MRT";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private FrameBuffer mrt;
		
		private Mesh mesh;
		private Matrix4 world;
		private Matrix4 wvp;
		
		private Shader shader;
		private Camera camera;
		
		private Spritebatch spritebatch;
		
		@Override
		public void init() {

			this.mrt = new FrameBuffer(WIDTH, HEIGHT, 3);
			
			this.mesh = MeshFactory.createBox(5, 5, 5);
			
			this.world = new Matrix4();
			this.wvp = new Matrix4();

			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/mrt.frag");
			this.shader.compile();

			this.camera = new Camera(new Vector3(0, 3, 8), new Vector3(0, -3, -8), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 1, 1000);		
			
			this.spritebatch = new Spritebatch(1024, WIDTH, HEIGHT);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}
		
		@Override
		public void update(Time time) {
			Matrix4.mul(this.camera.getViewProj(), this.world, this.wvp);
		}

		@Override
		public void render() {

			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();

			this.shader.bind();
			this.shader.setUniform("uMatrices.world", this.world);
			this.shader.setUniform("uMatrices.wvp", this.wvp);
			
			this.mrt.bind();
			FrameBuffer.clear();
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			FrameBuffer.unbind();
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		
			this.spritebatch.start();
			this.spritebatch.draw(this.mrt.getColorTexture(0), Vector2.ZERO);
			this.spritebatch.draw(this.mrt.getColorTexture(1), new Vector2(0, 2*HEIGHT/3), WIDTH/3, HEIGHT/3);
			this.spritebatch.draw(this.mrt.getColorTexture(2), Vector2.ZERO, WIDTH/3, HEIGHT/3);
			this.spritebatch.end();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.mesh.destroy();
			this.mrt.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
