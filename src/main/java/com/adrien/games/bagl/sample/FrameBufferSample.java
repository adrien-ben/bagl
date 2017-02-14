package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.utils.MeshFactory;

public class FrameBufferSample {

	private final static class TestGame implements Game {

		private static final Logger log = LogManager.getLogger(TestGame.class);
		
		public final static String TITLE = "FrameBuffer";
		public final static int WIDTH = 512;
		public final static int HEIGHT = WIDTH * 9 / 16;

		private Mesh mesh;
		private Matrix4 model;
		private Shader shader;
		private Camera camera;

		private Spritebatch spritebatch;
		
		private Texture colorBuffer;
		private int frameBuffer;
		private int depthBuffer;
		
		@Override
		public void init() {
			this.initFrameBuffer();

			this.mesh = MeshFactory.createBox(5, 5, 5);

			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/normal.frag");
			this.shader.compile();

			this.camera = new Camera(new Vector3(-5, 3, 8), new Vector3(5, -3, -8), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);		

			this.model = new Matrix4();
			
			this.spritebatch = new Spritebatch(1024, WIDTH, HEIGHT);

			glEnable(GL_CULL_FACE);
			glEnable(GL_DEPTH_TEST);
		}

		private void initFrameBuffer() {
			this.colorBuffer = new Texture(WIDTH, HEIGHT);
			this.colorBuffer.bind();
			
			this.frameBuffer = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, this.frameBuffer);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, this.colorBuffer.getHandle(), 0);

			this.depthBuffer = glGenRenderbuffers();
			glBindRenderbuffer(GL_RENDERBUFFER, this.depthBuffer);
			glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, WIDTH, HEIGHT);

			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, this.depthBuffer);
			
			int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
			log.info("Status : {}", status);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			this.shader.bind();
			this.shader.setUniform("uMatrices.model", this.model);
			this.shader.setUniform("uMatrices.mvp", this.camera.getViewProj());

			this.mesh.getMaterial().getDiffuseTexture().bind();
			this.mesh.getVertices().bind();
			this.mesh.getIndices().bind();

			glBindFramebuffer(GL_FRAMEBUFFER, this.frameBuffer);
			glClearColor(100f/255, 149f/255, 237f/255, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glDrawElements(GL_TRIANGLES, this.mesh.getIndices().getSize(), GL_UNSIGNED_INT, 0);

			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		
			this.spritebatch.start();
			this.spritebatch.draw(this.colorBuffer, Vector2.ZERO, WIDTH/2, HEIGHT/2);
			this.spritebatch.end();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
			this.mesh.destroy();
			this.destroyFrameBuffer();
		}

		private void destroyFrameBuffer() {
			glDeleteFramebuffers(this.frameBuffer);
			glDeleteRenderbuffers(this.depthBuffer);
			this.colorBuffer.destroy();
		}

	}

	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}

}
