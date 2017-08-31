package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Vector2;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * Input class.
 * <p>
 * This class is used to manage input of the application. You can retrieve the state of keyboard
 * keys and mouse buttons.A key or button is either pressed or released. You can also retrieve the
 * position of the cursor on screen. The bottom left corner of the screen represents the origin
 * (0, 0)
 * <p>
 * You can change the mouse input mode by using the {@link Input#setMouseMode(MouseMode)}
 * method. Note that if you are using {@link MouseMode#DISABLED} the cursor position is no
 * more related to the origin of the screen. It can be use to represents infinite movement
 */
public final class Input {

    private static final int KEY_COUNT = 350;
    private static final int MOUSE_BUTTON_COUNT = 10;
    private static final boolean[] KEY_STATES = new boolean[KEY_COUNT];
    private static final boolean[] KEY_PREVIOUS_STATES = new boolean[KEY_COUNT];
    private static final boolean[] MOUSE_BUTTON_STATES = new boolean[MOUSE_BUTTON_COUNT];
    private static final boolean[] MOUSE_BUTTON_PREVIOUS_STATES = new boolean[MOUSE_BUTTON_COUNT];
    private static final Vector2 MOUSE_PREVIOUS_POSITION = new Vector2();
    private static final Vector2 MOUSE_POSITION = new Vector2();
    private static final Vector2 MOUSE_DELTA = new Vector2();
    private static Consumer<MouseMode> MOUSE_MODE_UPDATE_CALLBACK = null;

    static {
        Arrays.fill(KEY_STATES, false);
        Arrays.fill(KEY_PREVIOUS_STATES, false);
        Arrays.fill(MOUSE_BUTTON_STATES, false);
        Arrays.fill(MOUSE_BUTTON_PREVIOUS_STATES, false);
    }

    private Input() {
    }

    /**
     * Sets a callback to call when the mouse mode is updated. INTERNAL STUFF.
     *
     * @param callback Callback.
     */
    static void setMouseModeUpdateCallback(final Consumer<MouseMode> callback) {
        MOUSE_MODE_UPDATE_CALLBACK = callback;
    }

    /**
     * This method is used to change mouse input mode.
     * <p>
     * It does nothing except forwarding the call to a callback if one is defined
     *
     * @param mouseMode Mouse mode to set.
     */
    public static void setMouseMode(final MouseMode mouseMode) {
        if (Objects.nonNull(MOUSE_MODE_UPDATE_CALLBACK)) {
            MOUSE_MODE_UPDATE_CALLBACK.accept(mouseMode);
        }
    }

    /**
     * Copies the current state into the previous state. This method must be called before
     * handle current frame inputs. INTERNAL.
     */
    static void update() {
        System.arraycopy(KEY_STATES, 0, KEY_PREVIOUS_STATES, 0, KEY_COUNT);
        System.arraycopy(MOUSE_BUTTON_STATES, 0, MOUSE_BUTTON_PREVIOUS_STATES, 0, MOUSE_BUTTON_COUNT);
        MOUSE_PREVIOUS_POSITION.set(MOUSE_POSITION);
        Vector2.sub(MOUSE_POSITION, MOUSE_PREVIOUS_POSITION, MOUSE_DELTA);
    }

    /**
     * Keyboard event callback. INTERNAL.
     *
     * @param window   Handle of the source window.
     * @param key      Key related to the event.
     * @param scanCode Scan code of the key.
     * @param action   Action of the event.
     * @param mods     Modifiers applied.
     */
    static void handleKeyboard(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW_PRESS) {
            KEY_STATES[key] = true;
        } else if (action == GLFW_RELEASE) {
            KEY_STATES[key] = false;
        }
    }

    /**
     * Mouse buttons event callback. INTERNAL.
     *
     * @param window Handle of the source window.
     * @param button Button related to the event.
     * @param action Action of the event.
     * @param mods   Modifiers applied.
     */
    static void handleMouseButton(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            MOUSE_BUTTON_STATES[button] = true;
        } else if (action == GLFW_RELEASE) {
            MOUSE_BUTTON_STATES[button] = false;
        }
    }

    /**
     * Mouse movement callback. INTERNAL.
     *
     * @param window The handle of the source window.
     * @param x      Position of the cursor on x axis.
     * @param y      Position of the cursor on y axis.
     */
    static void handleMouseMove(long window, double x, double y) {
        MOUSE_PREVIOUS_POSITION.set(MOUSE_POSITION);
        MOUSE_POSITION.setXY((float) x, (float) y);
        Vector2.sub(MOUSE_POSITION, MOUSE_PREVIOUS_POSITION, MOUSE_DELTA);
    }

    /**
     * Checks if a key is pressed.
     *
     * @param key Key to check.
     * @return true if pressed.
     */
    public static boolean isKeyPressed(int key) {
        return KEY_STATES[key];
    }

    /**
     * Checks if a key was just pressed this frame.
     *
     * @param key Key to check.
     * @return true if it was just pressed.
     */
    public static boolean wasKeyPressed(int key) {
        return KEY_STATES[key] && !KEY_PREVIOUS_STATES[key];
    }

    /**
     * Checks if a key was just released this frame.
     *
     * @param key Key to check.
     * @return true if it was just released.
     */
    public static boolean wasKeyReleased(int key) {
        return !KEY_STATES[key] && KEY_PREVIOUS_STATES[key];
    }

    /**
     * Checks if a mouse button is pressed.
     *
     * @param button Button to check.
     * @return true if pressed.
     */
    public static boolean isMouseButtonPressed(int button) {
        return MOUSE_BUTTON_STATES[button];
    }

    /**
     * Checks if a mouse button was just pressed this frame.
     *
     * @param button Button to check.
     * @return true if it was just pressed.
     */
    public static boolean wasMouseButtonPressed(int button) {
        return MOUSE_BUTTON_STATES[button] && !MOUSE_BUTTON_PREVIOUS_STATES[button];
    }

    /**
     * Checks if a mouse button was just released this frame.
     *
     * @param button Button to check.
     * @return true if it was just released.
     */
    public static boolean wasMouseButtonReleased(int button) {
        return !MOUSE_BUTTON_STATES[button] && MOUSE_BUTTON_PREVIOUS_STATES[button];
    }

    /**
     * Gets the current cursor position on screen. (0, 0) is the bottom left corner of the screen.
     *
     * @return The cursor position.
     */
    public static Vector2 getMousePosition() {
        return MOUSE_POSITION;
    }

    /**
     * Gets the previous cursor position. (0, 0) is the bottom left corner of the screen.
     *
     * @return The previous cursor position.
     */
    public static Vector2 getMousePreviousPosition() {
        return MOUSE_PREVIOUS_POSITION;
    }

    /**
     * Gets the displacement of the mouse from the previous frame to the current one.
     *
     * @return The displacement of the mouse.
     */
    public static Vector2 getMouseDelta() {
        return MOUSE_DELTA;
    }

}
