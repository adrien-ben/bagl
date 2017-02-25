package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;

public enum Filter {
	
	NEAREST(GL_NEAREST),
	LINEAR(GL_LINEAR),
	MIPMAP_LINEAR_LINEAR(GL_LINEAR_MIPMAP_LINEAR),
	MIPMAP_LINEAR_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
	MIPMAP_NEAREST_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
	MIPMAP_NEAREST_NEAREST(GL_NEAREST_MIPMAP_NEAREST);
	
	private final int glFilter;
	
	private Filter(int glFilter) {
		this.glFilter = glFilter; 
	}
	
	public int getGlFilter() {
		return this.glFilter;
	}
	
}
