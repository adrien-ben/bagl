package com.adrien.games.bagl.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

/**
 * Window class.
 * <p>
 * This class is a wrapper class for GLFW window. It is responsible
 * for creating, updating and destroying the window. When the window
 * is created, the OpenGL context is also created and linked to this
 * window.
 * <p>
 * Input events are forwarded to the {@link Input} class.
 */
public final class Window {

    private final int width;
    private final int height;
    private final long windowHandle;

    /**
     * Creates a window, sets input event callbacks and creates OpenGL window.
     *
     * @param title      Title of the window.
     * @param width      Width of the window.
     * @param height     Height of the window.
     * @param vSync      Enables vertical synchronisation.
     * @param fullScreen Enables full screen mode.
     */
    public Window(String title, int width, int height, boolean vSync, boolean fullScreen) {
        this.width = width;
        this.height = height;

        GLFW.glfwInit();
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        final long primaryMonitor = fullScreen ? GLFW.glfwGetPrimaryMonitor() : 0;
        this.windowHandle = GLFW.glfwCreateWindow(this.width, this.height, title, primaryMonitor, 0);
        if (this.windowHandle == 0) {
            throw new RuntimeException("Failed to create window");
        }

        GLFW.glfwSetKeyCallback(this.windowHandle, Input::handleKeyboard);
        GLFW.glfwSetMouseButtonCallback(this.windowHandle, Input::handleMouseButton);

        Input.setMouseModeUpdateCallback(this::setMouseMode);
        this.setMouseMode(MouseMode.NORMAL);

        GLFW.glfwMakeContextCurrent(this.windowHandle);
        GLFW.glfwSwapInterval(vSync ? 1 : 0);

        GL.createCapabilities();
        GLFW.glfwShowWindow(this.windowHandle);
    }

    /**
     * Updates the window.
     * <p>
     * Poll events and swaps buffers.
     */
    public void update() {
        GLFW.glfwPollEvents();
        this.updateCursorPosition();
        GLFW.glfwSwapBuffers(windowHandle);
    }

    /**
     * Retrieve the current cursor position and call {@link Input#handleMouseMove(long, double, double, boolean)}
     */
    private void updateCursorPosition() {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final DoubleBuffer x = stack.mallocDouble(1);
            final DoubleBuffer y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(this.windowHandle, x, y);
            Input.handleMouseMove(this.windowHandle, x.get(), this.height - y.get(), true);
        }
    }

    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(windowHandle);
    }

    /**
     * Sets the mouse input mode.
     *
     * @param mouseMode The mouse mode to set.
     */
    public void setMouseMode(final MouseMode mouseMode) {
        GLFW.glfwSetInputMode(this.windowHandle, GLFW.GLFW_CURSOR, mouseMode.getGlfwCode());
        if (mouseMode != MouseMode.DISABLED) {
            GLFW.glfwSetCursorPos(this.windowHandle, this.width / 2, this.height / 2);
            Input.handleMouseMove(this.windowHandle, this.width / 2, this.height / 2, false);
        }
    }

    /**
     * Destroys this window.
     */
    public void destroy() {
        GLFW.glfwDestroyWindow(this.windowHandle);
        GLFW.glfwTerminate();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
