package com.adrienben.games.bagl.opengl.texture;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;

public enum Type {

    TEXTURE_2D(GL_TEXTURE_2D),
    CUBE_MAP(GL_TEXTURE_CUBE_MAP);

    private final int glCode;

    Type(final int glCode) {
        this.glCode = glCode;
    }

    public int getGlCode() {
        return glCode;
    }
}
