package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Vector3;

public class SpotLight extends PointLight {

	private Vector3 direction;
	private float angle;
	private float edge;
	private float cutOff;
	private float outerCutOff;
	
	public SpotLight(float intensity, Color color, Vector3 position, float radius, Attenuation attenuation, Vector3 direction, 
			float angle, float edge) {
		super(intensity, color, position, radius, attenuation);
		this.direction = direction;
		this.angle = angle;
		this.edge = edge;
		this.updateCutOffs();
	}

	private static float computeCutOff(float angle) {
		return (float)Math.cos(Math.toRadians(angle));
	}
	
	private void updateCutOffs() {
		this.cutOff = computeCutOff(this.angle);
		this.outerCutOff = computeCutOff(this.angle + this.edge);
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
		this.updateCutOffs();
	}
	
	public float getEdge() {
		return edge;
	}

	public void setEdge(float edge) {
		this.edge = edge;
		this.updateCutOffs();
	}

	public float getCutOff() {
		return cutOff;
	}

	public float getOuterCutOff() {
		return outerCutOff;
	}
	
}
