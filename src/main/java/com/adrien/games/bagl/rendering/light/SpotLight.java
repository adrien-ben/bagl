package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Vector3;

public class SpotLight extends PointLight {

	private Vector3 direction;
	private float angle;
	
	public SpotLight(float intensity, Color color, Vector3 position, float radius, Attenuation attenuation, Vector3 direction, float angle) {
		super(intensity, color, position, radius, attenuation);
		this.direction = direction;
		this.angle = angle;
	}

	public Vector3 getDirection() {
		return direction;
	}

	public void setDirection(Vector3 direction) {
		this.direction = direction;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
	
}
