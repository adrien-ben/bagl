package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.utils.MeshFactory;

public final class TestLightAmbient {
	
	private final static class TestGame implements Game {
		public final static String TITLE = "Ambient Light";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;

		private Mesh mesh;
		private Matrix4 model;
		private Shader shader;
		private Camera camera;
		
		private float lightIntensity;
		private Vector3 lightColor;
		
		@Override
		public void init() {
			this.mesh = MeshFactory.createBox(5, 5, 5);
			
			this.shader = new Shader();
			this.shader.addVertexShader("/ambient.vert");
			this.shader.addFragmentShader("/ambient.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(-10, 5, 10), new Vector3(10, -5, -10), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);		
			
			this.model = new Matrix4();
			
			this.lightIntensity = .3f;
			this.lightColor = new Vector3(1.f, 1f, 1.f);
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {	
			this.shader.bind();
			this.shader.setUniform("uMatrices.model", this.model);
			this.shader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.shader.setUniform("uBaseLight.intensity", this.lightIntensity);
			this.shader.setUniform("uBaseLight.color", this.lightColor);
			
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
