package com.adrien.games.bagl.rendering.particles;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector3;

public class Particle {

	private Vector3 position;
	private Vector3 direction;
	private float size;
	private float speed;
	private Color color;
	private float ttl;
	
	private boolean alive;
	
	public Particle(Vector3 position, Vector3 direction, float size, float speed, Color color, float ttl) {
		this.reset(position, direction, size, speed, color, ttl);
	}
	
	public Particle() {
		this(new Vector3(), new Vector3(), 1, 0, Color.WHITE, 0);
	}
	
	public void update(Time time) {
		this.alive = this.ttl > 0;
		if(this.alive) {
			final float elapsedTime = time.getElapsedTime();
			this.ttl -= elapsedTime;
			this.direction.normalise();
			this.direction.scale(elapsedTime*this.speed);
			this.position.add(this.direction);
		}
	}
	
	public void reset(Vector3 position, Vector3 direction, float size, float speed, Color color, float ttl) {
		this.position = position;
		this.direction = direction;
		this.size = size;
		this.speed = speed;
		this.color = color;
		this.ttl = ttl;
		this.alive = this.ttl > 0;
	}
	
	public boolean isAlive() {
		return alive;
	}

	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Vector3 getDirection() {
		return direction;
	}

	public void setDirection(Vector3 direction) {
		this.direction = direction;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
}
