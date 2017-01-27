package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.rendering.Spritebatch;
import com.adrien.games.bagl.rendering.Texture;

public class SpritebatchSample {

private final static class TestGame implements Game {
		
		public final static String TITLE = "Normal buffer";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;

		private Texture texture;
		private Spritebatch spritebatch;
				
		@Override
		public void init() {
			this.spritebatch = new Spritebatch(2, WIDTH, HEIGHT);
			this.texture = new Texture("/default.png");
		}

		@Override
		public void update(Time time) {
		}

		@Override
		public void render() {
			this.spritebatch.start();
			this.spritebatch.draw(this.texture, Vector2.ZERO);
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
