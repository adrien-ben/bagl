package com.adrien.games.bagl.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;

/**
 * @author Adrien
 * Checks that the engine runs properly
 */
public final class TestEngine {
	
	private final static class TestGame implements Game {
		
		private static final Logger log = LogManager.getLogger(TestGame.class);

		public final static String TITLE = "Engine";
		public final static int WIDTH = 1024;
		public final static int HEIGHT = WIDTH * 9 / 16;
		
		@Override
		public void init() {
			log.info("Game initialized");	
		}

		@Override
		public void update(Time time) {
			log.info("Game updated");			
		}

		@Override
		public void render() {
			log.info("Game renderered");
		}

		@Override
		public void destroy() {
			log.info("Game destroyed");	
		}

	}
	
	public static void main(String [] args) {
		new Engine(new TestGame(), TestGame.TITLE, TestGame.WIDTH, TestGame.HEIGHT).start();
	}
}
