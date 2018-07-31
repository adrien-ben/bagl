package com.adrienben.games.bagl.engine.camera;

import com.adrienben.games.bagl.core.utils.Dirtiable;
import org.joml.*;

/**
 * 3D camera class
 *
 * @author adrien
 */
public class Camera {

    private final Vector3f position;
    private final Vector3f direction;
    private final Vector3f target;
    private final Vector3f up;
    private final Vector3f side;

    private final float fov;
    private final float aspectRatio;
    private final float zNear;
    private final float zFar;

    private final Dirtiable<Matrix4f> projection;
    private final Dirtiable<Matrix4f> view;
    private final Dirtiable<Matrix4f> viewProj;
    private final Dirtiable<Matrix4f> invertedViewProj;
    private final Dirtiable<Matrix4f> viewAtOrigin;
    private final Dirtiable<Matrix4f> viewProjAtOrigin;

    public Camera(
            final Vector3fc position,
            final Vector3fc direction,
            final Vector3fc up,
            final float fovRads,
            final float aspectRatio,
            final float zNear,
            final float zFar
    ) {
        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        this.target = new Vector3f(position).add(direction);
        this.up = new Vector3f(up);
        this.side = new Vector3f(this.direction).cross(this.up);

        this.fov = fovRads;
        this.aspectRatio = aspectRatio;
        this.zNear = zNear;
        this.zFar = zFar;

        // camera
        this.projection = new Dirtiable<>(new Matrix4f(), projection -> projection.setPerspective(this.fov, this.aspectRatio, this.zNear, this.zFar));
        this.view = new Dirtiable<>(new Matrix4f(), view -> view.setLookAt(this.position, this.target, this.up));
        this.viewProj = new Dirtiable<>(new Matrix4f(), viewProj -> this.projection.get().mulPerspectiveAffine(this.view.get(), viewProj));

        // inverted
        this.invertedViewProj = new Dirtiable<>(new Matrix4f(), inverted -> this.projection.get().invertPerspectiveView(this.view.get(), inverted));

        // at origin
        this.viewAtOrigin = new Dirtiable<>(new Matrix4f(), viewAtOrigin -> viewAtOrigin.setLookAlong(this.direction, this.up));
        this.viewProjAtOrigin = new Dirtiable<>(new Matrix4f(), atOrigin -> this.projection.get().mulPerspectiveAffine(this.viewAtOrigin.get(), atOrigin));
    }

    /**
     * Rotate the camera
     *
     * @param rotation The rotation quaternion
     * @return This for chaining
     */
    public Camera rotate(final Quaternionfc rotation) {
        this.direction.rotate(rotation);
        this.up.rotate(rotation);
        this.position.add(this.direction, this.target);
        this.direction.cross(this.up, this.side);
        this.view.dirty();
        this.viewProj.dirty();
        this.invertedViewProj.dirty();
        this.viewAtOrigin.dirty();
        this.viewProjAtOrigin.dirty();
        return this;
    }

    /**
     * Move the camera in a direction. This only changes
     * the position of the camera not its orientation
     *
     * @param direction The direction to move towards
     * @return This for chaining
     */
    public Camera move(final Vector3fc direction) {
        this.position.add(direction);
        this.position.add(this.direction, this.target);
        this.view.dirty();
        this.viewProj.dirty();
        this.invertedViewProj.dirty();
        return this;
    }

    /**
     * Return the projection matrix
     *
     * @return The projection matrix
     */
    public Matrix4fc getProjection() {
        return this.projection.get();
    }

    /**
     * Return the view matrix
     *
     * @return The view matrix
     */
    public Matrix4fc getView() {
        return this.view.get();
    }

    /**
     * Get a matrix which is the product of view and projection
     * matrices
     *
     * @return The view/projection matrix
     */
    public Matrix4fc getViewProj() {
        return this.viewProj.get();
    }

    /**
     * Get the inverse matrix of the combined view/projection matrix
     *
     * @return The inverse of the view/projection matrix
     */
    public Matrix4fc getInvertedViewProj() {
        return this.invertedViewProj.get();
    }

    /**
     * Get the view matrix at origin
     *
     * @return The view matrix at origin
     */
    public Matrix4fc getViewAtOrigin() {
        return this.viewAtOrigin.get();
    }

    /**
     * Return a matrix which is the product of view and projection
     * matrices with the camera positioned at the origin (0, 0, 0)
     *
     * @return The view/projection matrix at origin
     */
    public Matrix4fc getViewProjAtOrigin() {
        return this.viewProjAtOrigin.get();
    }

    public Vector3fc getPosition() {
        return this.position;
    }

    public Vector3fc getDirection() {
        return this.direction;
    }

    public Vector3fc getUp() {
        return this.up;
    }

    public Vector3fc getSide() {
        return this.side;
    }

    public void setPosition(final Vector3fc position) {
        this.position.set(position);
        this.position.add(this.direction, this.target);
        this.view.dirty();
        this.viewProj.dirty();
        this.invertedViewProj.dirty();
    }

    public void setDirection(final Vector3fc direction) {
        this.direction.set(direction);
        this.position.add(this.direction, this.target);
        this.direction.cross(this.up, this.side);
        this.view.dirty();
        this.viewProj.dirty();
        this.invertedViewProj.dirty();
        this.viewAtOrigin.dirty();
        this.viewProjAtOrigin.dirty();
    }

    public void setUp(final Vector3fc up) {
        this.up.set(up);
        this.direction.cross(this.up, this.side);
        this.view.dirty();
        this.viewProj.dirty();
        this.invertedViewProj.dirty();
        this.viewAtOrigin.dirty();
        this.viewProjAtOrigin.dirty();
    }
}
