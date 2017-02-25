package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Skybox;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.texture.Cubemap;

public class SkyboxSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Skybox";
		private static final int WIDTH = 1920;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private Skybox skybox;
		private Shader shader;
		private Camera camera;
		
		private Matrix4 transform;
		
		@Override
		public void init() {
			
			this.shader = new Shader();
			this.shader.addVertexShader("skybox.vert");
			this.shader.addFragmentShader("skybox.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(1000, 0, 0), Vector3.FORWARD, Vector3.UP, (float)Math.toRadians(60), 
					(float)WIDTH/HEIGHT, 1, 100);
			
			this.skybox = new Skybox(this.getResourcePath("/skybox/left.png"),
					this.getResourcePath("/skybox/right.png"),
					this.getResourcePath("/skybox/bottom.png"),
					this.getResourcePath("/skybox/top.png"),
					this.getResourcePath("/skybox/back.png"),
					this.getResourcePath("/skybox/front.png"));
			
			this.transform = new Matrix4();
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
		}
		
		private String getResourcePath(String resource) {
			return new File(TestGame.class.getResource(resource).getFile()).getAbsolutePath();
		}
		
		@Override
		public void update(Time time) {
			Matrix4.mul(this.camera.getViewProj(), Matrix4.createRotation(Vector3.UP, 
					(float)Math.toRadians(5*time.getElapsedTime())), this.camera.getViewProj());
			this.camera.getViewProj().removeTranslation(transform);
		}

		@Override
		public void render() {
			
			this.skybox.getVertexBuffer().bind();
			this.skybox.getIndexBuffer().bind();
			this.skybox.getCubemap().bind();
			this.shader.bind();
			this.shader.setUniform("viewProj", transform);

			glDrawElements(GL_TRIANGLES, this.skybox.getIndexBuffer().getSize(), GL_UNSIGNED_INT, 0);
			
			Shader.unbind();
			Cubemap.unbind();
			IndexBuffer.unbind();
			VertexBuffer.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.skybox.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
