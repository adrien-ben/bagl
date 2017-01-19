package com.adrien.games.bagl.sample;

import java.nio.FloatBuffer;

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

/**
 * @author Adrien
 * Checks that the Shader class works properly
 */
public final class TestShader
{
	private final static class TestGame implements Game
	{

		public final static String TITLE = "Shader";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private int vao, vbo;
		private Shader shader;
		private Matrix4 transform;
		
		public TestGame()
		{
			this.shader = null;
		}
		
		@Override
		public void init()
		{
			FloatBuffer fb = BufferUtils.createFloatBuffer(18);
			fb.put(-1);	fb.put(-1);	fb.put(0); fb.put(1);	fb.put(0);	fb.put(0);
			fb.put(1);	fb.put(-1);	fb.put(0); fb.put(0);	fb.put(1);	fb.put(0);
			fb.put(-1);	fb.put(1);	fb.put(0); fb.put(0);	fb.put(0);	fb.put(1);
			fb.flip();
			
			vao = GL30.glGenVertexArrays();
			vbo = GL15.glGenBuffers();
			
			GL30.glBindVertexArray(this.vao);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vbo);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fb, GL15.GL_STATIC_DRAW);
			
			GL20.glEnableVertexAttribArray(0);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT,  false, 6*4, 0);
			
			GL20.glEnableVertexAttribArray(1);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT,  false, 6*4, 3*4);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
			
			shader = new Shader();
			shader.addVertexShader("/base.vert");
			shader.addFragmentShader("/base.frag");
			shader.compile();
			
			Matrix4 proj = new Matrix4();
			proj.setPerspective((float)Math.toRadians(50f), (float)WIDTH / (float)HEIGHT, 0.1f, 1000f);
			
			Matrix4 view = new Matrix4();
			view.setLookAt(new Vector3(0, 0, 5), new Vector3(0, 0, 0), new Vector3(0, 1, 0));
			
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
			shader.setUniform("uValue", 0.9f);
			shader.setUniform("uMVP", transform);
			
			GL30.glBindVertexArray(this.vao);
			
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
			
			GL30.glBindVertexArray(0);
			
			Shader.unbind();
		}

		@Override
		public void destroy()
		{
			this.shader.destroy();
		}

	}
	
	public static void main(String [] args)
	{
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
