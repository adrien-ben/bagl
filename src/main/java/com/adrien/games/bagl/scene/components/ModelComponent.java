package com.adrien.games.bagl.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.model.Model;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.ComponentVisitor;

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
     */
    public ModelComponent(final Model model) {
        this.model = model;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#update(Time)
     */
    @Override
    public void update(final Time time) {
        this.model.transform(super.getParentObject().getTransform());
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        model.destroy();
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#accept(ComponentVisitor)
     */
    @Override
    public void accept(final ComponentVisitor visitor) {
        visitor.visit(this);
    }

    public Model getModel() {
        return this.model;
    }
}
