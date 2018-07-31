package com.adrienben.games.bagl.engine.scene.components;

import com.adrienben.games.bagl.engine.Time;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.scene.Component;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;

/**
 * Scene component containing a point light
 *
 * @author adrien
 */
public class PointLightComponent extends Component {

    private final PointLight light;

    /**
     * Construct a point light component
     *
     * @param light The light to link to this component
     */
    public PointLightComponent(final PointLight light) {
        this.light = light;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#update(Time)
     */
    @Override
    public void update(final Time time) {
        this.light.setPosition(super.parentObject.getTransform().getTranslation());
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

    public PointLight getLight() {
        return this.light;
    }
}
