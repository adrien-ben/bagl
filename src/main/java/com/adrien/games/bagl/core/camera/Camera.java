package com.adrien.games.bagl.core.camera;

import org.joml.*;

/**
 * 3D camera class
 *
 * @author adrien
 */
public class Camera {

    private Vector3f position;
    private Vector3f direction;
    private Vector3f target;
    private Vector3f up;
    private Vector3f side;

    private final float fov;
    private final float aspectRatio;
    private final float zNear;
    private final float zFar;

    private final Matrix4f projection;
    private final Matrix4f view;
    private final Matrix4f viewProj;
    private final Matrix4f invertedViewProj;
    private final Matrix4f viewAtOrigin;
    private final Matrix4f viewProjAtOrigin;

    private boolean dirtyProj;
    private boolean dirtyView;
    private boolean dirtyViewProj;
    private boolean dirtyInvertedViewProj;
    private boolean dirtyViewAtOrigin;
    private boolean dirtyViewProjAtOrigin;

    public Camera(final Vector3fc position, final Vector3fc direction, final Vector3fc up, final float fovRads, final float aspectRatio,
                  final float zNear, final float zFar) {
        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        this.target = new Vector3f(position).add(direction);
        this.up = new Vector3f(up);
        this.side = new Vector3f(this.direction).cross(this.up);

        this.fov = fovRads;
        this.aspectRatio = aspectRatio;
        this.zNear = zNear;
        this.zFar = zFar;

        this.projection = new Matrix4f().setPerspective(this.fov, this.aspectRatio, this.zNear, this.zFar);

        this.view = new Matrix4f().setLookAt(this.position, this.target, this.up);
        this.viewProj = new Matrix4f(this.projection).mulPerspectiveAffine(this.view);
        this.invertedViewProj = new Matrix4f();
        this.projection.invertPerspectiveView(this.view, this.invertedViewProj);

        this.viewAtOrigin = new Matrix4f().setLookAlong(this.direction, this.up);
        this.viewProjAtOrigin = new Matrix4f(this.projection).mulPerspectiveAffine(this.viewAtOrigin);
        this.dirtyProj = false;
        this.dirtyView = false;
        this.dirtyViewProj = false;
        this.dirtyInvertedViewProj = false;
        this.dirtyViewAtOrigin = false;
        this.dirtyViewProjAtOrigin = false;
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
        this.dirtyView = true;
        this.dirtyViewProj = true;
        this.dirtyInvertedViewProj = true;
        this.dirtyViewAtOrigin = true;
        this.dirtyViewProjAtOrigin = true;
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
        this.dirtyView = true;
        this.dirtyViewProj = true;
        this.dirtyInvertedViewProj = true;
        return this;
    }

    /**
     * Return the projection matrix
     *
     * @return The projection matrix
     */
    public Matrix4fc getProjection() {
        if (this.dirtyProj) {
            this.projection.setPerspective(this.fov, this.aspectRatio, this.zNear, this.zFar);
            this.dirtyProj = false;
        }
        return this.projection;
    }

    /**
     * Return the view matrix
     *
     * @return The view matrix
     */
    public Matrix4fc getView() {
        if (this.dirtyView) {
            this.view.setLookAt(this.position, this.target, this.up);
            this.dirtyView = false;
        }
        return this.view;
    }

    /**
     * Get a matrix which is the product of view and projection
     * matrices
     *
     * @return The view/projection matrix
     */
    public Matrix4fc getViewProj() {
        if (this.dirtyViewProj) {
            this.getProjection().mulPerspectiveAffine(this.getView(), this.viewProj);
            this.dirtyViewProj = false;
        }
        return this.viewProj;
    }

    /**
     * Get the inverse matrix of the combined view/projection matrix
     *
     * @return The inverse of the view/projection matrix
     */
    public Matrix4fc getInvertedViewProj() {
        if (this.dirtyInvertedViewProj) {
            this.getProjection().invertPerspectiveView(this.getView(), this.invertedViewProj);
            this.dirtyInvertedViewProj = false;
        }
        return this.invertedViewProj;
    }

    /**
     * Get the view matrix at origin
     *
     * @return The view matrix at origin
     */
    public Matrix4fc getViewAtOrigin() {
        if (this.dirtyViewAtOrigin) {
            this.viewAtOrigin.setLookAlong(this.direction, this.up);
            this.dirtyViewAtOrigin = false;
        }
        return this.viewAtOrigin;
    }

    /**
     * Return a matrix which is the product of view and projection
     * matrices with the camera positioned at the origin (0, 0, 0)
     *
     * @return The view/projection matrix at origin
     */
    public Matrix4fc getViewProjAtOrigin() {
        if (this.dirtyViewProjAtOrigin) {
            this.getProjection().mulPerspectiveAffine(this.getViewAtOrigin(), this.viewProjAtOrigin);
            this.dirtyViewProjAtOrigin = false;
        }
        return this.viewProjAtOrigin;
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
        this.dirtyView = true;
        this.dirtyViewProj = true;
        this.dirtyInvertedViewProj = true;
    }

    public void setDirection(final Vector3fc direction) {
        this.direction.set(direction);
        this.position.add(this.direction, this.target);
        this.direction.cross(this.up, this.side);
        this.dirtyView = true;
        this.dirtyViewProj = true;
        this.dirtyInvertedViewProj = true;
        this.dirtyViewAtOrigin = true;
        this.dirtyViewProjAtOrigin = true;
    }

    public void setUp(final Vector3fc up) {
        this.up.set(up);
        this.direction.cross(this.up, this.side);
        this.dirtyView = true;
        this.dirtyViewProj = true;
        this.dirtyInvertedViewProj = true;
        this.dirtyViewAtOrigin = true;
        this.dirtyViewProjAtOrigin = true;
    }
}
