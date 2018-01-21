package com.adrien.games.bagl.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.ComponentVisitor;

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
