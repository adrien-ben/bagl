package com.adrien.games.bagl.rendering;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public final class IndexBuffer
{
	private int handle;
	private int size;
	
	public IndexBuffer(int size)
	{
		this.handle = GL15.glGenBuffers();
		this.size = size;
	}
	
	public IndexBuffer(int[] data)
	{	
		this.handle = GL15.glGenBuffers();
		this.size = data.length;
		setData(data);
	}
	
	public void setData(int[] indices)
	{
		if(indices.length > size)
			throw new IllegalArgumentException("Too much indices");
		
		IntBuffer buffer = BufferUtils.createIntBuffer(size);
		buffer.put(indices);
		buffer.flip();
		
		bind();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		unbind();
	}
	
	public void bind()
	{
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, handle);
	}
	
	public static void unbind()
	{
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void destroy()
	{
		GL15.glDeleteBuffers(handle);
	}
	
	public int getSize()
	{
		return size;
	}
	
	
}
