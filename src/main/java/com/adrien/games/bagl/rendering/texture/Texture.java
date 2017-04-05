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

	/**
	 * Creates a new texture.
	 * @param width The width of the texture.
	 * @param height The height of the texture.
	 * @param parameters The parameters of the texture.
	 */
	public Texture(int width, int height, TextureParameters parameters) {
		this.width = width;
		this.height = height;
		this.handle = this.generateGlTexture(this.width, this.height, parameters, ByteBuffer.class.cast(null));
	}
	
	/**
	 * Creates a texture from an image file.
	 * @param file The path to the image file to load.
	 * @param parameters The parameters of the texture. (!format will be overridden by the image format). 
	 */
	public Texture(String file, TextureParameters parameters) {
		Image image = ImageUtils.loadImage(file);
		this.width = image.getWidth();
		this.height = image.getHeight();
		parameters.format(this.getFormat(image.getChannelCount()));
		this.handle = this.generateGlTexture(this.width, this.height, parameters, image.getData());
	}
	
	/**
	 * Creates a texture from a pixel buffer. You have to take extra care of
	 * parameters consistency. If texture format is not consistent with the
	 * content of pixel buffer and dimensions of the texture, errors will arise.
	 * @param width The width of the texture.
	 * @param height The height of the texture.
	 * @param pixels The pixel data.
	 * @param parameters The texture parameter.
	 */
	public Texture(int width, int height, ByteBuffer pixels, TextureParameters parameters) {
		this.width = width;
		this.height = height;
		this.handle = this.generateGlTexture(this.width, this.height, parameters, pixels);
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
	
	/**
	 * Release resources.
	 */
	public void destroy() {
		glDeleteTextures(handle);
	}
	
	/**
	 * Bind the textures to the default texture unit (0).
	 */
	public void bind() {
		this.bind(0);
	}
	
	/**
	 * Binds the texture to the given texture unit.
	 * @param unit The texture unit to bind the texture to.
	 */
	public void bind(int unit) {
		glActiveTexture(GL_TEXTURE0 + unit);
		glBindTexture(GL_TEXTURE_2D, handle);
	}
	
	/**
	 * Unbinds the texture bound to the default texture unit (0).
	 */
	public static void unbind() {
		Texture.unbind(0);
	}
	
	/**
	 * Unbinds the texture bound to the given texture unit.
	 * @param unit The texture unit to unbind the texture from.
	 */
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
