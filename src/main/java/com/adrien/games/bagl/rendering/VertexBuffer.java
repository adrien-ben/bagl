package com.adrien.games.bagl.rendering;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.adrien.games.bagl.rendering.vertex.Vertex;
import com.adrien.games.bagl.rendering.vertex.VertexDescription;
import com.adrien.games.bagl.rendering.vertex.VertexElement;

/**
 * OpengGL vertex buffer.
 *
 */
public final class VertexBuffer {
		
	private final VertexDescription description;
	private final BufferUsage usage;
	private final int vertexCount;
	private final int vaoHandle;
	private final int vboHandle;
	
	public VertexBuffer(VertexDescription description, BufferUsage usage, int size) {
		this.description = description;
		this.usage = usage;
		this.vertexCount = size;
		this.vaoHandle = GL30.glGenVertexArrays();
		this.vboHandle = GL15.glGenBuffers();
	}
	
	public VertexBuffer(VertexDescription description, BufferUsage usage, Vertex[] vertices) {
		this(description, usage, vertices.length);
		this.setData(vertices);
	}
	
	/**
	 * Puts vertex data in the buffer.
	 * @param vertices The vertices to put.
	 * @param limit The number of vertices to put.
	 */
	public void setData(Vertex[] vertices, int limit) {
		if(limit > this.vertexCount) {
			throw new IllegalArgumentException("Too much vertices.");
		}
		
		final int stride = this.description.getStride();
		
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(limit*stride);
		for(int i = 0; i < limit; i++) {
			buffer.put(vertices[i].getData());
		}
		buffer.flip();
		
		GL30.glBindVertexArray(this.vaoHandle);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vboHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, this.usage.getGlCode());
		
		for(VertexElement element : this.description.getVertexElements()) {
			final int location = element.getLocation();
			final int byteStride = stride*Float.SIZE/8;
			final int byteOffset = element.getOffset()*Float.SIZE/8;
			
			GL20.glEnableVertexAttribArray(location);
			GL20.glVertexAttribPointer(location, element.getSize(), GL11.GL_FLOAT, false, byteStride, byteOffset);
		}
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	
	/**
	 * Puts vertex data in the buffer.
	 * @param vertices The vertices to put.
	 */
	public void setData(Vertex[] vertices) {
		this.setData(vertices, vertices.length);
	}
	
	/**
	 * Binds the vertex buffer.
	 */
	public void bind() {
		GL30.glBindVertexArray(this.vaoHandle);
	}
	
	/**
	 * Unbinds the currently bound vertex buffer.
	 */
	public static void unbind() {
		GL30.glBindVertexArray(0);
	}
	
	/**
	 * Release OpenGL resources.
	 */
	public void destroy() {
		GL15.glDeleteBuffers(this.vboHandle);
		GL30.glDeleteVertexArrays(this.vaoHandle);
	}
	
	/**
	 * Returns the size of the buffer.
	 * @return
	 */
	public int getVertexCount() {
		return vertexCount;
	}
	
}