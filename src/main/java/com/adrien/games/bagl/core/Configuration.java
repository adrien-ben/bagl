package com.adrien.games.bagl.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Engine configuration class. This singleton class is used to load engine
 * configuration. The instantiation fails if one of the required property
 * is missing. Required properties are :
 * <ul>
 * <li>resolution.x (integer): resolution of the window alongside the x-axis.
 * <li>resolution.y (integer): resolution of the window alongside the y-axis.
 * <li>vsync (boolean) : flag indicating if vsync should be enabled.
 * <li>fullscreen (boolean) : flag indicating if full screen should be enabled.
 * <li>anisotropic (integer): level of anisotropic filtering.
 * <li>shadow_map_resolution : the resolution of the shadow map.
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

    private Configuration() {
        this.properties = new Properties();
        this.loadFile();
        this.xResolution = this.readRequiredInt("resolution.x");
        this.yResolution = this.readRequiredInt("resolution.y");
        this.vsync = this.readRequiredBool("vsync");
        this.fullscreen = this.readRequiredBool("fullscreen");
        this.anisotropicLevel = this.readRequiredInt("anisotropic");
        this.shadowMapResolution = this.readRequiredInt("shadow_map_resolution");
    }

    private void loadFile() {
        try (final InputStream inStream = Configuration.class.getResourceAsStream(CONFIGURATION_FILE_PATH)) {
            this.properties.load(inStream);
        } catch (IOException e) {
            log.error("Failed to load properties file {}", CONFIGURATION_FILE_PATH, e);
            throw new EngineException("Failed to load properties file", e);
        }
    }

    private int readRequiredInt(final String key) {
        try {
            return Integer.parseInt(this.properties.getProperty(key));
        } catch (NumberFormatException e) {
            log.error("Property {} is not a integer or is missing", key);
            throw new EngineException("Property " + key + " is not a integer or is missing");
        }
    }

    private boolean readRequiredBool(final String key) {
        final String property = this.properties.getProperty(key);
        if (Objects.isNull(property)) {
            log.error("Property {} is missing", key);
            throw new EngineException("Property " + key + " is missing");
        }
        return Boolean.parseBoolean(property);
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

}
