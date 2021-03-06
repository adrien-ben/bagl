package com.adrienben.games.bagl.engine.resource.scene;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.core.math.MathUtils;
import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.core.validation.Validation;
import com.adrienben.games.bagl.engine.Configuration;
import com.adrienben.games.bagl.engine.assets.AssetStore;
import com.adrienben.games.bagl.engine.camera.Camera;
import com.adrienben.games.bagl.engine.rendering.environment.EnvironmentMapGenerator;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import com.adrienben.games.bagl.engine.rendering.light.PointLight;
import com.adrienben.games.bagl.engine.rendering.light.SpotLight;
import com.adrienben.games.bagl.engine.rendering.model.Model;
import com.adrienben.games.bagl.engine.rendering.model.ModelFactory;
import com.adrienben.games.bagl.engine.rendering.particles.Particle;
import com.adrienben.games.bagl.engine.rendering.particles.ParticleEmitter;
import com.adrienben.games.bagl.engine.resource.scene.json.*;
import com.adrienben.games.bagl.engine.scene.Component;
import com.adrienben.games.bagl.engine.scene.components.*;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.google.gson.Gson;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.adrienben.games.bagl.core.math.MathUtils.random;

/**
 * This class is responsible for creating game component.
 *
 * @author adrien
 */
public class ComponentFactory {

    private final Map<String, Function<Object, Component>> componentCreationCommands;
    private final EnvironmentMapGenerator environmentMapGenerator;
    private AssetStore assetStore;
    private final Gson gson;

    public ComponentFactory(final EnvironmentMapGenerator environmentMapGenerator) {
        this.componentCreationCommands = new HashMap<>();
        this.environmentMapGenerator = environmentMapGenerator;
        this.gson = new Gson();

        initComponentCreationCommands();
    }

    private void initComponentCreationCommands() {
        addComponentCreationCommand("model", ModelJson.class, this::createModelComponent);
        addComponentCreationCommand("camera", CameraJson.class, this::createCameraComponent);
        addComponentCreationCommand("environment", EnvironmentJson.class, this::createEnvironmentComponent);
        addComponentCreationCommand("directional_light", DirectionalLightJson.class, this::createDirectionalLightComponent);
        addComponentCreationCommand("point_light", PointLightJson.class, this::createPointLightComponent);
        addComponentCreationCommand("spot_light", SpotLightJson.class, this::createSpotLightComponent);
        addComponentCreationCommand("particles", ParticleJson.class, this::createParticleComponent);
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
        componentCreationCommands.put(type, deserializer.andThen(mapper));
    }

    /**
     * Add a component creation command.
     * <p>
     * This method is used to extend the scene model so user can add component types
     * to the json scene model. {@code jsonClass} is the class representing the json model
     * which extends {@link Component}.
     * <p>
     * Use this method if the json model matches the component model.
     */
    public <T extends Component> void addComponentCreationCommand(final String type, final Class<T> jsonClass) {
        componentCreationCommands.put(type, obj -> convertValue(obj, jsonClass));
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
        final var command = componentCreationCommands.get(type);
        if (Objects.isNull(command)) {
            throw new IllegalArgumentException(String.format("This component type is not supported: %s", type));
        }
        return command.apply(componentData);
    }

    private <T> T convertValue(final Object value, final Class<T> type) {
        return gson.fromJson(gson.toJsonTree(value), type);
    }

    private ModelComponent createModelComponent(final ModelJson modelJson) {
        checkModelData(modelJson);

        return getModelComponent(modelJson);
    }

    private void checkModelData(final ModelJson modelJson) {
        final var path = modelJson.getPath();
        final var id = modelJson.getId();
        if (Objects.isNull(path) && Objects.isNull(id) || Objects.nonNull(path) && Objects.nonNull(id)) {
            throw new IllegalArgumentException("Model component should have a path OR id field");
        }
    }

    private ModelComponent getModelComponent(final ModelJson modelJson) {
        if (Objects.nonNull(modelJson.getPath())) {
            return new ModelComponent(ModelFactory.fromFile(ResourcePath.get(modelJson.getPath())), true);
        } else {
            return new ModelComponent(assetStore.getAsset(modelJson.getId(), Model.class), false);
        }
    }

    private CameraComponent createCameraComponent(final CameraJson cameraJson) {
        final var fov = Validation.validate(cameraJson.getFov(), Objects::nonNull, "Camera component should have a fov field");
        final var near = Validation.validate(cameraJson.getNear(), Objects::nonNull, "Camera component should have a near field");
        final var far = Validation.validate(cameraJson.getFar(), Objects::nonNull, "Camera component should have a far field");
        final var hasController = defaultIfNull(cameraJson.isEnableController(), false);

        final var conf = Configuration.getInstance();
        final var aspectRatio = (float) conf.getXResolution() / (float) conf.getYResolution();
        final var camera = new Camera(Vectors.VEC3_ZERO, Vectors.VEC3_ZERO, Vectors.VEC3_UP, MathUtils.toRadians(fov), aspectRatio, near, far);
        return new CameraComponent(camera, hasController);
    }

    private EnvironmentComponent createEnvironmentComponent(final EnvironmentJson environmentJson) {
        final var path = Validation.validate(environmentJson.getPath(), Objects::nonNull, "Environment component should have a path field");

        final var environmentMap = environmentMapGenerator.generateEnvironmentMap(ResourcePath.get(path));
        final var irradianceMap = environmentMapGenerator.generateIrradianceMap(environmentMap);
        final var preFilteredMap = environmentMapGenerator.generatePreFilteredMap(environmentMap);
        return new EnvironmentComponent(environmentMap, irradianceMap, preFilteredMap);
    }

