package com.adrien.games.bagl.sample;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.Texture;

public class SpritebatchSample {

private final static class TestGame implements Game {
		
		public final static String TITLE = "Spritebatch";
		public final static int WIDTH = 512;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		public final static int SPRITE_COUNT = 1000;

		private Texture texture;
		private Spritebatch spritebatch;
		
		private Vector2[] positions = new Vector2[SPRITE_COUNT];
		private int[] sizes = new int[SPRITE_COUNT];
		private float[] rotations = new float[SPRITE_COUNT];
				
		@Override
		public void init() {
			this.spritebatch = new Spritebatch(512, WIDTH, HEIGHT);
			this.texture = new Texture("/default.png");
			
			Random r = new Random();
			for(int i = 0; i < SPRITE_COUNT; i++) {
				positions[i] = new Vector2(r.nextFloat() * WIDTH, r.nextFloat() * HEIGHT);
				sizes[i] = r.nextInt(32) + 32;
				rotations[i] = r.nextFloat() * 360;
			}
			
			GL11.glClearColor(100f/255, 149f/255, 237f/255, 1);
		}

		@Override
		public void update(Time time) {
			System.out.println(time.getElapsedTime());
			for(int i = 0; i < SPRITE_COUNT; i++) {
				rotations[i] += time.getElapsedTime()*50;
			}
		}

		@Override
		public void render() {
			this.spritebatch.start();
			for(int i = 0; i < SPRITE_COUNT; i++) {
				this.spritebatch.draw(this.texture, positions[i], sizes[i], sizes[i], rotations[i], Color.GREEN);
			}
			this.spritebatch.end();
		}

		@Override
		public void destroy() {
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
	
}
