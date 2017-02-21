package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.utils.MeshFactory;

public final class TestLightDir {
	
	private static final class TestGame implements Game {
		
		private static final String TITLE = "Directional Light";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;

		private Mesh mesh;
		private Matrix4 model;
		private Shader shader;
		private Camera camera;		
		
		private DirectionalLight directional;
		
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
			
			this.directional = new DirectionalLight(1.0f, Color.WHITE, new Vector3(0f, -1f, -3f));
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			this.shader.bind();
			this.shader.setUniform("uMatrices.world", this.model);
			this.shader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.shader.setUniform("uEyePosition", this.camera.getPosition());
			this.shader.setUniform("uLight.base.intensity", this.directional.getIntensity());
			this.shader.setUniform("uLight.base.color", this.directional.getColor());
			this.shader.setUniform("uLight.direction", this.directional.getDirection());
			this.shader.setUniform("uMaterial.specularExponent", this.mesh.getMaterial().getSpecularExponent());
			this.shader.setUniform("uMaterial.specularIntensity", this.mesh.getMaterial().getSpecularIntensity());
			
			this.mesh.getMaterial().getDiffuseMap().bind();
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