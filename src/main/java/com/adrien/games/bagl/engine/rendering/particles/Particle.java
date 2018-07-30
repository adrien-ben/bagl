package com.adrien.games.bagl.engine.rendering.particles;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.engine.Time;
import org.joml.Vector3f;

public class Particle {

    private Vector3f position;
    private Vector3f direction;
    private float size;
    private float speed;
    private Color startColor;
    private Color endColor;
    private float ttl;

    private final Color color = new Color(0, 0, 0, 0);
    private float timeLeft;
    private boolean alive;

    public Particle(Vector3f position, Vector3f direction, float size, float speed, Color startColor, Color endColor, float ttl) {
        this.reset(position, direction, size, speed, startColor, endColor, ttl);
    }

    public Particle() {
        this(new Vector3f(), new Vector3f(), 1, 0, Color.WHITE, Color.WHITE, 0);
    }

    public void update(Time time) {
        this.alive = this.timeLeft > 0;
        if (this.alive) {
            final var elapsedTime = time.getElapsedTime();
            this.timeLeft -= elapsedTime;
            this.direction.normalize().mul(elapsedTime * this.speed);
            this.position.add(this.direction);

            final var life = this.timeLeft / this.ttl;
            Color.blend(this.startColor, this.endColor, life, this.color);
        }
    }

    public void reset(Vector3f position, Vector3f direction, float size, float speed, Color startColor, Color endColor, float ttl) {
        this.position = position;
        this.direction = direction;
        this.size = size;
        this.speed = speed;
        this.startColor = startColor;
        this.endColor = endColor;
        this.ttl = ttl;

        this.color.set(startColor);
        this.timeLeft = ttl;
        this.alive = this.timeLeft > 0;
    }

    public boolean isAlive() {
        return alive;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
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

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

}
