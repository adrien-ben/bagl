package com.adrien.games.bagl.sample;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPosition;
import com.adrien.games.bagl.utils.MeshFactory;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class SkyboxSample {

	private static final class TestGame implements Game {
		
		private static final String TITLE = "Skybox";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private VertexBuffer vertexBuffer;
		private IndexBuffer indexBuffer; 
		
		private Shader shader;
		private Camera camera;
		private int cubemap;
		
		@Override
		public void init() {

			this.initMesh();
			
			this.shader = new Shader();
			this.shader.addVertexShader("skybox.vert");
			this.shader.addFragmentShader("skybox.frag");
			this.shader.compile();
			
			this.camera = new Camera(Vector3.ZERO, new Vector3(1, -0.5f, 1), Vector3.UP, (float)Math.toRadians(60), 
					(float)WIDTH/HEIGHT, 1, 100);
			
			this.cubemap = glGenTextures();
			glBindTexture(GL_TEXTURE_CUBE_MAP, this.cubemap);
			
			this.loadImageBuffer(new File(MeshFactory.class.getResource("/skybox/left.png").getFile())
					.getAbsolutePath(), GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			this.loadImageBuffer(new File(MeshFactory.class.getResource("/skybox/right.png").getFile())
					.getAbsolutePath(), GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			this.loadImageBuffer(new File(MeshFactory.class.getResource("/skybox/bottom.png").getFile())
					.getAbsolutePath(), GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			this.loadImageBuffer(new File(MeshFactory.class.getResource("/skybox/top.png").getFile())
					.getAbsolutePath(), GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			this.loadImageBuffer(new File(MeshFactory.class.getResource("/skybox/back.png").getFile())
					.getAbsolutePath(), GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
			this.loadImageBuffer(new File(MeshFactory.class.getResource("/skybox/front.png").getFile())
					.getAbsolutePath(), GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
			
			glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
			
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);
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
		
		private void loadImageBuffer(String path, int target) {
			try (InputStream in = Files.newInputStream(Paths.get(path))) {
				PNGDecoder decoder = new PNGDecoder(in);
				int width = decoder.getWidth();
				int height = decoder.getHeight();
				ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width*height*3);
				decoder.decodeFlipped(byteBuffer, width*3, Format.RGB);
				byteBuffer.flip();
				glTexImage2D(target, 0, GL_RGB8, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, byteBuffer);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load texture '" + path + "'.", e);
			}
		}
		
		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			
			this.vertexBuffer.bind();
			this.indexBuffer.bind();
			glBindTexture(GL_TEXTURE_CUBE_MAP, this.cubemap);
			this.shader.bind();
			Matrix4.mul(this.camera.getViewProj(), Matrix4.createRotation(Vector3.UP, 
					(float)Math.toRadians(0.05)), this.camera.getViewProj());
			this.shader.setUniform("viewProj", this.camera.getViewProj());

			glDrawElements(GL_TRIANGLES, this.indexBuffer.getSize(), GL_UNSIGNED_INT, 0);
			
			Shader.unbind();
			glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
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
