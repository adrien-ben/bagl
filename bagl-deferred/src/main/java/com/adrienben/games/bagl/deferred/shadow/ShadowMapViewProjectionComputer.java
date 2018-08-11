package com.adrienben.games.bagl.deferred.shadow;

import com.adrienben.games.bagl.core.math.Frustum;
import com.adrienben.games.bagl.core.math.Vectors;
import com.adrienben.games.bagl.engine.rendering.light.DirectionalLight;
import org.joml.AABBf;
import org.joml.Matrix4f;

/**
 * Compute the view projection matrix for shadow map generation.
 *
 * @author adrien
 */
public class ShadowMapViewProjectionComputer {

    private static final int NEAR_PLANE_BIAS = 0;

    private final Frustum frustum = new Frustum();
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
        frustum.set(sceneAABB);
        frustum.transform(lightWorld);
    }

    private void updateLightSpaceSceneAABB() {
        frustum.computeBoundingBox(lightSpaceSceneAABB);
    }
}
