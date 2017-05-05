package com.adrien.games.bagl.rendering;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

/**
 * OpengGL index buffer.
 *
 */
public final class IndexBuffer {
	
	private final BufferUsage usage;
	private final int handle;
	private final int size;
	
	/**
	 * Creates an index buffer. The memory is allocated but not filled.
	 * @param usage The buffer usage.
	 * @param size The size of the buffer.
	 */
	public IndexBuffer(BufferUsage usage, int size) {
		this.usage = usage;
		this.handle = GL15.glGenBuffers();
		this.size = size;
	}
	
	/**
	 * Creates an index buffer and sets the data.
	 * @param usage The buffer usage.
	 * @param size The size of the buffer.
	 */
	public IndexBuffer(BufferUsage usage, int[] data) {
		this(usage, data.length);
		this.setData(data);
	}
	
	/**
	 * Sets the buffer data.
	 * @param indices The indices to set.
	 */
	public void setData(int[] indices) {
		if(indices.length > this.size) {			
			throw new IllegalArgumentException("Too much indices");
		}
		
		final IntBuffer buffer = BufferUtils.createIntBuffer(this.size);
		buffer.put(indices);
		buffer.flip();
		
		this.bind();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, this.usage.getGlCode());
		unbind();
	}
	
	public void bind() {
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.handle);
	}
	
	public static void unbind() {
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void destroy() {
		GL15.glDeleteBuffers(this.handle);
	}
	
	public int getSize() {
		return size;
	}
	
}
