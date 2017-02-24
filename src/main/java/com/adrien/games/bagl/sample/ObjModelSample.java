package com.adrien.games.bagl.sample;

import java.io.File;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Camera;
import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.parser.model.ModelParser;
import com.adrien.games.bagl.parser.model.ObjParser;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Mesh;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.texture.Texture;

public class ObjModelSample {

	private static final class TestGame implements Game {
				
		private static final String TITLE = "ObjModel";
		private static final int WIDTH = 512;
		private static final int HEIGHT = WIDTH * 9 / 16;
		
		private ModelParser parser = new ObjParser();
		private Mesh mesh;
		private Matrix4 model;
		private Shader shader;
		private Camera camera;
		
		private float lightIntensity;
		
		@Override
		public void init() {
			this.mesh = parser.parse(new File(TestGame.class.getResource("/cube.obj").getFile()).getAbsolutePath());
			
			this.shader = new Shader();
			this.shader.addVertexShader("/model.vert");
			this.shader.addFragmentShader("/ambient.frag");
			this.shader.compile();
			
			this.camera = new Camera(new Vector3(-2, 2, -2), new Vector3(2, -2, 2), Vector3.UP, 
					(float)Math.toRadians(70f), (float)WIDTH/(float)HEIGHT, 0.1f, 1000f);
			
			this.model = new Matrix4();
			
			this.lightIntensity = .8f;
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			this.shader.bind();
			this.shader.setUniform("uMatrices.world", this.model);
			this.shader.setUniform("uMatrices.wvp", this.camera.getViewProj());
			this.shader.setUniform("uBaseLight.intensity", this.lightIntensity);
			this.shader.setUniform("uBaseLight.color", Color.WHITE);
			this.mesh.getMaterial().applyTo(this.shader);
			
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
