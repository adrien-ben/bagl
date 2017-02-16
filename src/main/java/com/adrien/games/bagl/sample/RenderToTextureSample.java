package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
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

public class RenderToTextureSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "RTT";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		private static final int PADDING_H = (int)(HEIGHT*0.2);
		private static final int PADDING_V = (int)(WIDTH*0.2);
		
		private final static Color BLUEISH = new Color(100f/255, 149f/255, 237f/255);

		private Mesh mesh;
		private Matrix4 world;
		private Matrix4 wvp;
		private float rotation;
		
		private Shader shader;
		private Vector3 color = new Vector3(1, 1, 1);

		private Camera camera;		
		private Spritebatch spritebatch;
		private FrameBuffer frameBuffer;
		
		@Override
		public void init() {
			this.frameBuffer = new FrameBuffer(WIDTH, HEIGHT);

			this.mesh = MeshFactory.createBox(5, 5, 5);
			
			this.world = new Matrix4();
			this.wvp = new Matrix4();
			this.rotation = 0f;
			
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/ambient.frag");
			this.shader.compile();

			this.camera = new Camera(new Vector3(0, 3, 8), new Vector3(0, -3, -8), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 1, 1000);		
			
			this.spritebatch = new Spritebatch(1024, WIDTH, HEIGHT);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}

		@Override
		public void update(Time time) {
			this.rotation += 1/(2*Math.PI)*time.getElapsedTime();
			this.world.setRotation(Vector3.UP, this.rotation);
			Matrix4.mul(this.camera.getViewProj(), this.world, this.wvp);
		}

		@Override
		public void render() {

			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();

			this.renderColors();
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			
			this.spritebatch.start();
			this.spritebatch.draw(this.frameBuffer.getColorTexture(), new Vector2(PADDING_V, PADDING_H), WIDTH - 2*PADDING_V, 
					HEIGHT - 2*PADDING_H);
			this.spritebatch.end();
		}

		private void renderColors() {
			this.shader.bind();
			this.shader.setUniform("uMatrices.world", this.world);
			this.shader.setUniform("uMatrices.wvp", this.wvp);
			this.shader.setUniform("uBaseLight.color", this.color);
			this.shader.setUniform("uBaseLight.intensity", 1);
			this.frameBuffer.bind();
			
			FrameBuffer.clear(BLUEISH);
			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);
			
			FrameBuffer.unbind();
			Shader.unbind();
		}
		
		@Override
		public void destroy() {
			this.shader.destroy();
			this.mesh.destroy();
			this.frameBuffer.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}

}