package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Vector2;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public final class Input {

    private static final int KEY_COUNT = 350;
    private static final boolean[] KEY_STATES = new boolean[KEY_COUNT];
    private static final Vector2 MOUSE_PREVIOUS_POSITION = new Vector2();
    private static final Vector2 MOUSE_POSITION = new Vector2();
    private static final Vector2 MOUSE_DELTA = new Vector2();

    static {
        for(int i = 0; i < KEY_COUNT; i++) {
            KEY_STATES[i] = false;
        }
    }

    private Input() {
    }

    static void update() {
        MOUSE_PREVIOUS_POSITION.set(MOUSE_POSITION);
        Vector2.sub(MOUSE_POSITION, MOUSE_PREVIOUS_POSITION, MOUSE_DELTA);
    }

    static void handleInput(long window, int key, int scancode, int action, int mods) {
        if(action == GLFW_PRESS) {
            KEY_STATES[key] = true;
        } else if(action == GLFW_RELEASE) {
            KEY_STATES[key] = false;
        }
    }

    static void handleMouse(long window, double x, double y) {
        MOUSE_PREVIOUS_POSITION.set(MOUSE_POSITION);
        MOUSE_POSITION.setXY((float)x, (float)y);
        Vector2.sub(MOUSE_POSITION, MOUSE_PREVIOUS_POSITION, MOUSE_DELTA);
    }

    public static boolean isKeyPressed(int key) {
        return KEY_STATES[key];
    }

    public static Vector2 getMousePosition() {
        return MOUSE_POSITION;
    }

    public static Vector2 getMousePreviousPosition() {
        return MOUSE_PREVIOUS_POSITION;
    }

    public static Vector2 getMouseDelta() {
        return MOUSE_DELTA;
    }

}
