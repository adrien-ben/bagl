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

public final class TestLightDir
{
	
	private final static class TestGame implements Game
	{
		public final static String TITLE = "Directional Light";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;

		private Mesh mesh;
		private Matrix4 model;
		private Shader shader;
		private Camera camera;
		
		private float lightIntensity;
		private Vector3 lightColor;
		private Vector3 lightDir;
		private float specExponent;
		private float specIntensity;
		
		@Override
		public void init() {
			this.mesh = MeshFactory.createRoom(20, 10, 20);
			this.model = new Matrix4();
			
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/directional.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(0, 2, 10), new Vector3(0, -2, -10), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);
			
			this.lightIntensity = 1.f;
			this.lightColor = new Vector3(1.f, 1.f, 1.f);
			this.lightDir = new Vector3(0f, -1f, -3f);
			this.specExponent = 32f;
			this.specIntensity = 2f;
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			this.shader.bind();
			this.shader.setUniform("uMatrices.model", this.model);
			this.shader.setUniform("uMatrices.mvp", this.camera.getViewProj());
			this.shader.setUniform("uEyePosition", this.camera.getPosition());
			this.shader.setUniform("uLight.base.intensity", this.lightIntensity);
			this.shader.setUniform("uLight.base.color", this.lightColor);
			this.shader.setUniform("uLight.direction", this.lightDir);
			this.shader.setUniform("uMaterial.specularExponent", this.specExponent);
			this.shader.setUniform("uMaterial.specularIntensity", this.specIntensity);
			
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
