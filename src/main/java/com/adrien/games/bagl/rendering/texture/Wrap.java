package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL44.*;

public enum Wrap {

	CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
	CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER),
	MIRRORED_REPEAT(GL_MIRRORED_REPEAT),
	REPEAT(GL_REPEAT),
	MIRROR_CLAMP_TO_EDGE(GL_MIRROR_CLAMP_TO_EDGE);
	
	private int glWrap;
	
	private Wrap(int glWrap) {
		this.glWrap = glWrap;
	}
	
	int getGlWrap() {
		return this.glWrap;
	}
	
}
