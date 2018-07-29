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
    private final boolean selfDestruction;

    /**
     * Construct a direction light component
     *
     * @param model The model to link to this component
     * @param selfDestruction Indicate whether the component is responsible for destroying its model
     */
    public ModelComponent(final Model model, final boolean selfDestruction) {
        this.model = model;
        this.selfDestruction = selfDestruction;
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
     * <p>
     * Will destroy its model if its {@code selfDestruction} flag is set to true
     *
     * @see Component#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        if (selfDestruction) {
            model.destroy();
        }
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
