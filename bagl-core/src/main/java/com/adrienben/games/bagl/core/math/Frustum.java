package com.adrienben.games.bagl.core.math;

import org.joml.*;

import static org.joml.Matrix4fc.*;

/**
 * Frustum.
 *
 * @author adrien
 */
public class Frustum {

    private static final int CORNER_COUNT = 8;

    private final Vector3f bottomLeftNearCorner = new Vector3f();
    private final Vector3f bottomRightNearCorner = new Vector3f();
    private final Vector3f topRightNearCorner = new Vector3f();
    private final Vector3f topLeftNearCorner = new Vector3f();
    private final Vector3f bottomLeftFarCorner = new Vector3f();
    private final Vector3f bottomRightFarCorner = new Vector3f();
    private final Vector3f topRightFarCorner = new Vector3f();
    private final Vector3f topLeftFarCorner = new Vector3f();
    private final Vector3f center = new Vector3f();

    private final Vector3f nearToFarBuffer = new Vector3f();

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
        center.mulPosition(transformation);
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
     * Compute the bounding sphere of this frustum.
     *
     * @param destination The {@link Spheref} where to store the result.
     * @return {@code destination}.
     */
    public Spheref computeBoundingSphere(final Spheref destination) {
        computeCenter();
        final var nearRadius = center.distance(bottomLeftNearCorner);
        final var farRadius = center.distance(bottomLeftFarCorner);
        destination.x = center.x;
        destination.y = center.y;
        destination.z = center.z;
        destination.r = MathUtils.max(nearRadius, farRadius);
        return destination;
    }

    private void computeCenter() {
        center.set(bottomLeftNearCorner);
        center.add(bottomRightNearCorner);
        center.add(topRightNearCorner);
        center.add(topLeftNearCorner);
        center.add(bottomLeftFarCorner);
        center.add(bottomRightFarCorner);
        center.add(topRightFarCorner);
        center.add(topLeftFarCorner);
        center.div(CORNER_COUNT);
    }

    /**
     * Clip this frustum on the z axis and store the result in {@code destination}.
     *
     * @param farFactor    The fraction of the original depth to keep.
     * @param destination The frustum where to store the result.
     * @return destination.
     */
    public Frustum clipZ(final float farFactor, final Frustum destination) {
        return clipZ(0, farFactor, destination);
    }

    /**
     * Clip this frustum on the z axis and store the result in {@code destination}.
     *
     * @param nearFactor  The fraction of the original depth to clip.
     * @param farFactor   The fraction of the original depth to keep.
     * @param destination The frustum where to store the result.
     * @return destination.
     */
    public Frustum clipZ(final float nearFactor, final float farFactor, final Frustum destination) {
        clipEdge(bottomLeftNearCorner, nearFactor, destination.bottomLeftNearCorner, bottomLeftFarCorner, farFactor, destination.bottomLeftFarCorner);
        clipEdge(bottomRightNearCorner, nearFactor, destination.bottomRightNearCorner, bottomRightFarCorner, farFactor, destination.bottomRightFarCorner);
        clipEdge(topRightNearCorner, nearFactor, destination.topRightNearCorner, topRightFarCorner, farFactor, destination.topRightFarCorner);
        clipEdge(topLeftNearCorner, nearFactor, destination.topLeftNearCorner, topLeftFarCorner, farFactor, destination.topLeftFarCorner);
        return destination;
    }

    private void clipEdge(final Vector3f oldNear, final float nearFactor, final Vector3f newNear,
                          final Vector3f oldFar, final float farFactor, final Vector3f newFar) {
        getVectorFromAtoB(oldNear, oldFar, nearToFarBuffer);
        newFar.set(nearToFarBuffer).mul(farFactor).add(oldNear);
        newNear.set(oldNear).add(nearToFarBuffer.mul(nearFactor));
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

    public Frustum set(final Frustum other) {
        bottomLeftNearCorner.set(other.bottomLeftNearCorner);
        bottomRightNearCorner.set(other.bottomRightNearCorner);
        topRightNearCorner.set(other.topRightNearCorner);
        topLeftNearCorner.set(other.topLeftNearCorner);
        bottomLeftFarCorner.set(other.bottomLeftFarCorner);
        bottomRightFarCorner.set(other.bottomRightFarCorner);
        topRightFarCorner.set(other.topRightFarCorner);
        topLeftFarCorner.set(other.topLeftFarCorner);
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
