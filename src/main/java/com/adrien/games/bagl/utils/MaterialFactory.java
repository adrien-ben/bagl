package com.adrien.games.bagl.utils;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.rendering.Material;
import com.adrien.games.bagl.rendering.texture.Texture;

public final class MaterialFactory {

	private MaterialFactory() {
	}
	
	public static Material createWhiteShiny() {
		Material material = new Material();
		material.setDiffuseColor(Color.WHITE);
		material.setSpecularIntensity(1.0f);
		material.setSpecularExponent(32.f);
		return material;
	}
	
	public static Material createDiffuseMap(Texture diffuseMap, float specularIntensity, float specularExponent) {
		Material material = new Material();
		material.setDiffuseMap(diffuseMap);
		material.setSpecularIntensity(specularIntensity);
		material.setSpecularExponent(specularExponent);
		return material;
	}
	
	public static Material createDiffuseMap(Texture diffuseMap, Texture specularMap) {
		Material material = new Material();
		material.setDiffuseMap(diffuseMap);
		material.setSpecularMap(specularMap);
		return material;
	}
	
	public static Material createDiffuseColor(Color diffuseColor, float specularIntensity, float specularExponent) {
		Material material = new Material();
		material.setDiffuseColor(diffuseColor);
		material.setSpecularIntensity(specularIntensity);
		material.setSpecularExponent(specularExponent);
		return material;
	}
		
}
