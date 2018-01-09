package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.Model;
import com.adrien.games.bagl.rendering.environment.EnvironmentMap;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;

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

    private EnvironmentMap environmentMap;
    private EnvironmentMap irradianceMap;

    private Light ambient;
    private final List<DirectionalLight> directionals;
    private final List<PointLight> points;
    private final List<SpotLight> spots;

    public Scene() {
        this.root = new SceneNode<>();
        this.environmentMap = null;
        this.irradianceMap = null;
        this.ambient = new Light(1f, Color.WHITE);
        this.directionals = new ArrayList<>();
        this.points = new ArrayList<>();
        this.spots = new ArrayList<>();
    }

    public Optional<EnvironmentMap> getEnvironmentMap() {
        return Optional.ofNullable(this.environmentMap);
    }

    public void setEnvironmentMap(final EnvironmentMap environmentMap) {
        this.environmentMap = environmentMap;
    }

    public Optional<EnvironmentMap> getIrradianceMap() {
        return Optional.ofNullable(this.irradianceMap);
    }

    public void setIrradianceMap(final EnvironmentMap irradianceMap) {
        this.irradianceMap = irradianceMap;
    }

    public Light getAmbient() {
        return ambient;
    }

    public void setAmbient(final Light ambient) {
        this.ambient = ambient;
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
