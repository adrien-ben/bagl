package com.adrien.games.bagl.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public final class Window {

	private final String title;
	private final int width;
	private final int height;
	private final long windowHandle;
	
	public Window(String title, int width, int height) {
		this.title = title;
		this.width = width;
		this.height = height;
		
		GLFW.glfwInit();
		GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
		
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		this.windowHandle = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0, 0);
		if(this.windowHandle == 0) {
			throw new RuntimeException("Failed to create window");
		}
		
		GLFW.glfwMakeContextCurrent(this.windowHandle);
		GL.createCapabilities();
		GLFW.glfwShowWindow(this.windowHandle);
	}
	
	public void update() {
		GLFW.glfwPollEvents();
		GLFW.glfwSwapBuffers(windowHandle);
	}

	public boolean isCloseRequested() {
		return GLFW.glfwWindowShouldClose(windowHandle);
	}
	
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
	
	public String getGLVersion() {
		return GL11.glGetString(GL11.GL_VERSION);
	}
	
}
