package com.adrien.games.bagl.rendering.scene.components;

import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.scene.Component;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
     * @param id    The id of the component
     * @param tags  The tags of the component
     */
    public SpotLightComponent(final SpotLight light, final String id, final String... tags) {
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
        this.light.setPosition(super.transform.getTranslation());
        // TODO: find another way
        final Quaternionf rotation = super.transform.getRotation();
        final Vector3f direction = new Vector3f(2 * (rotation.x() * rotation.z() + rotation.w() * rotation.y()),
                2 * (rotation.y() * rotation.z() - rotation.w() * rotation.x()),
                1 - 2 * (rotation.x() * rotation.x() + rotation.y() * rotation.y()));
        this.light.setDirection(direction);
        renderer.addSpotLight(this.light);
    }

    public SpotLight getLight() {
        return this.light;
    }
}
