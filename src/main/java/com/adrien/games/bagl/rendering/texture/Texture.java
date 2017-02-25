package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

public final class Texture {
	
	private int handle;
	private int width;
	private int height;
	
	public Texture(int width, int height) {
		this(width, height, Format.RGBA8);
	}
	
	public Texture(int width, int height, Format format) {
		this.width = width;
		this.height = height;
		this.handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, this.handle);
		glTexImage2D(GL_TEXTURE_2D, 0, format.getGlInternalFormat(), this.width, this.height, 0, 
				format.getGlFormat(), GL_UNSIGNED_BYTE, new float[width*height]);
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
			throw new RuntimeException("Failed to load texture : '" + path + "'.");
		}
		
		this.width = width.get();
		this.height = height.get();
		int channelCount = comp.get();
		Format format = (channelCount == 4) ? Format.RGBA8 : Format.RGB8; 
		
		int handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, handle);
		glTexImage2D(GL_TEXTURE_2D, 0, format.getGlInternalFormat(), this.width, this.height, 0, 
				format.getGlFormat(), GL_UNSIGNED_BYTE, image);
		glGenerateMipmap(GL_TEXTURE_2D);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
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
