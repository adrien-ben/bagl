package com.adrien.games.bagl.rendering;

import java.util.Objects;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.texture.Texture;

public class Material {
	
	private Color diffuseColor = Color.WHITE;
	private Texture diffuseMap = null;
	private float specularExponent = 0.0f;
	private float specularIntensity = 0.0f;
	private Texture specularMap = null;
	
	public boolean hasDiffuseMap() {
		return Objects.nonNull(this.diffuseMap);
	}
	
	public boolean hasSpecularMap() {
		return Objects.nonNull(this.specularMap);
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
	
}
