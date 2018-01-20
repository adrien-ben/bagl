package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.scene.Component;

/**
 * Object component
 * <p>
 * This component contains no specific data, it is
 * used as a mean to group components together
 *
 * @author adrien
 */
public class ObjectComponent extends Component {

    /**
     * Construct an object component
     *
     * @param id   The id of the component
     * @param tags The tags of the component
     */
    public ObjectComponent(final String id, final String... tags) {
        super(id, tags);
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#onUpdate(Time)
     */
    @Override
    protected void onUpdate(final Time time) {
        // does nothing
    }

    /**
     * Does nothing
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        // does nothing
    }
}
