package com.adrien.games.bagl.resource.scene;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Configuration;
import com.adrien.games.bagl.core.camera.Camera;
import com.adrien.games.bagl.core.math.Vectors;
import com.adrien.games.bagl.rendering.environment.EnvironmentMapGenerator;
import com.adrien.games.bagl.rendering.light.DirectionalLight;
import com.adrien.games.bagl.rendering.light.PointLight;
import com.adrien.games.bagl.rendering.light.SpotLight;
import com.adrien.games.bagl.rendering.model.ModelFactory;
import com.adrien.games.bagl.resource.scene.json.*;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.components.*;
import com.adrien.games.bagl.utils.MathUtils;
import com.adrien.games.bagl.utils.ResourcePath;
import com.google.gson.Gson;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.adrien.games.bagl.utils.AssertUtils.validate;

/**
 * This class is responsible for creating game component.
 *
 * @author adrien
 */
public class ComponentFactory {

    private final Map<String, Function<Object, Component>> commands;
    private final EnvironmentMapGenerator environmentMapGenerator;
    private final Gson gson;

    public ComponentFactory() {
        this.commands = new HashMap<>();
        this.environmentMapGenerator = new EnvironmentMapGenerator();
        this.gson = new Gson();

        initCommands();
    }

    private void initCommands() {
        addComponentCreationCommand("model", ModelJson.class, this::createModelComponent);
        addComponentCreationCommand("camera", CameraJson.class, this::createCameraComponent);
        addComponentCreationCommand("environment", EnvironmentJson.class, this::createEnvironmentComponent);
        addComponentCreationCommand("directional_light", DirectionalLightJson.class, this::createDirectionalLightComponent);
        addComponentCreationCommand("point_light", PointLightJson.class, this::createPointLightComponent);
        addComponentCreationCommand("spot_light", SpotLightJson.class, this::createSpotLightComponent);
    }

    /**
     * Add a component creation command.
     * <p>
     * This method is used to extend the scene model so user can add component types
     * to the scene json model. {@code jsonClass} is the class representing the json model.
     * {@code mapper} is a {@link Function} that will map the deserialized json object to the
     * component.
     */
    public <T> void addComponentCreationCommand(final String type, final Class<T> jsonClass, final Function<T, Component> mapper) {
        final Function<Object, T> deserializer = obj -> convertValue(obj, jsonClass);
        commands.put(type, deserializer.andThen(mapper));
    }

    /**
     * Add a component creation command.
     * <p>
     * This method is used to extend the scene model so user can add component types
     * to the json scene model. {@code jsonClass} is the class representing the json model.
     * <p>
     * Use this method if the json model matches the component model.
     */
    public <T extends Component> void addComponentCreationCommand(final String type, final Class<T> jsonClass) {
        commands.put(type, obj -> convertValue(obj, jsonClass));
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
        final var command = commands.get(type);
        if (Objects.isNull(command)) {
            throw new IllegalArgumentException(String.format("This component type is not supported: %s", type));
        }
        return command.apply(componentData);
    }

    private <T> T convertValue(final Object value, final Class<T> type) {
        return gson.fromJson(gson.toJsonTree(value), type);
    }

    private ModelComponent createModelComponent(final ModelJson modelJson) {
        final var path = validate(modelJson.getPath(), Objects::nonNull, "Model component should have a path field");

        final var model = ModelFactory.fromFile(ResourcePath.get(path));
        return new ModelComponent(model);
    }

    private CameraComponent createCameraComponent(final CameraJson cameraJson) {
        final var fov = validate(cameraJson.getFov(), Objects::nonNull, "Camera component should have a fov field");
        final var near = validate(cameraJson.getNear(), Objects::nonNull, "Camera component should have a near field");
        final var far = validate(cameraJson.getFar(), Objects::nonNull, "Camera component should have a far field");
        final var hasController = defaultIfNull(cameraJson.isEnableController(), false);

        final var conf = Configuration.getInstance();
        final var aspectRatio = (float) conf.getXResolution() / (float) conf.getYResolution();
        final var camera = new Camera(Vectors.VEC3_ZERO, Vectors.VEC3_ZERO, Vectors.VEC3_UP, MathUtils.toRadians(fov), aspectRatio, near, far);
        return new CameraComponent(camera, hasController);
    }

    private EnvironmentComponent createEnvironmentComponent(final EnvironmentJson environmentJson) {
        final var path = validate(environmentJson.getPath(), Objects::nonNull, "Environment component should have a path field");

        final var environmentMap = environmentMapGenerator.generateEnvironmentMap(ResourcePath.get(path));
        final var irradianceMap = environmentMapGenerator.generateIrradianceMap(environmentMap);
        final var preFilteredMap = environmentMapGenerator.generatePreFilteredMap(environmentMap);
        return new EnvironmentComponent(environmentMap, irradianceMap, preFilteredMap);
    }

    private DirectionalLightComponent createDirectionalLightComponent(final DirectionalLightJson directionalLightJson) {
        final var intensity = validate(directionalLightJson.getIntensity(), Objects::nonNull, "Directional light component should have an intensity field");
        final var color = validate(directionalLightJson.getColor(), Objects::nonNull, "Directional light component should have an color field");

        final var light = new DirectionalLight(intensity, mapColor(color), new Vector3f());
        return new DirectionalLightComponent(light);
    }

    private PointLightComponent createPointLightComponent(final PointLightJson pointLightJson) {
        final var intensity = validate(pointLightJson.getIntensity(), Objects::nonNull, "Point light component should have an intensity field");
        final var color = validate(pointLightJson.getColor(), Objects::nonNull, "Point light component should have an color field");
        final var radius = validate(pointLightJson.getRadius(), Objects::nonNull, "Point light component should have a radius field");

        final var light = new PointLight(intensity, mapColor(color), new Vector3f(), radius);
        return new PointLightComponent(light);
    }

    private SpotLightComponent createSpotLightComponent(final SpotLightJson spotLightJson) {
        final var intensity = validate(spotLightJson.getIntensity(), Objects::nonNull, "Spot light component should have an intensity field");
        final var color = validate(spotLightJson.getColor(), Objects::nonNull, "Spot light component should have an color field");
        final var radius = validate(spotLightJson.getRadius(), Objects::nonNull, "Spot light component should have a radius field");
        final var angle = validate(spotLightJson.getAngle(), Objects::nonNull, "Spot light component should have a angle field");
        final var edge = validate(spotLightJson.getEdge(), Objects::nonNull, "Spot light component should have a edge field");

        final var light = new SpotLight(intensity, mapColor(color), new Vector3f(), radius, new Vector3f(), angle, edge);
        return new SpotLightComponent(light);
    }

    private <T> T defaultIfNull(final T value, final T defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }

    private Color mapColor(final ColorJson color) {
        return new Color(color.getR(), color.getG(), color.getB());
    }
}
