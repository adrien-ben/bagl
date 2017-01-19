package com.adrien.games.bagl.core;

public final class Time
{
	private static long SECOND = 1000000000L;
	
	private float elapsed;
	private float total;
	private float time;
	
	public Time()
	{
		this.elapsed = 0;
		this.total = 0;
		this.time = time();
	}
	
	public void update()
	{
		float newTime = time();
		elapsed = newTime - time;
		total += elapsed;
		time = newTime;
	}
	
	private float time()
	{
		return (float)System.nanoTime()/SECOND;
	}
	
	public float getElapsedTime()
	{
		return elapsed;
	}
	
	public float getTotalTime()
	{
		return total;
	}
	
}
