package com.adrien.games.bagl.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

import com.adrien.games.bagl.core.Color;

/**
 * This class creates an OpenGL {@link FrameBuffer} object. (version >= 3.0).
 * <p>It binds a render buffer to if for the depth component and at least one texture (that you can 
 * retrieve for later use) as the color buffers. 
 * <br>When the frame buffer is bound OpenGL will render into this frame buffer. If no frame buffer
 * is bound OpenGL will then render in the default frame buffer.
 * 
 * @author Adrien
 *
 */
public class FrameBuffer {

	private final int handle;
	private final int depthBufferHandle;
	private final int width;
	private final int height; 
	private final Texture[] colorOutputs;
	
	/**
	 * Creates a new {@link FrameBuffer}.
	 * @param width The width of the frame buffer.
	 * @param height The height of the frame buffer.
	 */
	public FrameBuffer(int width, int height) {
		this(width, height, 1);
	}
	
	/**
	 * Creates a new {@link FrameBuffer}.
	 * @param width The width of the frame buffer.
	 * @param height The height of the frame buffer.
	 * @param colorOutputs The number of color outputs.
	 */
	public FrameBuffer(int width, int height, int colorOutputs) {
		this.width = width;
		this.height = height;
		this.depthBufferHandle = this.createDepthBuffer(this.width, this.height);
		this.colorOutputs = this.createColorOutputs(colorOutputs, this.width, this.height);
		this.handle = this.createBuffer(this.colorOutputs, this.depthBufferHandle);
	}
	
	/**
	 * Creates all textures to use as the different color outputs.
	 * @param colorOutputs The number of texture to generate.
	 * @param width The width of the frame buffer.
	 * @param height The height of the frame buffer.
	 * @return An array of {@link Texture}.
	 */
	private Texture[] createColorOutputs(int colorOutputs, int width, int height) {
		Texture[] textures = new Texture[colorOutputs];
		for(int i = 0; i < colorOutputs; i++) {
			textures[i] = new Texture(width, height);
		}
		return textures;
	}

	/**
	 * Create the actual OpenGL frame buffer object and attaches
	 * the color buffer textures and the depth render buffer.
	 * @param textures The textures to use as color outputs.
	 * @param depthBufferHandle The handle of the OpenGL render buffer.
	 * @return The handle of the OpengGL frame buffer.
	 */
	private int createBuffer(Texture[] textures, int depthBufferHandle) {
		int bufferHandle = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, bufferHandle);
		for(int i = 0; i < textures.length; i++) {			
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, textures[i].getHandle(), 0);
		}
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBufferHandle);
		glDrawBuffers(this.generateBuffersToDraw(textures.length));
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		return bufferHandle;
	}
	
	/**
	 * Generates an array containing the buffers to draw.
	 * @param count The number of buffer to draw.
	 * @return An array of integer.
	 */
	private int[] generateBuffersToDraw(int count) {
		int[] buffers = new int[count];
		for(int i = 0; i < count; i++) {
			buffers[i] = GL_COLOR_ATTACHMENT0 + i;
		}
		return buffers;
	}
	
	/**
	 * Generates an OpenGL render buffer which will be used as
	 * the depth buffer of the frame buffer.
	 * @param width The width of the depth buffer.
	 * @param height The height of the depth buffer.
	 * @return The handle of the OpenGL render buffer.
	 */
	private int createDepthBuffer(int width, int height) {
		int bufferHandle = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, bufferHandle);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, width, height);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		return bufferHandle;
	}
	
	/**
	 * Clears the <b>currently bound</b> frame buffer.
	 */
	public static void clear() {
		FrameBuffer.clear(Color.BLACK);
	}
	
	/**
	 * Clears the <u>currently bound</u> frame buffer.
	 * @param color The background color.
	 */
	public static void clear(Color color) {
		glClearColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	/**
	 * Binds the frame buffer.
	 */
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, this.handle);
	}
	
	/**
	 * Unbinds the currently bound frame buffer.
	 */
	public static void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	/**
	 * Releases resources.
	 */
	public void destroy() {
		for(Texture t : colorOutputs) {
			t.destroy();
		}
		glDeleteRenderbuffers(this.depthBufferHandle);
		glDeleteFramebuffers(this.handle);
	}
	
	public int getHandle() {
		return handle;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Texture getColorTexture() {
		return this.getColorTexture(0);
	}
	
	public Texture getColorTexture(int index) {
		return colorOutputs[index];
	}
	
}
