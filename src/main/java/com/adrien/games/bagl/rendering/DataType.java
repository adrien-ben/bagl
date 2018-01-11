package com.adrien.games.bagl.rendering;

import org.lwjgl.opengl.GL11;

/**
 * OpenGl data types
 *
 * @author adrien
 */
public enum DataType {

    DOUBLE(GL11.GL_DOUBLE, Double.SIZE / 8),
    FLOAT(GL11.GL_FLOAT, Float.SIZE / 8),
    INT(GL11.GL_INT, Integer.SIZE / 8),
    SHORT(GL11.GL_SHORT, Short.SIZE / 8),
    BYTE(GL11.GL_BYTE, Byte.SIZE / 8);

    private final int glCode;
    private final int size;

    DataType(final int glCode, final int size) {
        this.glCode = glCode;
        this.size = size;
    }

    public int getGlCode() {
        return this.glCode;
    }

    public int getSize() {
        return this.size;
    }
}
