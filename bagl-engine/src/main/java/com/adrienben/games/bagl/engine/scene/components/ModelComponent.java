package com.adrienben.games.bagl.engine.scene.components;

import com.adrienben.games.bagl.engine.Time;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.scene.Component;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;

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
        this.model.update(time);
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
