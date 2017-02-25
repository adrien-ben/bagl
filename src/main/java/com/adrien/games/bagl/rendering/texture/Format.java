package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public enum Format {

	RGBA8(GL_RGBA8, GL_RGBA),
	RGB8(GL_RGB8, GL_RGB),
	RED8(GL_R8, GL_RED),
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
