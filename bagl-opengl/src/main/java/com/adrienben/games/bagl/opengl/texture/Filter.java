package com.adrienben.games.bagl.opengl.texture;

import static org.lwjgl.opengl.GL11.*;

public enum Filter {

    NEAREST(GL_NEAREST),
    LINEAR(GL_LINEAR),
    MIPMAP_LINEAR_LINEAR(GL_LINEAR_MIPMAP_LINEAR),
    MIPMAP_LINEAR_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
    MIPMAP_NEAREST_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
    MIPMAP_NEAREST_NEAREST(GL_NEAREST_MIPMAP_NEAREST);

    private final int glFilter;

    Filter(int glFilter) {
        this.glFilter = glFilter;
    }

    /**
     * Check if this filter is a mipmap filter
     *
     * @return true if the filter is a mipmap filter
     */
    public boolean isMipmap() {
        return this == MIPMAP_LINEAR_LINEAR
                || this == MIPMAP_LINEAR_NEAREST
                || this == MIPMAP_NEAREST_LINEAR
                || this == MIPMAP_NEAREST_NEAREST;
    }

    public int getGlFilter() {
        return this.glFilter;
    }

}
