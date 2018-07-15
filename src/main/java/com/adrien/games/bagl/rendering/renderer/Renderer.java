package com.adrien.games.bagl.rendering.renderer;

/**
 * Base interface for all renderer.
 * <p>
 * A renderer in responsible for rendering something.
 *
 * @author adrien
 */
public interface Renderer<T> {

    /**
     * Render {@code t}.
     */
    void render(T t);

}
