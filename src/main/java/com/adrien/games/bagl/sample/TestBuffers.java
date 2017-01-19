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
import com.adrien.games.bagl.rendering.VertexPositionTexture;

public final class TestBuffers
{
	private final static class TestGame implements Game
	{

		public final static String TITLE = "Buffers";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private VertexBuffer vb;
		private IndexBuffer ib;
		private Shader shader;
		private Texture texture;
		private Matrix4 transform;
		
		@Override
		public void init()
		{
			Vertex[] vertices = new Vertex[4];
			vertices[0] = new VertexPositionTexture(new Vector3(-10,  0, 10), new Vector2(0, 0));
			vertices[1] = new VertexPositionTexture(new Vector3(10,  0, 10), new Vector2(8, 0));
			vertices[2] = new VertexPositionTexture(new Vector3(-10,  0, -10), new Vector2(0, 8));
			vertices[3] = new VertexPositionTexture(new Vector3(10,  0, -10), new Vector2(8, 8));
			int[] indices = new int[]{0, 1, 2, 2, 1, 3 };
			
			ib = new IndexBuffer(indices);
			vb = new VertexBuffer(VertexPositionTexture.DESCRIPTION, vertices);
			
			shader = new Shader();
			shader.addVertexShader("/model.vert");
			shader.addFragmentShader("/texture.frag");
			shader.compile();
			
			texture = new Texture("/default.png");
			
			Matrix4 proj = new Matrix4();
			proj.setPerspective((float)Math.toRadians(70f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000f);
			
			Matrix4 view = new Matrix4();
			view.setLookAt(new Vector3(-10, 5, 20), Vector3.ZERO, Vector3.UP);
			
			transform = Matrix4.mul(proj, view);
		}

		@Override
		public void update(Time time)
		{
			
		}

		@Override
		public void render()
		{			
			shader.bind();
			shader.setUniform("uMatrices.mvp", transform);
			
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
