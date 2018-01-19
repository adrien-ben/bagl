package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.core.math.Quaternions;
import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.scene.Component;

/**
 * Scene component containing a directional light
 *
 * @author adrien
 */
public class DirectionalLightComponent extends Component {

    private final DirectionalLight light;

    /**
     * Construct a directional light component
     *
     * @param light The light to link to this component
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public DirectionalLightComponent(final DirectionalLight light, final String id, final String... tags) {
        super(id, tags);
        this.light = light;
    }

    /**
     * Add the light contained in this component to the renderer
     *
     * @param renderer The visiting renderer
     */
    @Override
    public void visit(final Renderer renderer) {
        this.light.setDirection(Quaternions.getForwardVector(super.transform.getRotation()));
        renderer.addDirectionalLight(this.light);
    }

    public DirectionalLight getLight() {
        return this.light;
    }
}
