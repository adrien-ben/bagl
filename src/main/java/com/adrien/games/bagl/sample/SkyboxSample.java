package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;

import java.io.File;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Skybox;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.texture.Cubemap;

public class SkyboxSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Skybox";
		
		private int width;
		private int height;
		
		private Skybox skybox;
		private Shader shader;
		private Camera camera;
		
		@Override
		public void init() {
			this.width = Configuration.getInstance().getXResolution();
			this.height = Configuration.getInstance().getYResolution();
			
			this.shader = new Shader();
			this.shader.addVertexShader("skybox.vert");
			this.shader.addFragmentShader("skybox.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(1000, 0, 0), Vector3.FORWARD, Vector3.UP, (float)Math.toRadians(60), 
					(float)this.width/this.height, 1, 100);
			
			this.skybox = new Skybox(this.getResourcePath("/skybox/left.png"),
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
		
		@Override
		public void update(Time time) {			
			this.camera.rotate(Quaternion.fromAngleAndVector((float)Math.toRadians(-5*time.getElapsedTime()), Vector3.UP));
		}

		@Override
		public void render() {
			this.skybox.getVertexBuffer().bind();
			this.skybox.getIndexBuffer().bind();
			this.skybox.getCubemap().bind();
			this.shader.bind();
			this.shader.setUniform("viewProj", this.camera.getViewProjAtOrigin());

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
		new Engine(new TestGame(), TestGame.TITLE).start();
	}
	
}
