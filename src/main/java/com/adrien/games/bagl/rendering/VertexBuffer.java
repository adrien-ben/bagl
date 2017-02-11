package com.adrien.games.bagl.rendering;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class VertexBuffer {
	private VertexDescription description;
	private int vertexCount;
	private int vaoHandle;
	private int vboHandle;
	
	public VertexBuffer(VertexDescription description, int size) {
		this.description = description;
		this.vertexCount = size;
		this.vaoHandle = GL30.glGenVertexArrays();
		this.vboHandle = GL15.glGenBuffers();
	}
	
	public VertexBuffer(VertexDescription description, Vertex[] vertices) {
		this.description = description;
		this.vertexCount = vertices.length;
		this.vaoHandle = GL30.glGenVertexArrays();
		this.vboHandle = GL15.glGenBuffers();
		
		setData(vertices);
	}
	
	public void setData(Vertex[] vertices, int limit) {
		if(limit > vertexCount) {
			throw new IllegalArgumentException("Too much vertices.");
		}
		
		int stride = description.getStride();
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(limit*stride);
		for(int i = 0; i < limit; i++) {
			buffer.put(vertices[i].getData());
		}
		buffer.flip();
		
		GL30.glBindVertexArray(vaoHandle);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboHandle);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		
		for(VertexElement element : description.getVertexElements()) {
			int location = element.getLocation();
			int byteStride = stride*Float.SIZE/8;
			int byteOffset = element.getOffset()*Float.SIZE/8;
			
			GL20.glEnableVertexAttribArray(location);
			GL20.glVertexAttribPointer(location, element.getSize(), GL11.GL_FLOAT, false, byteStride, byteOffset);
		}
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	
	public void setData(Vertex[] vertices) {
		this.setData(vertices, vertices.length);
	}
	
	public void bind() {
		GL30.glBindVertexArray(vaoHandle);
	}
	
	public static void unbind() {
		GL30.glBindVertexArray(0);
	}
	
	public void destroy() {
		GL15.glDeleteBuffers(vboHandle);
		GL30.glDeleteVertexArrays(vaoHandle);
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
}