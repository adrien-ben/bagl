package com.adrienben.games.bagl.engine;

import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.core.io.ResourcePath;
import com.adrienben.games.bagl.engine.rendering.postprocess.fxaa.FxaaPresets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Engine configuration class. This singleton class is used to load engine
 * configuration. The instantiation fails if one of the required property
 * is missing. Required properties are :
 * <ul>
 * <li>resolution.x (integer) : resolution of the window alongside the x-axis.
 * <li>resolution.y (integer) : resolution of the window alongside the y-axis.
 * <li>vsync (boolean) : flag indicating if vsync should be enabled.
 * <li>fullscreen (boolean) : flag indicating if full screen should be enabled.
 * <li>anisotropic (integer) : level of anisotropic filtering.
 * <li>shadow_map_resolution (integer) : the resolution of the shadow map.
 * <li>shadow_max_distance (float) : the max distance from the camera at which shadow are displayed
 * <li>shadow_cascade_split_lambda (float) : the ratio between logarithmic and uniform split for cascaded shadow maps
 * <li>fxaa_quality (String) : the preset quality of the fxaa. Should be LOW, MEDIUM or HIGH.
 * <li>assets_descriptor_path (String) : the path of the asset descriptor json file.
 */
public class Configuration {

    private static final Logger log = LogManager.getLogger(Configuration.class);

    private static final String CONFIGURATION_FILE_PATH = "classpath:/config.properties";
    private static final String DEFAULT_ASSETS_DESCRIPTOR_PATH = "classpath:/assets.json";

    private static Configuration instance;

    private final Properties properties;
    private final int xResolution;
    private final int yResolution;
    private final boolean vsync;
    private final boolean fullscreen;
    private final int anisotropicLevel;
    private final int shadowMapResolution;
    private final float shadowMaxDistance;
    private final float shadowCascadeSplitLambda;
    private final FxaaPresets fxaaPresets;
    private final ResourcePath assetDescriptorFilePath;

    private Configuration() {
        this.properties = new Properties();
        this.loadFile();
        this.xResolution = readRequiredInt("resolution.x");
        this.yResolution = readRequiredInt("resolution.y");
        this.vsync = readRequiredBool("vsync");
        this.fullscreen = readRequiredBool("fullscreen");
        this.anisotropicLevel = readRequiredInt("anisotropic");
        this.shadowMapResolution = readRequiredInt("shadow_map_resolution");
        this.shadowMaxDistance = readRequiredFloat("shadow_max_distance");
        this.shadowCascadeSplitLambda = readRequiredFloat("shadow_cascade_split_lambda");
        this.fxaaPresets = readRequiredAndMap("fxaa_quality", FxaaPresets::valueOf);
        this.assetDescriptorFilePath = readAndMapIfPresent("assets_descriptor_path", ResourcePath::get)
                .orElse(ResourcePath.get(DEFAULT_ASSETS_DESCRIPTOR_PATH));
    }

    private void loadFile() {
        try (final var inStream = ResourcePath.get(CONFIGURATION_FILE_PATH).openInputStream()) {
            this.properties.load(inStream);
        } catch (final IOException e) {
            log.error("Failed to load properties file {}", CONFIGURATION_FILE_PATH, e);
            throw new EngineException("Failed to load properties file", e);
        }
    }


    private int readRequiredInt(final String key) {
        try {
            return readRequiredAndMap(key, Integer::parseInt);
        } catch (final NumberFormatException e) {
            throw new EngineException("Property " + key + " is not a valid integer");
        }
    }

    private boolean readRequiredBool(final String key) {
        return readRequiredAndMap(key, Boolean::parseBoolean);
    }

    private float readRequiredFloat(final String key) {
        try {
            return readRequiredAndMap(key, Float::parseFloat);
        } catch (final NumberFormatException e) {
            throw new EngineException("Property " + key + " is not a valid integer");
        }
    }

    private <T> T readRequiredAndMap(final String key, final Function<String, T> mapper) {
        final var property = this.getProperty(key);
        if (Objects.isNull(property)) {
            throw new EngineException("Property " + key + " is missing");
        }
        return mapper.apply(property);
    }

    private <T> Optional<T> readAndMapIfPresent(final String key, final Function<String, T> mapper) {
        final var property = this.getProperty(key);
        if (Objects.isNull(property)) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(property));
    }

    /**
     * Returns the instance of the engine configuration.
     *
     * @return A {@link Configuration} instance.
     */
    public static Configuration getInstance() {
        if (Objects.isNull(instance)) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * Gets a property by name.
     *
     * @param key The name/key of the property.
     * @return The value of the property as a {@link String}.
     */
    public String getProperty(final String key) {
        return this.properties.getProperty(key);
    }

    public int getXResolution() {
        return this.xResolution;
    }

    public int getYResolution() {
        return this.yResolution;
    }

    public boolean getVsync() {
        return this.vsync;
    }

    public boolean getFullscreen() {
        return this.fullscreen;
    }

    public int getAnisotropicLevel() {
        return anisotropicLevel;
    }

    public int getShadowMapResolution() {
        return shadowMapResolution;
    }

    public float getShadowMaxDistance() {
        return shadowMaxDistance;
    }

    public float getShadowCascadeSplitLambda() {
        return shadowCascadeSplitLambda;
    }

    public FxaaPresets getFxaaPresets() {
        return fxaaPresets;
    }

    public ResourcePath getAssetDescriptorFilePath() {
        return assetDescriptorFilePath;
    }
}
