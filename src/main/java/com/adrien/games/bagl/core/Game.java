package com.adrien.games.bagl.core;

public interface Game
{
	public void init();
	public void destroy();
	public void update(Time time);
	public void render();
}
