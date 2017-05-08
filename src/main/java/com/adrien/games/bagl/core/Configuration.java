package com.adrien.games.bagl.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Engine configuration class. This singleton class is used to load engine
 * configuration. The instantiation fails if one of the required property 
 * is missing. Required properties are :
 * <ul>
 * <li>resolution.x (integer): resolution of the window alongside the x-axis.
 * <li>resolution.y (integer): resolution of the window alongside the y-axis.
 * <li>anisotropic (integer): level of anisotropic filtering.
 *
 */
public class Configuration {

	private static final Logger log = LogManager.getLogger(Configuration.class);
	
	private static final String CONFIGURATION_FILE_PATH = "/config.properties";
	private static final String X_RESOLUTION_KEY = "resolution.x";
	private static final String Y_RESOLUTION_KEY = "resolution.y";
	private static final String VSYNC_KEY = "vsync";
	private static final String ANISOTROPIC_KEY = "anisotropic";
	
	private final Properties properties;
	private final int xResolution;
	private final int yResolution;
	private final  boolean vsync;
	private final int anisotropicLevel;
	
	private static Configuration instance; 
	
	private Configuration() {
		this.properties = new Properties();
		this.loadFile();
		this.xResolution = this.readRequiredInt(X_RESOLUTION_KEY);
		this.yResolution = this.readRequiredInt(Y_RESOLUTION_KEY);
		this.vsync = this.readRequiredBool(VSYNC_KEY);
		this.anisotropicLevel = this.readRequiredInt(ANISOTROPIC_KEY);
	}
	
	private void loadFile() {
		try (final InputStream inStream = Configuration.class.getResourceAsStream(CONFIGURATION_FILE_PATH)) {			
			this.properties.load(inStream);
		} catch (IOException e) {
			log.error("Failed to load properties file {}", CONFIGURATION_FILE_PATH, e);
			throw new RuntimeException("Failed to load properties file", e);
		}
	}
	
	private int readRequiredInt(final String key) {
		try {
			return Integer.parseInt(this.properties.getProperty(key));
		} catch (NumberFormatException e) {
			log.error("Property {} is not a integer or is missing", key);
			throw new RuntimeException("Property " + key + " is not a integer or is missing");
		}
	}
	
	private boolean readRequiredBool(final String key) {
		final String property = this.properties.getProperty(key);
		if(Objects.isNull(property)) {
			log.error("Property {} is missing", key);
			throw new RuntimeException("Property " + key + " is missing");
		}
		return Boolean.parseBoolean(property);
	}
	
	/**
	 * Returns the instance of the engine configuration.
	 * @return A {@link Configuration} instance.
	 */
	public static Configuration getInstance() {
		if(Objects.isNull(instance)) {
			instance = new Configuration();
		}
		return instance;
	}
	
	/**
	 * Gets a property by name.
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
	
	public int getAnisotropicLevel() {
		return anisotropicLevel;
	}
	
}
