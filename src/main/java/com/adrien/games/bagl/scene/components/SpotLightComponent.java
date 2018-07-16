package com.adrien.games.bagl.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Quaternions;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.ComponentVisitor;

/**
 * Scene component containing a spot light
 *
 * @author adrien
 */
public class SpotLightComponent extends Component {

    private final SpotLight light;

    /**
     * Construct a spot light component
     *
     * @param light The light to link to this component
     */
    public SpotLightComponent(final SpotLight light) {
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
        this.light.setDirection(Quaternions.getForwardVector(super.parentObject.getTransform().getRotation()));
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

    public SpotLight getLight() {
        return this.light;
    }
}
