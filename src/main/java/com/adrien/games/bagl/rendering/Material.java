package com.adrien.games.bagl.rendering;

public class Material
{
	private Texture diffuse;
	private float specularExponent;
	private float specularIntensity;
	
	public Material(Texture diffuse, float specularExponent, float specularIntensity)
	{
		this.diffuse = diffuse;
		this.specularExponent = specularExponent;
		this.specularIntensity = specularIntensity;
	}
	
	public void destroy()
	{
		diffuse.destroy();
	}
	
	public Texture getDiffuseTexture()
	{
		return diffuse;
	}
	
	public float getSpecularExponent()
	{
		return specularExponent;
	}
	
	public float getSpecularIntensity()
	{
		return specularIntensity;
	}	
	
}
