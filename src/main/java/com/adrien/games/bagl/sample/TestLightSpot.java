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

public final class TestLightSpot {
	
	private static final class TestGame implements Game {
		
		private static final String TITLE = "Shader";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private Mesh mesh;
		private Matrix4 model;
		private Shader shader;
		private Camera camera;
		
		private Vector3 lightAtten;
		private float lightIntensity;
		private Vector3 lightColor;
		private Vector3 lightPosition;
		private Vector3 lightDirection;
		private float lightCutOff;
		
		private float specExponent;
		private float specIntensity;
		
		@Override
		public void init() {
			
			this.mesh = MeshFactory.createRoom(20, 10, 20);
			
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/spot.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(-3, 2f, 3), new Vector3(3, -2, -3), Vector3.UP, 
					(float)Math.toRadians(60f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);
			
			this.model = new Matrix4();
			
			this.lightAtten = new Vector3(0f, 0f, 0.05f);
			this.lightIntensity = 0.8f;
			this.lightColor = new Vector3(1.f, 1.f, 0.85f);
			this.lightPosition = new Vector3(0f, 2f, -4f);
			this.lightDirection = new Vector3(0, -1, 2);
			this.lightCutOff = (float)Math.cos(Math.toRadians(25));
			
			this.specExponent = 8.f;
			this.specIntensity = 0.6f;
			
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {	
			this.shader.bind();
			this.shader.setUniform("uMatrices.world", this.model);
			this.shader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.shader.setUniform("uLight.point.attenuation", this.lightAtten);
			this.shader.setUniform("uLight.point.base.intensity", this.lightIntensity);
			this.shader.setUniform("uLight.point.base.color", this.lightColor);
			this.shader.setUniform("uLight.point.position", this.lightPosition);
			this.shader.setUniform("uLight.point.range", 10.f);
			this.shader.setUniform("uLight.direction", this.lightDirection);
			this.shader.setUniform("uLight.cutOff", this.lightCutOff);
			this.shader.setUniform("uMaterial.specularExponent", this.specExponent);
			this.shader.setUniform("uMaterial.specularIntensity", this.specIntensity);
			this.shader.setUniform("uEyePosition", this.camera.getPosition());
			
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
