package com.adrien.games.bagl.rendering.scene.components;

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
     */
    public PointLightComponent(final PointLight light) {
        this.light = light;
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
}
