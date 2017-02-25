package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGB8;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT32F;

public enum Format {

	RGBA8(GL_RGBA8, GL_RGBA),
	RGB8(GL_RGB8, GL_RGB),
	DEPTH_32F(GL_DEPTH_COMPONENT32F, GL_DEPTH_COMPONENT);
	
	private final int glInternalFormat;
	private final int glFormat;
	
	private Format(int glInternalFormat, int glFormat) {
		this.glInternalFormat = glInternalFormat;
		this.glFormat = glFormat;
	}
	
	public int getGlInternalFormat() {
		return this.glInternalFormat;
	}
	
	public int getGlFormat() {
		return this.glFormat;
	}
	
}