    private DirectionalLightComponent createDirectionalLightComponent(final DirectionalLightJson directionalLightJson) {
        final var intensity = Validation.validate(directionalLightJson.getIntensity(), Objects::nonNull, "Directional light component should have an intensity field");
        final var color = Validation.validate(directionalLightJson.getColor(), Objects::nonNull, "Directional light component should have an color field");

        final var light = new DirectionalLight(intensity, mapColor(color), new Vector3f());
        return new DirectionalLightComponent(light);
    }

    private PointLightComponent createPointLightComponent(final PointLightJson pointLightJson) {
        final var intensity = Validation.validate(pointLightJson.getIntensity(), Objects::nonNull, "Point light component should have an intensity field");
        final var color = Validation.validate(pointLightJson.getColor(), Objects::nonNull, "Point light component should have an color field");
        final var radius = Validation.validate(pointLightJson.getRadius(), Objects::nonNull, "Point light component should have a radius field");

        final var light = new PointLight(intensity, mapColor(color), new Vector3f(), radius);
        return new PointLightComponent(light);
    }

    private SpotLightComponent createSpotLightComponent(final SpotLightJson spotLightJson) {
        final var intensity = Validation.validate(spotLightJson.getIntensity(), Objects::nonNull, "Spot light component should have an intensity field");
        final var color = Validation.validate(spotLightJson.getColor(), Objects::nonNull, "Spot light component should have an color field");
        final var radius = Validation.validate(spotLightJson.getRadius(), Objects::nonNull, "Spot light component should have a radius field");
        final var angle = Validation.validate(spotLightJson.getAngle(), Objects::nonNull, "Spot light component should have a angle field");
        final var edge = Validation.validate(spotLightJson.getEdge(), Objects::nonNull, "Spot light component should have a edge field");

        final var light = new SpotLight(intensity, mapColor(color), new Vector3f(), radius, new Vector3f(), angle, edge);
        return new SpotLightComponent(light);
    }

    private <T> T defaultIfNull(final T value, final T defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }

    private Color mapColor(final ColorJson color) {
        return new Color(color.getR(), color.getG(), color.getB());
    }

    private ParticleComponent createParticleComponent(final ParticleJson particleJson) {
        final var texture = getParticleTexture(particleJson);
        final var blendMode = Validation.validate(particleJson.getBlendMode(), Objects::nonNull, "Particle component should have a blendMode field");
        final var rate = Validation.validate(particleJson.getRate(), v -> v > 0, "Particle component should have a rate superior to 0");
        final var batchSize = Validation.validate(particleJson.getBatchSize(), v -> v > 0, "Particle component should have a batchSize superior to 0");
        final var initializer = mapParticleInitializer(particleJson);

        final var builder = ParticleEmitter.builder().texture(texture).blendMode(blendMode).rate(rate).batchSize(batchSize).initializer(initializer);
        return new ParticleComponent(builder.build());
    }

    private Texture2D getParticleTexture(final ParticleJson particleJson) {
        checkParticleTextureLink(particleJson);
        final var texturePath = particleJson.getTexturePath();
        final var textureId = particleJson.getTextureId();

        if (Objects.nonNull(texturePath)) {
            return Texture2D.fromFile(ResourcePath.get(texturePath), TextureParameters.builder());
        } else if (Objects.nonNull(textureId)) {
            return assetStore.getAsset(textureId, Texture2D.class);
        }
        return null;
    }

    private void checkParticleTextureLink(final ParticleJson particleJson) {
        if (Objects.nonNull(particleJson.getTexturePath()) && Objects.nonNull(particleJson.getTextureId())) {
            throw new IllegalArgumentException("Particle component can only have 'texturePath', 'textureId' or none defined. Both are defined.");
        }
    }

    private Consumer<Particle> mapParticleInitializer(final ParticleJson particleJson) {
        final var initializer = Validation.validate(particleJson.getInitializer(), Objects::nonNull, "Particle component should have an initializer field");
        final var position = Validation.validate(initializer.getPosition(), Objects::nonNull, "Particle component's initializer should have a position field");
        final var direction = Validation.validate(initializer.getDirection(), Objects::nonNull, "Particle component's initializer should have a direction field");
        final var size = Validation.validate(initializer.getSize(), Objects::nonNull, "Particle component's initializer should have a size field");
        final var speed = Validation.validate(initializer.getSpeed(), Objects::nonNull, "Particle component's initializer should have a speed field");
        final var startColor = Validation.validate(particleJson.getStartColor(), Objects::nonNull, "Particle component should have a startColor field");
        final var endColor = Validation.validate(particleJson.getEndColor(), Objects::nonNull, "Particle component should have an endColor field");
        final var ttl = Validation.validate(initializer.getTtl(), Objects::nonNull, "Particle component's initializer should have a ttl field");

        return particle -> particle.reset(Vectors.randomInRange(position), Vectors.randomInRange(direction).normalize(),
                random(size), random(speed), mapColor(startColor), mapColor(endColor), random(ttl));
    }

    public void setAssetStore(final AssetStore assetStore) {
        this.assetStore = assetStore;
    }
}
