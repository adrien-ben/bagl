package com.adrien.games.bagl.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * Input class
 * <p>
 * This class is used to manage input of the application. You can retrieve the state of keyboard
 * keys and mouse buttons.A key or button is either pressed or released. You can also retrieve the
 * position of the cursor on screen. The bottom left corner of the screen represents the origin
 * (0, 0)
 * <p>
 * You can change the mouse input mode by using the {@link Input#setMouseMode(MouseMode)}
 * method. Note that if you are using {@link MouseMode#DISABLED} the cursor position is no
 * more related to the origin of the screen. It can be use to represents infinite movement
 *
 * @author adrien
 */
public final class Input {

    private static Logger log = LogManager.getLogger(Input.class);

    private static final int KEY_COUNT = 350;
    private static final int MOUSE_BUTTON_COUNT = 10;
    private static final boolean[] KEY_STATES = new boolean[KEY_COUNT];
    private static final boolean[] KEY_PREVIOUS_STATES = new boolean[KEY_COUNT];
    private static final boolean[] MOUSE_BUTTON_STATES = new boolean[MOUSE_BUTTON_COUNT];
    private static final boolean[] MOUSE_BUTTON_PREVIOUS_STATES = new boolean[MOUSE_BUTTON_COUNT];
    private static final Vector2f MOUSE_PREVIOUS_POSITION = new Vector2f();
    private static final Vector2f MOUSE_POSITION = new Vector2f();
    private static final Vector2f MOUSE_DELTA = new Vector2f();
    private static final Vector2f WHEEL_DELTA = new Vector2f();
    private static Consumer<MouseMode> MOUSE_MODE_UPDATE_CALLBACK = null;

    static {
        Arrays.fill(KEY_STATES, false);
        Arrays.fill(KEY_PREVIOUS_STATES, false);
        Arrays.fill(MOUSE_BUTTON_STATES, false);
        Arrays.fill(MOUSE_BUTTON_PREVIOUS_STATES, false);
    }

    /**
     * Private constructor to prevent instantiation
     */
    private Input() {
    }

    /**
     * Set a callback to call when the mouse mode is updated
     * <p>
     * This must not be called by end user
     *
     * @param callback Callback
     */
    public static void setMouseModeUpdateCallback(final Consumer<MouseMode> callback) {
        MOUSE_MODE_UPDATE_CALLBACK = callback;
    }

    /**
     * This method is used to change mouse input mode
     * <p>
     * It does nothing except forwarding the call to a callback if one is defined
     *
     * @param mouseMode Mouse mode to set
     */
    public static void setMouseMode(final MouseMode mouseMode) {
        if (Objects.nonNull(MOUSE_MODE_UPDATE_CALLBACK)) {
            MOUSE_MODE_UPDATE_CALLBACK.accept(mouseMode);
        }
    }

    /**
     * Copy the current state into the previous state. This method must be called before
     * handle current frame inputs
     * <p>
     * This must not be called by end user
     */
    public static void update() {
        System.arraycopy(KEY_STATES, 0, KEY_PREVIOUS_STATES, 0, KEY_COUNT);
        System.arraycopy(MOUSE_BUTTON_STATES, 0, MOUSE_BUTTON_PREVIOUS_STATES, 0, MOUSE_BUTTON_COUNT);
        MOUSE_PREVIOUS_POSITION.set(MOUSE_POSITION);
        MOUSE_POSITION.sub(MOUSE_PREVIOUS_POSITION, MOUSE_DELTA);
        WHEEL_DELTA.set(0f, 0f);
    }

    /**
     * Keyboard event callback
     * <p>
     * This must not be called by end user
     *
     * @param window   Handle of the source window
     * @param key      Key related to the event
     * @param scanCode Scan code of the key
     * @param action   Action of the event
     * @param mods     Modifiers applied
     */
    public static void handleKeyboard(final long window, final int key, final int scanCode, final int action, final int mods) {
        // TODO : handle modifiers
        if (key >= 0 && key < KEY_COUNT) {
            if (action == GLFW_PRESS) {
                log.debug("Key {}({}) pressed with modifier {}", key, scanCode, mods);
                KEY_STATES[key] = true;
            } else if (action == GLFW_RELEASE) {
                KEY_STATES[key] = false;
            }
        }
    }

    /**
     * Mouse buttons event callback
     * <p>
     * This must not be called by end user
     *
     * @param window Handle of the source window
     * @param button Button related to the event
     * @param action Action of the event
     * @param mods   Modifiers applied
     */
    public static void handleMouseButton(final long window, final int button, final int action, final int mods) {
        if (button >= 0 && button < MOUSE_BUTTON_COUNT) {
            if (action == GLFW_PRESS) {
                MOUSE_BUTTON_STATES[button] = true;
            } else if (action == GLFW_RELEASE) {
                MOUSE_BUTTON_STATES[button] = false;
            }
        }
    }

    /**
     * Mouse movement callback
     * <p>
     * This must not be called by end user
     *
     * @param window      The handle of the source window
     * @param x           Position of the cursor on x axis
     * @param y           Position of the cursor on y axis
     * @param updateDelta Should update the mouse position delta ?
     */
    public static void handleMouseMove(final long window, final double x, final double y, final boolean updateDelta) {
        MOUSE_PREVIOUS_POSITION.set(MOUSE_POSITION);
        MOUSE_POSITION.set((float) x, (float) y);
        if (updateDelta) {
            MOUSE_POSITION.sub(MOUSE_PREVIOUS_POSITION, MOUSE_DELTA);
        }
    }

    /**
     * Mouse wheel callback
     * <p>
     * This must not be called by end user
     *
     * @param window  The handle of the source window
     * @param xOffset The horizontal offset of the wheel
     * @param yOffset The vertical offset of the wheel
     */
    public static void handleScroll(final long window, final double xOffset, final double yOffset) {
        WHEEL_DELTA.set((float) xOffset, (float) yOffset);
    }

    /**
     * Check if a key is pressed
     *
     * @param key Key to check
     * @return true if pressed
     */
    public static boolean isKeyPressed(final int key) {
        return KEY_STATES[key];
    }

    /**
     * Check if a key was just pressed this frame
     *
     * @param key Key to check
     * @return true if it was just pressed
     */
    public static boolean wasKeyPressed(final int key) {
        return KEY_STATES[key] && !KEY_PREVIOUS_STATES[key];
    }

    /**
     * Check if a key was just released this frame
     *
     * @param key Key to check
     * @return true if it was just released
     */
    public static boolean wasKeyReleased(final int key) {
        return !KEY_STATES[key] && KEY_PREVIOUS_STATES[key];
    }

    /**
     * Check if a mouse button is pressed
     *
     * @param button Button to check
     * @return true if pressed
     */
    public static boolean isMouseButtonPressed(final int button) {
        return MOUSE_BUTTON_STATES[button];
    }

    /**
     * Check if a mouse button was just pressed this frame
     *
     * @param button Button to check
     * @return true if it was just pressed
     */
    public static boolean wasMouseButtonPressed(final int button) {
        return MOUSE_BUTTON_STATES[button] && !MOUSE_BUTTON_PREVIOUS_STATES[button];
    }

    /**
     * Check if a mouse button was just released this frame
     *
     * @param button Button to check
     * @return true if it was just released
     */
    public static boolean wasMouseButtonReleased(final int button) {
        return !MOUSE_BUTTON_STATES[button] && MOUSE_BUTTON_PREVIOUS_STATES[button];
    }

    /**
     * Get the current cursor position on screen. (0, 0) is the bottom left corner of the screen
     *
     * @return The cursor position
     */
    public static Vector2f getMousePosition() {
        return MOUSE_POSITION;
    }

    /**
     * Get the previous cursor position. (0, 0) is the bottom left corner of the screen
     *
     * @return The previous cursor position
     */
    public static Vector2f getMousePreviousPosition() {
        return MOUSE_PREVIOUS_POSITION;
    }

    /**
     * Get the displacement of the mouse from the previous frame to the current one
     *
     * @return The displacement of the mouse
     */
    public static Vector2f getMouseDelta() {
        return MOUSE_DELTA;
    }

    public static Vector2f getWheelDelta() {
        return WHEEL_DELTA;
    }

}
