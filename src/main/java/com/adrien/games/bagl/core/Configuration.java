package com.adrien.games.bagl.core;

import com.adrien.games.bagl.exception.EngineException;
import com.adrien.games.bagl.rendering.postprocess.fxaa.FxaaPresets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
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
 * <li>fxaa_quality (String) : the preset quality of the fxaa. Should be LOW, MEDIUM or HIGH.
 */
public class Configuration {

    private static final Logger log = LogManager.getLogger(Configuration.class);

    private static final String CONFIGURATION_FILE_PATH = "/config.properties";

    private static Configuration instance;

    private final Properties properties;
    private final int xResolution;
    private final int yResolution;
    private final boolean vsync;
    private final boolean fullscreen;
    private final int anisotropicLevel;
    private final int shadowMapResolution;
    private final FxaaPresets fxaaPresets;

    private Configuration() {
        this.properties = new Properties();
        this.loadFile();
        this.xResolution = this.readRequiredInt("resolution.x");
        this.yResolution = this.readRequiredInt("resolution.y");
        this.vsync = this.readRequiredBool("vsync");
        this.fullscreen = this.readRequiredBool("fullscreen");
        this.anisotropicLevel = this.readRequiredInt("anisotropic");
        this.shadowMapResolution = this.readRequiredInt("shadow_map_resolution");
        this.fxaaPresets = this.readRequiredAndMap("fxaa_quality", FxaaPresets::valueOf);
    }

    private void loadFile() {
        try (final var inStream = Configuration.class.getResourceAsStream(CONFIGURATION_FILE_PATH)) {
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

    private <T> T readRequiredAndMap(final String key, final Function<String, T> mapper) {
        final var property = this.getProperty(key);
        if (Objects.isNull(property)) {
            throw new EngineException("Property " + key + " is missing");
        }
        return mapper.apply(property);
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
        return this.shadowMapResolution;
    }

    public FxaaPresets getFxaaPresets() {
        return fxaaPresets;
    }
}
