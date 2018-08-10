package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import org.joml.AABBf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Compute the view projection matrix for shadow map generation.
 *
 * @author adrien
 */
public class ShadowMapViewProjectionComputer {

    private static final int NEAR_PLANE_BIAS = 0;

    private final Vector3f corner0 = new Vector3f();
    private final Vector3f corner1 = new Vector3f();
    private final Vector3f corner2 = new Vector3f();
    private final Vector3f corner3 = new Vector3f();
    private final Vector3f corner4 = new Vector3f();
    private final Vector3f corner5 = new Vector3f();
    private final Vector3f corner6 = new Vector3f();
    private final Vector3f corner7 = new Vector3f();
    private final Matrix4f lightWorld = new Matrix4f();
    private final AABBf lightSpaceSceneAABB = new AABBf();

    /**
     * Compute the view projection matrix for shadow map generation.
     * <p>
     * The matrix is computed by transforming the scene bounding box into {@code light} space and
     * computing the bounding box of that transformed box. A orthographic projection is then created from this
     * box and is multiply by {@code light}'s view matrix to generated the view-projection matrix.
     * <p>
     * The result is stored in {@code destination} and returned.
     */
    public Matrix4f computeViewProjectionFromCameraAndLight(final DirectionalLight light, final AABBf sceneAABB, final Matrix4f destination) {
        computeLightWorld(light);
        computeSceneAABBInLightSpace(sceneAABB);
        updateLightSpaceSceneAABB();
        return computeViewProjection(destination);
    }

    private void computeLightWorld(final DirectionalLight light) {
        lightWorld.setLookAt(Vectors.VEC3_ZERO, light.getDirection(), Vectors.VEC3_UP);
    }

    private Matrix4f computeViewProjection(final Matrix4f destination) {
        return computeProjection(destination).mulOrthoAffine(lightWorld);
    }

    private Matrix4f computeProjection(final Matrix4f destination) {
        final var zNear = -lightSpaceSceneAABB.maxZ - NEAR_PLANE_BIAS;
        final var zFar = -lightSpaceSceneAABB.minZ;
        destination.setOrtho(lightSpaceSceneAABB.minX, lightSpaceSceneAABB.maxX, lightSpaceSceneAABB.minY, lightSpaceSceneAABB.maxY, zNear, zFar);
        return destination;
    }

    private void computeSceneAABBInLightSpace(final AABBf sceneAABB) {
        corner0.set(sceneAABB.minX, sceneAABB.minY, sceneAABB.minZ).mulPosition(lightWorld);
        corner1.set(sceneAABB.maxX, sceneAABB.minY, sceneAABB.minZ).mulPosition(lightWorld);
        corner2.set(sceneAABB.maxX, sceneAABB.maxY, sceneAABB.minZ).mulPosition(lightWorld);
        corner3.set(sceneAABB.minX, sceneAABB.maxY, sceneAABB.minZ).mulPosition(lightWorld);
        corner4.set(sceneAABB.minX, sceneAABB.minY, sceneAABB.maxZ).mulPosition(lightWorld);
        corner5.set(sceneAABB.maxX, sceneAABB.minY, sceneAABB.maxZ).mulPosition(lightWorld);
        corner6.set(sceneAABB.maxX, sceneAABB.maxY, sceneAABB.maxZ).mulPosition(lightWorld);
        corner7.set(sceneAABB.minX, sceneAABB.maxY, sceneAABB.maxZ).mulPosition(lightWorld);
    }

    private void updateLightSpaceSceneAABB() {
        resetMinAndMax(lightSpaceSceneAABB);
        updateMinAndMax(corner0, lightSpaceSceneAABB);
        updateMinAndMax(corner1, lightSpaceSceneAABB);
        updateMinAndMax(corner2, lightSpaceSceneAABB);
        updateMinAndMax(corner3, lightSpaceSceneAABB);
        updateMinAndMax(corner4, lightSpaceSceneAABB);
        updateMinAndMax(corner5, lightSpaceSceneAABB);
        updateMinAndMax(corner6, lightSpaceSceneAABB);
        updateMinAndMax(corner7, lightSpaceSceneAABB);
    }

    private void resetMinAndMax(final AABBf destination) {
        destination.setMin(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        destination.setMax(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    }

    private void updateMinAndMax(final Vector3f point, final AABBf destination) {
        if (point.x() < destination.minX) {
            destination.minX = point.x();
        }
        if (point.x() > destination.maxX) {
            destination.maxX = point.x();
        }
        if (point.y() < destination.minY) {
            destination.minY = point.y();
        }
        if (point.y() > destination.maxY) {
            destination.maxY = point.y();
        }
        if (point.z() < destination.minZ) {
            destination.minZ = point.z();
        }
        if (point.z() > destination.maxZ) {
            destination.maxZ = point.z();
        }
    }
}
