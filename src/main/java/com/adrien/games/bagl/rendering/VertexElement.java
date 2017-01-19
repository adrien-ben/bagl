package com.adrien.games.bagl.rendering;

public final class VertexElement
{
	private int location;
	private int size;
	private int offset;
	
	public VertexElement(int location, int size, int offset)
	{
		this.location = location;
		this.size = size;
		this.offset = offset;
	}
	
	public int getLocation()
	{
		return location;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public int getOffset()
	{
		return offset;
	}
}