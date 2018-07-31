package com.adrienben.games.bagl.extensions;

import com.adrienben.games.bagl.core.exception.EngineException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

/**
 * JUnit extension that manage an OpenGL context which is required to unit tests OpenGL dependent classes.
 *
 * @author adrien
 */
public class OGLExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger LOG = LogManager.getLogger(OGLExtension.class);

    private static final int MIN_WINDOWS_SIZE = 1;

    private long windowHandle;

    @Override
    public void beforeAll(final ExtensionContext extensionContext) {
        LOG.info("Loading OpenGL context");
        GLFW.glfwInit();
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        windowHandle = GLFW.glfwCreateWindow(MIN_WINDOWS_SIZE, MIN_WINDOWS_SIZE, "junit_ogl_extension", 0, 0);
        if (windowHandle == 0) {
            throw new EngineException("Failed to create OpenGL context");
        }

        GLFW.glfwMakeContextCurrent(this.windowHandle);
        GL.createCapabilities();
        LOG.info("OpenGL context loaded");
    }

    @Override
    public void afterAll(final ExtensionContext extensionContext) {
        LOG.info("Killing OpenGL context");
        GLFW.glfwDestroyWindow(windowHandle);
        GLFW.glfwTerminate();
        LOG.info("OpenGL context killed");
    }
}
