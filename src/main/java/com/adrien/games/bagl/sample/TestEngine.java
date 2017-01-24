package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;

/**
 * @author Adrien
 * Checks that the engine runs properly
 */
public final class TestEngine {
	
	private final static class TestGame implements Game {

		public final static String TITLE = "Engine";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		@Override
		public void init() {
			System.out.println("Game initialized");	
		}

		@Override
		public void update(Time time) {
			System.out.println("Game updated");			
		}

		@Override
		public void render() {
			System.out.println("Game renderered");
		}

		@Override
		public void destroy() {
			System.out.println("Game destroyed");	
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
