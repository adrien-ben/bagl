package com.adrien.games.bagl.sample;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.IndexBuffer;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;
import com.adrien.games.bagl.rendering.Vertex;
import com.adrien.games.bagl.rendering.VertexBuffer;
import com.adrien.games.bagl.rendering.VertexPositionNormalTexture;

public final class TestLightDir
{
	private final static class TestGame implements Game
	{
		public final static String TITLE = "Directional Light";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private VertexBuffer vb;
		private IndexBuffer ib;
		private Shader shader;
		private Texture texture;
		private Matrix4 transform;
		private Matrix4 model;
		private Vector3 eyePosition;
		
		private float lightIntensity;
		private Vector3 lightColor;
		private Vector3 lightDir;
		private float specExponent;
		private float specIntensity;
		
		@Override
		public void init()
		{
			Vertex[] vertices = new Vertex[12];
			vertices[0] = new VertexPositionNormalTexture(new Vector3(-10,  0, 10), new Vector3(0, 1, 0), new Vector2(0, 0));
			vertices[1] = new VertexPositionNormalTexture(new Vector3(10,  0, 10), new Vector3(0, 1, 0), new Vector2(10, 0));
			vertices[2] = new VertexPositionNormalTexture(new Vector3(-10,  0, -10), new Vector3(0, 1, 1), new Vector2(0, 10));
			vertices[3] = new VertexPositionNormalTexture(new Vector3(10,  0, -10), new Vector3(-1, 1, 1), new Vector2(10, 10));
			
			vertices[4] = new VertexPositionNormalTexture(new Vector3(10,  0, -10), new Vector3(-1, 1, 1), new Vector2(0, 0));
			vertices[5] = new VertexPositionNormalTexture(new Vector3(10,  0, 10), new Vector3(-1, 0, 0), new Vector2(10, 0));
			vertices[6] = new VertexPositionNormalTexture(new Vector3(10,  10, 10), new Vector3(-1, 0, 0), new Vector2(10, 5));
			vertices[7] = new VertexPositionNormalTexture(new Vector3(10,  10, -10), new Vector3(-1, 0, 1), new Vector2(0, 5));
			
			vertices[8] = new VertexPositionNormalTexture(new Vector3(-10,  0, -10), new Vector3(0, 1, 1), new Vector2(0, 0));
			vertices[9] = new VertexPositionNormalTexture(new Vector3(10,  0, -10), new Vector3(-1, 1, 1), new Vector2(10, 0));
			vertices[10] = new VertexPositionNormalTexture(new Vector3(10,  10, -10), new Vector3(-1, 0, 1), new Vector2(10, 5));
			vertices[11] = new VertexPositionNormalTexture(new Vector3(-10,  10, -10), new Vector3(0, 0, 1), new Vector2(0, 5));

			int[] indices = new int[]{
					0, 1, 2, 2, 1, 3,
					4, 5, 6, 6, 7, 4,
					8, 9, 10, 10, 11, 8,
					};
			
			ib = new IndexBuffer(indices);
			vb = new VertexBuffer(VertexPositionNormalTexture.DESCRIPTION, vertices);
			
			shader = new Shader();
			shader.addVertexShader("/model.vert");
			shader.addFragmentShader("/directional.frag");
			shader.compile();
			
			texture = new Texture("/default.png");
			
			eyePosition = new Vector3(0, 2, 10);
			
			Matrix4 proj = new Matrix4();
			proj.setPerspective((float)Math.toRadians(70f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000f);
			
			Matrix4 view = new Matrix4();
			view.setLookAt(eyePosition, Vector3.ZERO, Vector3.UP);
			
			transform = Matrix4.mul(proj, view);
			
			model = new Matrix4();
			
			lightIntensity = 1.f;
			lightColor = new Vector3(1.f, 1.f, 1.f);
			lightDir = new Vector3(0f, -1f, -3f);
			specExponent = 32f;
			specIntensity = 2f;
		}

		@Override
		public void update(Time time)
		{
			
		}

		@Override
		public void render()
		{	
			shader.bind();
			shader.setUniform("uMatrices.model", model);
			shader.setUniform("uMatrices.mvp", transform);
			shader.setUniform("uEyePosition", eyePosition);
			shader.setUniform("uLight.base.intensity", lightIntensity);
			shader.setUniform("uLight.base.color", lightColor);
			shader.setUniform("uLight.direction", lightDir);
			shader.setUniform("uMaterial.specularExponent", specExponent);
			shader.setUniform("uMaterial.specularIntensity", specIntensity);
			
			texture.bind();
			vb.bind();
			ib.bind();
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, ib.getSize(), GL11.GL_UNSIGNED_INT, 0);
			
			IndexBuffer.unbind();
			VertexBuffer.unbind();
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy()
		{
			shader.destroy();
			ib.destroy();
			vb.destroy();
			texture.destroy();
		}

	}
	
	public static void main(String [] args)
	{
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
