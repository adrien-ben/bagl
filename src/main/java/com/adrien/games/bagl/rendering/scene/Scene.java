package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.Model;
import com.adrien.games.bagl.rendering.Skybox;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.Light;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D scene.
 *
 */
public class Scene {

    private final SceneNode<Model> root;

    private Skybox skybox;

    private Light ambient;
    private final List<DirectionalLight> directionals;
    private final List<PointLight> points;
    private final List<SpotLight> spots;

    public Scene() {
        this.root = new SceneNode<>();
        this.skybox = null;
        this.ambient = new Light(1f, Color.WHITE);
        this.directionals = new ArrayList<>();
        this.points = new ArrayList<>();
        this.spots = new ArrayList<>();
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public void setSkybox(Skybox skybox) {
        this.skybox = skybox;
    }

    public Light getAmbient() {
        return ambient;
    }

    public void setAmbient(Light ambient) {
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
