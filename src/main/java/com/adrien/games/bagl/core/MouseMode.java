package com.adrien.games.bagl.core;

import org.lwjgl.glfw.GLFW;

public enum MouseMode {

    NORMAL(GLFW.GLFW_CURSOR_NORMAL),
    HIDDEN(GLFW.GLFW_CURSOR_HIDDEN),
    DISABLED(GLFW.GLFW_CURSOR_DISABLED);

    private final int glfwCode;

    MouseMode(final int glfwCode) {
        this.glfwCode = glfwCode;
    }

    public int getGlfwCode() {
        return this.glfwCode;
    }

}
