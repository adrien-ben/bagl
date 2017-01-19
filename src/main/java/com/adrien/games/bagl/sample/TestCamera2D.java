package com.adrien.games.bagl.sample;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.adrien.games.bagl.core.Camera2D;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.rendering.Shader;

/**
 * Checks that the 2d camera works properly.
 * @author Adrien
 */
public class TestCamera2D {

	private final static class TestGame implements Game {

		public final static String TITLE = "Camera2D";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		private int vao, vbo;
		private Shader shader;
		private Camera2D camera;
		private Vector2 movement;
		
		@Override
		public void init() {
			FloatBuffer fb = BufferUtils.createFloatBuffer(18);
			fb.put(0);		fb.put(0);		fb.put(0); fb.put(1);	fb.put(0);	fb.put(0);
			fb.put(100);	fb.put(0);		fb.put(0); fb.put(0);	fb.put(1);	fb.put(0);
			fb.put(0);		fb.put(100);	fb.put(0); fb.put(0);	fb.put(0);	fb.put(1);
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
			
			camera = new Camera2D(new Vector2(), WIDTH, HEIGHT);
			movement = new Vector2(0.05f, 0f);
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {			
			shader.bind();
			shader.setUniform("uValue", 0.9f);
			shader.setUniform("uMVP", camera.getOrthographic());
			
			camera.translate(movement);
			
			GL30.glBindVertexArray(this.vao);
			
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
			
			GL30.glBindVertexArray(0);
			
			Shader.unbind();
		}

		@Override
		public void destroy() {
			this.shader.destroy();
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
