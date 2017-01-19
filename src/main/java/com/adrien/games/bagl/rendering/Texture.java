package com.adrien.games.bagl.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public final class Texture
{	
	private int handle;

	public Texture(String file)
	{
		handle = loadTexture(file);
	}
	
	private static int loadTexture(String name)
	{
		int handle = 0;
		int width = 0;
		int height = 0;
		int channelCount = 0;
		ByteBuffer buffer = null;
		int internalFormat = 0;
		int format;
		
		try (InputStream in = Texture.class.getResourceAsStream(name)) {
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			channelCount = decoder.hasAlpha() ? 4 : 3;
			Format imgformat = (channelCount == 4) ? Format.RGBA : Format.RGB;
			buffer = BufferUtils.createByteBuffer(width*height*channelCount);
			decoder.decodeFlipped(buffer, width*channelCount, imgformat);
			buffer.flip();
		} catch (IOException e) {
			e.printStackTrace();
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
	
	public void destroy()
	{
		GL11.glDeleteTextures(handle);
	}
	
	public void bind()
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
	}
	
	public static void unbind()
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

}
