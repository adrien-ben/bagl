package com.adrien.games.bagl.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public final class Engine {
	
	private static final Logger log = LogManager.getLogger(Engine.class);
	
	private Game game;
	private Window window;
	private Time time;
	private boolean isRunning;

	public Engine(Game game, String title, int width, int height) {
		log.info("Initializing engine");
		if (game == null) {
			throw new IllegalArgumentException("The argument game cannot be null.");
		}
		this.game = game;
		this.window = new Window(title, width, height);
		this.time = new Time();
		this.isRunning = false;		
		this.game.init();
	}

	public void start() {
		log.info("Starting engine");
		this.isRunning = true;
		while (this.isRunning) {
			if (this.window.isCloseRequested()) {
				stop();
			}
			this.update();
			this.render();
			this.window.update();
		}
		this.destroy();
	}

	private void update() {
		this.time.update();
		this.game.update(this.time);
	}

	private void render() {
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		this.game.render();
	}

	public void stop() {
		log.info("Stopping engine");
		this.isRunning = false;
	}

	private void destroy() {
		log.info("Destroying engine");
		game.destroy();
		window.destroy();
	}

}
