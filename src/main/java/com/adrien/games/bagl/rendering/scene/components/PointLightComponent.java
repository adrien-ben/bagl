package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.scene.Component;

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
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public PointLightComponent(final PointLight light, final String id, final String... tags) {
        super(id, tags);
        this.light = light;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#onUpdate(Time)
     */
    @Override
    protected void onUpdate(final Time time) {
        this.light.setPosition(super.transform.getTranslation());
    }

    /**
     * Add the light contained in this component to the renderer
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        renderer.addPointLight(this.light);
    }

    public PointLight getLight() {
        return this.light;
    }
}
