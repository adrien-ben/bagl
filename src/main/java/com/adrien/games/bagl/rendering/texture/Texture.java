package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

public final class Texture {
	
	private int handle;
	private int width;
	private int height;
	
	public Texture(int width, int height) {
		this(width, height, GL_RGBA8, GL_RGBA);
	}
	
	public Texture(int width, int height, int internalFormat, int format) {
		this.width = width;
		this.height = height;
		this.handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, this.handle);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, this.width, this.height, 0, format, 
				GL_UNSIGNED_BYTE, new float[width*height]);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public Texture(String file) {
		handle = loadTexture(file);
	}
	
	private int loadTexture(String path) {		
		IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
		STBImage.stbi_set_flip_vertically_on_load(0);
		ByteBuffer image = STBImage.stbi_load(path, width, height, comp, 0);
		if(image == null) {
			throw new RuntimeException("Failed to load a face from the cubemap : '" + path + "'.");
		}
		
		this.width = width.get();
		this.height = height.get();
		int channelCount = comp.get();
		int internalFormat = (channelCount == 4) ? GL_RGBA8 : GL_RGB8;
		int format = (channelCount == 4) ? GL_RGBA : GL_RGB;
		
		int handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, handle);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, this.width, this.height, 0, format, GL_UNSIGNED_BYTE, image);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		return handle;
	}
		
	public void destroy() {
		glDeleteTextures(handle);
	}
	
	public void bind() {
		this.bind(0);
	}
	
	public void bind(int unit) {
		glActiveTexture(GL_TEXTURE0 + unit);
		glBindTexture(GL_TEXTURE_2D, handle);
	}
	
	public static void unbind() {
		Texture.unbind(0);
	}
	
	public static void unbind(int unit) {
		glActiveTexture(GL_TEXTURE0 + unit);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	
	public int getHandle() {
		return this.handle;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}

}
