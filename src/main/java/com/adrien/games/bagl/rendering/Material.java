package com.adrien.games.bagl.rendering;

import java.util.Objects;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.texture.Texture;

public class Material {
	
	private static final int DIFFUSE_MAP_CHANNEL = 0;
	private static final int SPECULAR_MAP_CHANNEL = 1;
	private static final int NORMAL_MAP_CHANNEL = 2;
	
	private Color diffuseColor = Color.WHITE;
	private Texture diffuseMap = null;
	private float specularExponent = 0.0f;
	private float specularIntensity = 0.0f;
	private Texture specularMap = null;
	private Texture bumpMap = null;
	
	/**
	 * Apply the current material to a shader.
	 * @param shader The shader to apply the material to.
	 */
	public void applyTo(Shader shader) {
		if(this.hasDiffuseMap()) {
			shader.setUniform("uMaterial.diffuseMap", DIFFUSE_MAP_CHANNEL);
			this.diffuseMap.bind(0);
		}
		if(this.hasSpecularMap()) {
			shader.setUniform("uMaterial.specularMap", SPECULAR_MAP_CHANNEL);
			this.specularMap.bind(1);
		}
		if(this.hasBumpMap()) {
			shader.setUniform("uMaterial.bumpMap", NORMAL_MAP_CHANNEL);
			this.bumpMap.bind(2);
		}
		shader.setUniform("uMaterial.diffuseColor", this.diffuseColor);
		shader.setUniform("uMaterial.hasDiffuseMap", this.hasDiffuseMap());
		shader.setUniform("uMaterial.shininess", this.specularIntensity);
		shader.setUniform("uMaterial.hasSpecularMap", this.hasSpecularMap());
		shader.setUniform("uMaterial.glossiness", this.specularExponent);
		shader.setUniform("uMaterial.hasBumpMap", this.hasBumpMap());
	}
	
	public boolean hasDiffuseMap() {
		return Objects.nonNull(this.diffuseMap);
	}
	
	public boolean hasSpecularMap() {
		return Objects.nonNull(this.specularMap);
	}
	
	public boolean hasBumpMap() {
		return Objects.nonNull(this.bumpMap);
	}
	
	public void destroy() {
		if(Objects.nonNull(diffuseMap)) {			
			this.diffuseMap.destroy();
		}
	}

	public Color getDiffuseColor() {
		return diffuseColor;
	}

	public void setDiffuseColor(Color diffuseColor) {
		this.diffuseColor = diffuseColor;
	}

	public Texture getDiffuseMap() {
		return diffuseMap;
	}

	public void setDiffuseMap(Texture diffuseMap) {
		this.diffuseMap = diffuseMap;
	}

	public float getSpecularExponent() {
		return specularExponent;
	}

	public void setSpecularExponent(float specularExponent) {
		this.specularExponent = specularExponent;
	}

	public float getSpecularIntensity() {
		return specularIntensity;
	}

	public void setSpecularIntensity(float specularIntensity) {
		this.specularIntensity = specularIntensity;
	}

	public Texture getSpecularMap() {
		return specularMap;
	}

	public void setSpecularMap(Texture specularMap) {
		this.specularMap = specularMap;
	}

	public Texture getBumpMap() {
		return bumpMap;
	}

	public void setBumpMap(Texture bumpMap) {
		this.bumpMap = bumpMap;
	}
	
}
