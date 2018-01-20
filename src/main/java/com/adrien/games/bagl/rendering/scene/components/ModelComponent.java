package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.rendering.scene.Component;
import com.adrien.games.bagl.rendering.scene.ComponentVisitor;

/**
 * Scene component containing a model
 *
 * @author adrien
 */
public class ModelComponent extends Component {

    private final Model model;

    /**
     * Construct a direction light component
     *
     * @param model The model to link to this component
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public ModelComponent(final Model model, final String id, final String... tags) {
        super(id, tags);
        this.model = model;
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

    public Model getModel() {
        return this.model;
    }
}
