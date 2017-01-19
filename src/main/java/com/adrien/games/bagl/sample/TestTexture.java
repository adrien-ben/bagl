package com.adrien.games.bagl.sample;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Matrix4;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector3;
import com.adrien.games.bagl.rendering.Shader;
import com.adrien.games.bagl.rendering.Texture;

public final class TestTexture
{
	private final static class TestGame implements Game
	{

		public final static String TITLE = "Texture";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private int vao, vbo, ibo;
		private Shader shader;
		private Texture texture;
		private Matrix4 transform;
		
		public TestGame()
		{
			this.shader = null;
		}
		
		@Override
		public void init()
		{
			FloatBuffer fb = BufferUtils.createFloatBuffer(30);
			fb.put(-10);	fb.put(0);	fb.put(10); fb.put(0);	fb.put(0);
			fb.put(10);		fb.put(0);	fb.put(10); fb.put(8);	fb.put(0);
			fb.put(-10);	fb.put(0);	fb.put(-10); fb.put(0);	fb.put(8);
			fb.put(10);		fb.put(0);	fb.put(-10); fb.put(8);	fb.put(8);
			fb.flip();
			
			IntBuffer ibuff = BufferUtils.createIntBuffer(6);
			ibuff.put(0);	ibuff.put(1);	ibuff.put(2);
			ibuff.put(2);	ibuff.put(1);	ibuff.put(3);
			ibuff.flip();
			
			vao = GL30.glGenVertexArrays();
			vbo = GL15.glGenBuffers();
			ibo = GL15.glGenBuffers();
			
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, ibuff, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			
			GL30.glBindVertexArray(this.vao);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
			
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT,  false, 5*4, 0);
			GL20.glEnableVertexAttribArray(2);
			GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT,  false, 5*4, 3*4);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
			
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
			
			GL30.glBindVertexArray(this.vao);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
			
			GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
			
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
			Texture.unbind();
			Shader.unbind();
		}

		@Override
		public void destroy()
		{
			shader.destroy();
			GL15.glDeleteBuffers(ibo);
			GL15.glDeleteBuffers(vbo);
			GL30.glDeleteVertexArrays(vao);
			texture.destroy();
		}

	}
	
	public static void main(String [] args)
	{
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
