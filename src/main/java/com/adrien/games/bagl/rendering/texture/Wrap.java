package com.adrien.games.bagl.rendering.texture;

import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL44.GL_MIRROR_CLAMP_TO_EDGE;

public enum Wrap {

    CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GL_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GL_MIRRORED_REPEAT),
    REPEAT(GL_REPEAT),
    MIRROR_CLAMP_TO_EDGE(GL_MIRROR_CLAMP_TO_EDGE);

    private final int glWrap;

    Wrap(int glWrap) {
        this.glWrap = glWrap;
    }

    int getGlWrap() {
        return this.glWrap;
    }

}
