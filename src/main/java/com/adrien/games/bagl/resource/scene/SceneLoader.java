package com.adrien.games.bagl.resource.scene;

import com.adrien.games.bagl.core.Transform;
import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.resource.scene.descriptors.GameObjectDescriptor;
import com.adrien.games.bagl.resource.scene.descriptors.SceneDescriptor;
import com.adrien.games.bagl.resource.scene.descriptors.TransformDescriptor;
import com.adrien.games.bagl.scene.GameObject;
import com.adrien.games.bagl.scene.Scene;
import com.adrien.games.bagl.utils.CollectionUtils;
import com.google.gson.Gson;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public SceneLoader() {
        this.gson = new Gson();
        this.componentFactory = new ComponentFactory(this.gson);
    }

    public Scene load(final String filePath) {
        final var sceneDescriptor = loadFile(filePath);
        final var scene = new Scene();
        sceneDescriptor.getChildren().forEach(child -> mapGameObject(scene.getRoot(), child));
        return scene;
    }

    private SceneDescriptor loadFile(final String filePath) {
        try {
            return gson.fromJson(Files.newBufferedReader(Paths.get(filePath)), SceneDescriptor.class);
        } catch (final IOException exception) {
            throw new EngineException("Failed to load scene file " + filePath, exception);
        }
    }

    private void mapGameObject(final GameObject parent, final GameObjectDescriptor gameObjectDescriptor) {
        final GameObject gameObject;
        if (Objects.nonNull(gameObjectDescriptor.getTags())) {
            final var tagArray = gameObjectDescriptor.getTags().toArray(new String[0]);
            gameObject = parent.createChild(gameObjectDescriptor.getId(), tagArray);
        } else {
            gameObject = parent.createChild(gameObjectDescriptor.getId());
        }
        if (Objects.nonNull(gameObjectDescriptor.getTransform())) {
            updateTransform(gameObject.getLocalTransform(), gameObjectDescriptor.getTransform());
        }
        if (CollectionUtils.isNotEmpty(gameObjectDescriptor.getComponents())) {
            gameObjectDescriptor.getComponents().forEach(component -> mapComponent(gameObject, component));
        }
        if (CollectionUtils.isNotEmpty(gameObjectDescriptor.getChildren())) {
            gameObjectDescriptor.getChildren().forEach(child -> mapGameObject(gameObject, child));
        }
    }

    private void updateTransform(final Transform transform, final TransformDescriptor transformDescriptor) {
        transform
                .setTranslation(mapTranslation(transformDescriptor.getTranslation()))
                .setRotation(quaternionFromRotation(transformDescriptor.getRotation()))
                .setScale(mapScale(transformDescriptor.getScale()));
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

    private Quaternionf quaternionFromRotation(final Vector3f rotation) {
        if (Objects.isNull(rotation)) {
            return new Quaternionf();
        }
        return new Quaternionf().rotation(
                (float) Math.toRadians(rotation.x),
                (float) Math.toRadians(rotation.y),
                (float) Math.toRadians(rotation.z));
    }

    private void mapComponent(final GameObject gameObject, final Map<String, Object> componentData) {
        final var type = (String) componentData.get("type");
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("A component should have a type");
        }
        gameObject.addComponent(componentFactory.createComponent(type, componentData));
    }
}
