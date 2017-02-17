package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Vector3;

public class PointLight extends Light {

	private Vector3 position;
	private float radius;
	
	public PointLight(Vector3 position, float radius) {
		super();
		this.position = position;
		this.radius = radius;
	}
	
	public PointLight(float intensity, Color color, Vector3 position, float radius) {
		super(intensity, color);
		this.position = position;
		this.radius = radius;
	}

	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
}
