package com.adrien.games.bagl.core;

import static org.lwjgl.glfw.GLFW.*;

public final class Input {
	
	private static final int KEY_COUNT = 350;
	private static final boolean[] KEY_STATES = new boolean[KEY_COUNT];
	
	static {
		for(int i = 0; i < KEY_COUNT; i++) {
			KEY_STATES[i] = false;
		}
	}
	
	private Input() {
	}
	
	public static void handleInput(long window, int key, int scancode, int action, int mods) {
		if(action == GLFW_PRESS) {
			KEY_STATES[key] = true;
		} else if(action == GLFW_RELEASE) {
			KEY_STATES[key] = false;
		}
	}
	
	public static boolean isKeyPressed(int key) {
		return KEY_STATES[key];
	}
	
}
