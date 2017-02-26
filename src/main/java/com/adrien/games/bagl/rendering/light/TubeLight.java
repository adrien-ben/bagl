package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Vector3;

public class TubeLight extends PointLight {

	private Vector3 direction;
	private float length;
	
	public TubeLight(float intensity, Color color, Vector3 position, float radius, Attenuation attenuation, Vector3 direction, float length) {
		super(intensity, color, position, radius, attenuation);
		this.direction = direction;
		this.length = length;	
	}

	public Vector3 getDirection() {
		return direction;
	}

	public void setDirection(Vector3 direction) {
		this.direction = direction;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}
	
}
