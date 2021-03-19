package com.adrienben.games.bagl.renderer.shadow;

import com.adrienben.games.bagl.core.math.Frustum;
import org.joml.Matrix4f;
import org.joml.primitives.Spheref;

/**
 * Split data for cascaded shadow mapping.
 *
 * @author adrien
 */
public class CSMSplit {

    private float nearDepth;
    private float farDepth;
    private final Frustum frustum = new Frustum();
    private final Spheref boundingSphere = new Spheref();
    private final Matrix4f lightsViewProjection = new Matrix4f();

    public float getNearDepth() {
        return nearDepth;
    }

    public void setNearDepth(float nearDepth) {
        this.nearDepth = nearDepth;
    }

    public float getFarDepth() {
        return farDepth;
    }

    public void setFarDepth(float farDepth) {
        this.farDepth = farDepth;
    }

    public Frustum getFrustum() {
        return frustum;
    }

    public Spheref getBoundingSphere() {
        return boundingSphere;
    }

    public Matrix4f getLightsViewProjection() {
        return lightsViewProjection;
    }
}
