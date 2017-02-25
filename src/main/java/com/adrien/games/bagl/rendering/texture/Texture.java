package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;

import java.nio.ByteBuffer;

import com.adrien.games.bagl.utils.Image;
import com.adrien.games.bagl.utils.ImageUtils;

public final class Texture {
		
	private int handle;
	private int width;
	private int height;
	
	public Texture(int width, int height, TextureParameters parameters) {
		this.width = width;
		this.height = height;
		this.handle = this.generateGlTexture(this.width, this.height, parameters, (ByteBuffer)null);
	}
	
	public Texture(String file, TextureParameters parameters) {
		Image image = ImageUtils.loadImage(file);
		this.width = image.getWidth();
		this.height = image.getHeight();
		parameters.format(this.getFormat(image.getChannelCount()));
		this.handle = this.generateGlTexture(this.width, this.height, parameters, image.getData());
	}
	
	private int generateGlTexture(int width, int height, TextureParameters parameters, ByteBuffer image) {
		int handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, handle);
		glTexImage2D(GL_TEXTURE_2D, 0, parameters.getFormat().getGlInternalFormat(), this.width, this.height, 0, 
				parameters.getFormat().getGlFormat(), GL_UNSIGNED_BYTE, image);
		if(parameters.getMipmaps()) {			
			glGenerateMipmap(GL_TEXTURE_2D);
		}
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, parameters.getMagFilter().getGlFilter());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, parameters.getMinFilter().getGlFilter());
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, parameters.getAnisotropic());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, parameters.getsWrap().getGlWrap());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, parameters.gettWrap().getGlWrap());
		glBindTexture(GL_TEXTURE_2D, 0);
		
		return handle;
	}
	
	private Format getFormat(int channelCount) {
		if(channelCount == 4) {
			return Format.RGBA8;
		} else if(channelCount == 3) {
			return Format.RGB8;
		} else if(channelCount == 1) {
			return Format.RED8;
		} else {
			throw new RuntimeException("Only textures with 1, 3 or 4 color channel or supported.");
		}
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
