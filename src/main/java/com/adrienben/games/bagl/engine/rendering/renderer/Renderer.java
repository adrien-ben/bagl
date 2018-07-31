package com.adrienben.games.bagl.engine.rendering.renderer;

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
