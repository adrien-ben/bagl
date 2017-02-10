package com.adrien.games.bagl.rendering;

import java.util.Objects;

public class Material {
	
	private Texture diffuse;
	private float specularExponent;
	private float specularIntensity;
	
	public Material(Texture diffuse, float specularExponent, float specularIntensity) {
		this.diffuse = diffuse;
		this.specularExponent = specularExponent;
		this.specularIntensity = specularIntensity;
	}
	
	public void destroy() {
		if(Objects.nonNull(diffuse)) {			
			this.diffuse.destroy();
		}
	}
	
	public Texture getDiffuseTexture() {
		return diffuse;
	}
	
	public float getSpecularExponent() {
		return specularExponent;
	}
	
	public float getSpecularIntensity() {
		return specularIntensity;
	}

	public void setDiffuse(Texture diffuse) {
		this.diffuse = diffuse;
	}

	public void setSpecularExponent(float specularExponent) {
		this.specularExponent = specularExponent;
	}

	public void setSpecularIntensity(float specularIntensity) {
		this.specularIntensity = specularIntensity;
	}
	
}
