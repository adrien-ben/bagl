package com.adrienben.games.bagl.core.math;

import org.joml.AABBf;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.joml.Matrix4fc.*;

/**
 * Frustum.
 *
 * @author adrien
 */
public class Frustum {

    private final Vector3f bottomLeftNearCorner = new Vector3f();
    private final Vector3f bottomRightNearCorner = new Vector3f();
    private final Vector3f topRightNearCorner = new Vector3f();
    private final Vector3f topLeftNearCorner = new Vector3f();
    private final Vector3f bottomLeftFarCorner = new Vector3f();
    private final Vector3f bottomRightFarCorner = new Vector3f();
    private final Vector3f topRightFarCorner = new Vector3f();
    private final Vector3f topLeftFarCorner = new Vector3f();

    /**
     * Construct an identity frustum.
     */
    public Frustum() {
        setIdentity();
    }

    /**
     * Construct a frustum from a projection or view/projection matrix.
     *
     * @param viewProjectionMatrix The matrix to create the frustum from.
     */
    public Frustum(final Matrix4fc viewProjectionMatrix) {
        set(viewProjectionMatrix);
    }

    /**
     * Construct a frustum from an {@link AABBs}.
     *
     * @param boundingBox The bounding box to create the frustum from.
     */
    public Frustum(final AABBf boundingBox) {
        set(boundingBox);
    }

    /**
     * Transform this frustum with a matrix.
     *
     * @param transformation The transformation matrix to apply to this frustum.
     * @return this.
     */
    public Frustum transform(final Matrix4fc transformation) {
        bottomLeftNearCorner.mulPosition(transformation);
        bottomRightNearCorner.mulPosition(transformation);
        topRightNearCorner.mulPosition(transformation);
        topLeftNearCorner.mulPosition(transformation);
        bottomLeftFarCorner.mulPosition(transformation);
        bottomRightFarCorner.mulPosition(transformation);
        topRightFarCorner.mulPosition(transformation);
        topLeftFarCorner.mulPosition(transformation);
        return this;
    }

    /**
     * Compute the {@link AABBs} containing this frustum and store it in {@code destination}.
     *
     * @param destination The {@link AABBs} where to store the result.
     * @return destination.
     */
    public AABBf computeBoundingBox(final AABBf destination) {
        return AABBs.computeAABBOfPoints(destination,
                bottomLeftNearCorner,
                bottomRightNearCorner,
                topRightNearCorner,
                topLeftNearCorner,
                bottomLeftFarCorner,
                bottomRightFarCorner,
                topRightFarCorner,
                topLeftFarCorner);
    }

    /**
     * Clip this frustum on the z axis and store the result in {@code destination}.
     *
     * @param fraction    The fraction of the original depth to keep.
     * @param destination The frustum where to store the result.
     * @return destination.
     */
    public Frustum clipZ(final float fraction, final Frustum destination) {
        copyNearPlaneInto(destination);
        return copyAndClipFarPlaneInto(fraction, destination);
    }

    private Frustum copyNearPlaneInto(final Frustum destination) {
        destination.bottomLeftNearCorner.set(bottomLeftNearCorner);
        destination.bottomRightNearCorner.set(bottomRightNearCorner);
        destination.topRightNearCorner.set(topRightNearCorner);
        destination.topLeftNearCorner.set(topLeftNearCorner);
        return destination;
    }

    private Frustum copyAndClipFarPlaneInto(final float fraction, final Frustum destination) {
        getVectorFromAtoB(bottomLeftNearCorner, bottomLeftFarCorner, destination.bottomLeftFarCorner).mul(fraction).add(bottomLeftNearCorner);
        getVectorFromAtoB(bottomRightNearCorner, bottomRightFarCorner, destination.bottomRightFarCorner).mul(fraction).add(bottomRightNearCorner);
        getVectorFromAtoB(topRightNearCorner, topRightFarCorner, destination.topRightFarCorner).mul(fraction).add(topRightNearCorner);
        getVectorFromAtoB(topLeftNearCorner, topLeftFarCorner, destination.topLeftFarCorner).mul(fraction).add(topLeftNearCorner);
        return destination;
    }

    private Vector3f getVectorFromAtoB(final Vector3fc pointA, final Vector3fc pointB, final Vector3f destination) {
        return pointB.sub(pointA, destination);
    }

    /**
     * Set this frustum to be an identity frustum.
     *
     * @return this.
     */
    public Frustum setIdentity() {
        bottomLeftNearCorner.set(-1f, -1f, -1f);
        bottomRightNearCorner.set(1f, -1f, -1f);
        topRightNearCorner.set(1f, 1f, -1f);
        topLeftNearCorner.set(-1f, 1f, -1f);
        bottomLeftFarCorner.set(-1f, -1f, 1f);
        bottomRightFarCorner.set(1f, -1f, 1f);
        topRightFarCorner.set(1f, 1f, 1f);
        topLeftFarCorner.set(-1f, 1f, 1f);
        return this;
    }

    /**
     * Set this frustum from a projection or view/projection matrix.
     *
     * @param viewProjectionMatrix The matrix to compute the frustum from.
     * @return this.
     */
    public Frustum set(final Matrix4fc viewProjectionMatrix) {
        viewProjectionMatrix.frustumCorner(CORNER_NXNYNZ, bottomLeftNearCorner);
        viewProjectionMatrix.frustumCorner(CORNER_PXNYNZ, bottomRightNearCorner);
        viewProjectionMatrix.frustumCorner(CORNER_PXPYNZ, topRightNearCorner);
        viewProjectionMatrix.frustumCorner(CORNER_NXPYNZ, topLeftNearCorner);
        viewProjectionMatrix.frustumCorner(CORNER_NXNYPZ, bottomLeftFarCorner);
        viewProjectionMatrix.frustumCorner(CORNER_PXNYPZ, bottomRightFarCorner);
        viewProjectionMatrix.frustumCorner(CORNER_PXPYPZ, topRightFarCorner);
        viewProjectionMatrix.frustumCorner(CORNER_NXPYPZ, topLeftFarCorner);
        return this;
    }

    /**
     * Set this frustum from an {@link AABBs}.
     *
     * @param boundingBox The {@link AABBs} to compute the frustum from.
     * @return this.
     */
    public Frustum set(final AABBf boundingBox) {
        bottomLeftNearCorner.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        bottomRightNearCorner.set(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        topRightNearCorner.set(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        topLeftNearCorner.set(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        bottomLeftFarCorner.set(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        bottomRightFarCorner.set(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        topRightFarCorner.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        topLeftFarCorner.set(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        return this;
    }

    public Vector3fc getBottomLeftNearCorner() {
        return bottomLeftNearCorner;
    }

    public Vector3fc getBottomRightNearCorner() {
        return bottomRightNearCorner;
    }

    public Vector3fc getTopRightNearCorner() {
        return topRightNearCorner;
    }

    public Vector3fc getTopLeftNearCorner() {
        return topLeftNearCorner;
    }

    public Vector3fc getBottomLeftFarCorner() {
        return bottomLeftFarCorner;
    }

    public Vector3fc getBottomRightFarCorner() {
        return bottomRightFarCorner;
    }

    public Vector3fc getTopRightFarCorner() {
        return topRightFarCorner;
    }

    public Vector3fc getTopLeftFarCorner() {
        return topLeftFarCorner;
    }
}
