package com.adrien.games.bagl.core;

import org.lwjgl.opengl.GL11;


public final class Engine
{	
	private Game game;
	private Window window;
	private Time time;
	private boolean isRunning;

	public Engine(Game game, String title, int width, int height)
	{
		if (game == null)
			throw new IllegalArgumentException(
					"The argument game cannot be null.");

		this.game = game;
		this.window = new Window(title, width, height);
		this.time = new Time();
		this.isRunning = false;
		
		System.out.println("Engine.constructor:\n\t-width:"
				+ window.getWidth() + "\n\t-height:"
				+ window.getHeight() + "\n\t-gl:"
				+ window.getGLVersion());
		
		this.game.init();
	}

	public void start()
	{
		isRunning = true;

		while (isRunning)
		{
			if (window.isCloseRequested())
			{
				stop();
			}

			update();
			render();
			window.update();
		}

		destroy();
	}

	private void update()
	{
		time.update();
		game.update(time);
	}

	private void render()
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		game.render();
	}

	public void stop()
	{
		isRunning = false;
	}

	private void destroy()
	{
		game.destroy();
		window.destroy();
	}

}
