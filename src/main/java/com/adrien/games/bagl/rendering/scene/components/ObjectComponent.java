package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.scene.Component;
import com.adrien.games.bagl.rendering.scene.ComponentVisitor;

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
     * {@inheritDoc}
     *
     * @see Component#onAccept(ComponentVisitor)
     */
    @Override
    protected void onAccept(final ComponentVisitor visitor) {
        visitor.visit(this);
    }
}
