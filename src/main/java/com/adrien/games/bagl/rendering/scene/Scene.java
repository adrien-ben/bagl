package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.rendering.Renderer;
import com.adrien.games.bagl.rendering.texture.Cubemap;

import java.util.Optional;

/**
 * 3D scene
 * <p>
 * Contains a {@link Component}'s graph. You can add components as follows:
 * <pre>
 * final Scene scene = new Scene();
 * scene.getRoot().addChild(...);
 * </pre>
 *
 * @author adrien
 */
public class Scene {

    private final Component root;
    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;

    /**
     * Construct an empty scene with a empty root component
     */
    public Scene() {
        this.root = new Component() {
            @Override
            public void visit(final Renderer renderer) {
            }
        };
        this.environmentMap = null;
        this.irradianceMap = null;
        this.preFilteredMap = null;
    }

    public Optional<Cubemap> getEnvironmentMap() {
        return Optional.ofNullable(this.environmentMap);
    }

    public void setEnvironmentMap(final Cubemap environmentMap) {
        this.environmentMap = environmentMap;
    }

    public Optional<Cubemap> getIrradianceMap() {
        return Optional.ofNullable(this.irradianceMap);
    }

    public void setIrradianceMap(final Cubemap irradianceMap) {
        this.irradianceMap = irradianceMap;
    }

    public Optional<Cubemap> getPreFilteredMap() {
        return Optional.ofNullable(this.preFilteredMap);
    }

    public void setPreFilteredMap(final Cubemap preFilteredMap) {
        this.preFilteredMap = preFilteredMap;
    }

    public Component getRoot() {
        return this.root;
    }
}
