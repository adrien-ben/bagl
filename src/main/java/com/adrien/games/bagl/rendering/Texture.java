package com.adrien.games.bagl.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public final class Texture {
	
	private int handle;
	private int width;
	private int height;
	
	public Texture(int width, int height) {
		this(width, height, GL11.GL_RGBA8, GL11.GL_RGBA);
	}
	
	public Texture(int width, int height, int internalFormat, int format) {
		this.width = width;
		this.height = height;
		this.handle = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.handle);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, this.width, this.height, 0, format, 
				GL11.GL_UNSIGNED_BYTE, new float[width*height]);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public Texture(String file) {
		handle = loadTexture(file);
	}
	
	private int loadTexture(String path) {
		int handle = 0;
		int channelCount = 0;
		ByteBuffer buffer = null;
		int internalFormat = 0;
		int format;
		
		try (InputStream in = Files.newInputStream(Paths.get(path))) {
			PNGDecoder decoder = new PNGDecoder(in);
			this.width = decoder.getWidth();
			this.height = decoder.getHeight();
			channelCount = decoder.hasAlpha() ? 4 : 3;
			Format imgformat = (channelCount == 4) ? Format.RGBA : Format.RGB;
			buffer = BufferUtils.createByteBuffer(width*height*channelCount);
			decoder.decodeFlipped(buffer, width*channelCount, imgformat);
			buffer.flip();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load texture '" + path + "'.", e);
		}	
		
		internalFormat = (channelCount == 4) ? GL11.GL_RGBA8 : GL11.GL_RGB8;
		format = (channelCount == 4) ? GL11.GL_RGBA : GL11.GL_RGB;
		handle = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL11.GL_UNSIGNED_BYTE, buffer);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		return handle;
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
	
	public void destroy() {
		GL11.glDeleteTextures(handle);
	}
	
	public void bind() {
		this.bind(0);
	}
	
	public void bind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
	}
	
	public static void unbind() {
		Texture.unbind(0);
	}
	
	public static void unbind(int unit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

}
