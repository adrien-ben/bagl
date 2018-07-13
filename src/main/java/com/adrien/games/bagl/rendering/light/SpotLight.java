package com.adrien.games.bagl.rendering.light;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.utils.MathUtils;
import org.joml.Vector3f;

public class SpotLight extends PointLight {

    private Vector3f direction;
    private float angle;
    private float edge;
    private float cutOff;
    private float outerCutOff;

    public SpotLight(float intensity, Color color, Vector3f position, float radius, Vector3f direction,
                     float angle, float edge) {
        super(intensity, color, position, radius);
        this.direction = direction;
        this.angle = angle;
        this.edge = edge;
        this.updateCutOffs();
    }

    private static float computeCutOff(float angle) {
        return (float) Math.cos(MathUtils.toRadians(angle));
    }

    private void updateCutOffs() {
        this.cutOff = computeCutOff(this.angle);
        this.outerCutOff = computeCutOff(this.angle + this.edge);
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
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
