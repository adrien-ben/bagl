package com.adrienben.games.bagl.opengl;

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
    UNSIGNED_INT(GL11.GL_UNSIGNED_INT, Integer.SIZE / 8),
    SHORT(GL11.GL_SHORT, Short.SIZE / 8),
    UNSIGNED_SHORT(GL11.GL_UNSIGNED_SHORT, Short.SIZE / 8),
    BYTE(GL11.GL_BYTE, Byte.SIZE / 8),
    UNSIGNED_BYTE(GL11.GL_UNSIGNED_BYTE, Byte.SIZE / 8);

    private final int glCode;
    private final int size;

    DataType(final int glCode, final int size) {
        this.glCode = glCode;
        this.size = size;
    }

    /**
     * Is this value a type representing a whole number
     *
     * @return true if one of the whole number data type
     */
    public boolean isWholeType() {
        return this == INT || this == SHORT || this == BYTE;
    }

    public int getGlCode() {
        return this.glCode;
    }

    public int getSize() {
        return this.size;
    }
}
