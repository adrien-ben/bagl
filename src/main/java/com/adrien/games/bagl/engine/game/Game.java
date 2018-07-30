package com.adrien.games.bagl.engine.game;

import com.adrien.games.bagl.engine.Time;

/**
 * Base interface for game application
 * <p>
 * End user must implement this interface
 */
public interface Game {

    /**
     * Game initialization
     * <p>
     * All OpenGL related initialization MUST happen here
     */
    void init();

    /**
     * Destroy allocated resource before stop the app
     */
    void destroy();

    /**
     * Update the logic of the game application
     * <p>
     * This method will be called once every frame
     *
     * @param time The time og the application
     */
    void update(Time time);

    /**
     * Perform all rendering
     */
    void render();
}
