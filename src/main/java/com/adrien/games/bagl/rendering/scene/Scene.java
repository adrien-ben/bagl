package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.rendering.Model;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.texture.Cubemap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 3D scene
 *
 * @author adrien
 */
public class Scene {

    private final SceneNode<Model> root;

    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;

    private final List<DirectionalLight> directionals;
    private final List<PointLight> points;
    private final List<SpotLight> spots;

    public Scene() {
        this.root = new SceneNode<>();
        this.environmentMap = null;
        this.irradianceMap = null;
        this.directionals = new ArrayList<>();
        this.points = new ArrayList<>();
        this.spots = new ArrayList<>();
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

    public SceneNode<Model> getRoot() {
        return root;
    }

    public List<DirectionalLight> getDirectionals() {
        return directionals;
    }

    public List<PointLight> getPoints() {
        return points;
    }

    public List<SpotLight> getSpots() {
        return spots;
    }
}
