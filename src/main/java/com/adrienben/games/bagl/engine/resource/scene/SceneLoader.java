package com.adrienben.games.bagl.engine.resource.scene;

import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.math.MathUtils;
import com.adrienben.games.bagl.core.utils.CollectionUtils;
import com.adrienben.games.bagl.engine.Transform;
import com.adrienben.games.bagl.engine.resource.scene.json.GameObjectJson;
import com.adrienben.games.bagl.engine.resource.scene.json.SceneJson;
import com.adrienben.games.bagl.engine.resource.scene.json.TransformJson;
import com.adrienben.games.bagl.engine.scene.GameObject;
import com.adrienben.games.bagl.engine.scene.Scene;
import com.google.gson.Gson;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The scene loader is responsible for loading scenes from files.
 *
 * @author adrien
 */
public class SceneLoader {

    private Gson gson;
    private ComponentFactory componentFactory;

    public SceneLoader(final ComponentFactory componentFactory) {
        this.gson = new Gson();
        this.componentFactory = componentFactory;
    }

    public Scene load(final ResourcePath path) {
        final var sceneDescriptor = loadFile(path);
        final var scene = new Scene();
        sceneDescriptor.getChildren().forEach(child -> mapGameObject(scene.getRoot(), child));
        return scene;
    }

    private SceneJson loadFile(final ResourcePath path) {
        return gson.fromJson(new InputStreamReader(path.openInputStream()), SceneJson.class);
    }

    private void mapGameObject(final GameObject parent, final GameObjectJson gameObjectJson) {
        final GameObject gameObject = createGameObject(parent, gameObjectJson);
        gameObject.setEnabled(gameObjectJson.isEnabled());
        if (Objects.nonNull(gameObjectJson.getTransform())) {
            setTransform(gameObject.getLocalTransform(), gameObjectJson.getTransform());
        }
        if (CollectionUtils.isNotEmpty(gameObjectJson.getComponents())) {
            gameObjectJson.getComponents().forEach(component -> mapComponent(gameObject, component));
        }
        if (CollectionUtils.isNotEmpty(gameObjectJson.getChildren())) {
            gameObjectJson.getChildren().forEach(child -> mapGameObject(gameObject, child));
        }
    }

    private GameObject createGameObject(GameObject parent, GameObjectJson gameObjectJson) {
        if (Objects.nonNull(gameObjectJson.getTags())) {
            final var tagArray = gameObjectJson.getTags().toArray(new String[0]);
            return parent.createChild(gameObjectJson.getId(), tagArray);
        }
        return parent.createChild(gameObjectJson.getId());
    }

    private void setTransform(final Transform transform, final TransformJson transformJson) {
        transform
                .setTranslation(mapTranslation(transformJson.getTranslation()))
                .setRotation(mapRotations(transformJson.getRotations()))
                .setScale(mapScale(transformJson.getScale()));
    }

    private Quaternionf mapRotations(final List<Vector3f> rotations) {
        final var result = new Quaternionf();
        if (CollectionUtils.isNotEmpty(rotations)) {
            rotations.forEach(r -> result.rotateXYZ(MathUtils.toRadians(r.x()), MathUtils.toRadians(r.y()), MathUtils.toRadians(r.z())));
        }
        return result;
    }

    private Vector3f mapTranslation(final Vector3f translation) {
        if (Objects.isNull(translation)) {
            return new Vector3f();
        }
        return new Vector3f(translation);
    }

    private Vector3f mapScale(final Vector3f scale) {
        if (Objects.isNull(scale)) {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
        return new Vector3f(scale);
    }

    private void mapComponent(final GameObject gameObject, final Map<String, Object> componentData) {
        final var type = (String) componentData.get("type");
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("A component should have a type");
        }
        gameObject.addComponent(componentFactory.createComponent(type, componentData));
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }
}
