package com.adrien.games.bagl.resource.scene;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.core.math.Vectors;
import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.model.ModelFactory;
import com.adrien.games.bagl.resource.scene.descriptors.*;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.components.*;
import com.adrien.games.bagl.utils.FileUtils;
import com.adrien.games.bagl.utils.MathUtils;
import com.google.gson.Gson;
import org.joml.Vector3f;

import java.util.Objects;

import static com.adrien.games.bagl.utils.AssertUtils.validate;

/**
 * This class is responsible for creating game component.
 *
 * @author adrien
 */
public class ComponentFactory {

    private EnvironmentMapGenerator environmentMapGenerator;
    private Gson gson;

    public ComponentFactory(final Gson gson) {
        this.environmentMapGenerator = new EnvironmentMapGenerator();
        this.gson = gson;
    }

    /**
     * Create a game component from a {@code type} and the {@code componentData}.
     * <p>
     * It will return a different component implementation according to {@code type}.
     * <p>
     * Throw a {@link IllegalArgumentException} if the type is not supported or if the component data
     * is missing required fields.
     */
    public Component createComponent(final String type, final Object componentData) {
        switch (type) {
            case "model":
                return createModelComponent(convertValue(componentData, ModelDescriptor.class));
            case "camera":
                return createCameraComponent(convertValue(componentData, CameraDescriptor.class));
            case "environment":
                return createEnvironmentComponent(convertValue(componentData, EnvironmentDescriptor.class));
            case "directional_light":
                return createDirectionalLightComponent(convertValue(componentData, DirectionalLightDescriptor.class));
            case "point_light":
                return createPointLightComponent(convertValue(componentData, PointLightDescriptor.class));
            case "spot_light":
                return createSpotLightComponent(convertValue(componentData, SpotLightDescriptor.class));
            default:
                throw new IllegalArgumentException("This component type is not supported: " + type);
        }
    }

    private <T> T convertValue(final Object value, final Class<T> type) {
        return gson.fromJson(gson.toJsonTree(value), type);
    }

    private ModelComponent createModelComponent(final ModelDescriptor modelDescriptor) {
        final var path = validate(modelDescriptor.getPath(), Objects::nonNull, "Model component should have a path field");

        final var model = ModelFactory.fromFile(getAbsolutePath(path));
        return new ModelComponent(model);
    }

    private CameraComponent createCameraComponent(final CameraDescriptor cameraDescriptor) {
        final var fov = validate(cameraDescriptor.getFov(), Objects::nonNull, "Camera component should have a fov field");
        final var near = validate(cameraDescriptor.getNear(), Objects::nonNull, "Camera component should have a near field");
        final var far = validate(cameraDescriptor.getFar(), Objects::nonNull, "Camera component should have a far field");
        final var hasController = defaultIfNull(cameraDescriptor.isEnableController(), false);

        final var conf = Configuration.getInstance();
        final var aspectRatio = (float) conf.getXResolution() / (float) conf.getYResolution();
        final var camera = new Camera(Vectors.VEC3_ZERO, Vectors.VEC3_ZERO, Vectors.VEC3_UP, MathUtils.toRadians(fov), aspectRatio, near, far);
        return new CameraComponent(camera, hasController);
    }

    private EnvironmentComponent createEnvironmentComponent(final EnvironmentDescriptor environmentDescriptor) {
        final var path = validate(environmentDescriptor.getPath(), Objects::nonNull, "Environment component should have a path field");

        final var environmentMap = environmentMapGenerator.generateEnvironmentMap(getAbsolutePath(path));
        final var irradianceMap = environmentMapGenerator.generateIrradianceMap(environmentMap);
        final var preFilteredMap = environmentMapGenerator.generatePreFilteredMap(environmentMap);
        return new EnvironmentComponent(environmentMap, irradianceMap, preFilteredMap);
    }

    private DirectionalLightComponent createDirectionalLightComponent(final DirectionalLightDescriptor directionalLightDescriptor) {
        final var intensity = validate(directionalLightDescriptor.getIntensity(), Objects::nonNull, "Directional light component should have an intensity field");
        final var color = validate(directionalLightDescriptor.getColor(), Objects::nonNull, "Directional light component should have an color field");

        final var light = new DirectionalLight(intensity, mapColor(color), new Vector3f());
        return new DirectionalLightComponent(light);
    }

    private PointLightComponent createPointLightComponent(final PointLightDescriptor pointLightDescriptor) {
        final var intensity = validate(pointLightDescriptor.getIntensity(), Objects::nonNull, "Point light component should have an intensity field");
        final var color = validate(pointLightDescriptor.getColor(), Objects::nonNull, "Point light component should have an color field");
        final var radius = validate(pointLightDescriptor.getRadius(), Objects::nonNull, "Point light component should have a radius field");

        final var light = new PointLight(intensity, mapColor(color), new Vector3f(), radius);
        return new PointLightComponent(light);
    }

    private SpotLightComponent createSpotLightComponent(final SpotLightDescriptor spotLightDescriptor) {
        final var intensity = validate(spotLightDescriptor.getIntensity(), Objects::nonNull, "Spot light component should have an intensity field");
        final var color = validate(spotLightDescriptor.getColor(), Objects::nonNull, "Spot light component should have an color field");
        final var radius = validate(spotLightDescriptor.getRadius(), Objects::nonNull, "Spot light component should have a radius field");
        final var angle = validate(spotLightDescriptor.getAngle(), Objects::nonNull, "Spot light component should have a angle field");
        final var edge = validate(spotLightDescriptor.getEdge(), Objects::nonNull, "Spot light component should have a edge field");

        final var light = new SpotLight(intensity, mapColor(color), new Vector3f(), radius, new Vector3f(), angle, edge);
        return new SpotLightComponent(light);
    }

    private String getAbsolutePath(final String abstractPath) {
        final var split = abstractPath.split(":");
        if (split.length == 1) {
            return split[0];
        } else if (split.length == 2 && "classpath".equals(split[0])) {
            return FileUtils.getResourceAbsolutePath(split[1]);
        } else {
            throw new EngineException("Illegal model path. Should be [classpath:]<file_path>");
        }
    }

    private <T> T defaultIfNull(final T value, final T defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }

    private Color mapColor(final ColorDescriptor color) {
        return new Color(color.getR(), color.getG(), color.getB());
    }
}
