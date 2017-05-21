package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;

/**
 * 3D camera class.
 *
 */
public class Camera {

    private static final Matrix4 buffer = Matrix4.createZero();

    private Vector3 position;
    private Vector3 direction;
    private Vector3 up;
    private Vector3 side;

    private final float fov;
    private final float aspectRatio;
    private final float zNear;
    private final float zFar;

    private final Matrix4 projection;
    private final Matrix4 view;
    private final Matrix4 viewProj;
    private final Matrix4 viewAtOrigin;
    private final Matrix4 viewProjAtOrigin;

    private boolean dirtyProj;
    private boolean dirtyView;
    private boolean dirtyViewAtOrigin;
    private boolean dirtyViewProj;
    private boolean dirtyViewProjAtOrigin;

    public Camera(Vector3 position, Vector3 direction, Vector3 up, float fovRads, float aspectRatio, float zNear, float zFar) {
        this.position = position;
        this.direction = direction;
        this.up = up;
        this.side = Vector3.cross(this.direction, this.up);

        this.fov = fovRads;
        this.aspectRatio = aspectRatio;
        this.zNear = zNear;
        this.zFar = zFar;

        this.projection = Matrix4.createPerspective(this.fov, aspectRatio, zNear, zFar);
        this.view = Matrix4.createLookAt(position, Vector3.add(position, direction), up);
        this.viewProj = Matrix4.mul(this.projection, this.view);
        this.viewAtOrigin = Matrix4.createLookAt(Vector3.ZERO, direction, up);
        this.viewProjAtOrigin = Matrix4.mul(this.projection, this.viewAtOrigin);

        this.dirtyProj = false;
        this.dirtyView = false;
        this.dirtyViewAtOrigin = false;
        this.dirtyViewProj = false;
        this.dirtyViewProjAtOrigin = false;
    }

    /**
     * Rotates the camera.
     * @param rotation The rotation quaternion.
     * @return This for chaining.
     */
    public Camera rotate(Quaternion rotation) {
        buffer.setRotation(rotation);
        this.direction.transform(buffer, 0);
        this.up.transform(buffer, 0);
        Vector3.cross(this.direction, this.up, this.side);
        this.dirtyView = true;
        this.dirtyViewAtOrigin = true;
        this.dirtyViewProj = true;
        this.dirtyViewProjAtOrigin = true;
        return this;
    }

    /**
     * Moves the camera in a direction. This only changes
     * the position of the camera not its orientation.
     * @param direction The direction to move towards.
     * @return This for chaining.
     */
    public Camera move(Vector3 direction) {
        this.position.add(direction);
        this.dirtyView = true;
        this.dirtyViewProj = true;
        return this;
    }

    /**
     * Returns the projection matrix.
     * @return The projection matrix.
     */
    public Matrix4 getProjection() {
        if(this.dirtyProj) {
            this.projection.setPerspective(this.fov, this.aspectRatio, this.zNear, this.zFar);
            this.dirtyProj = false;
        }
        return this.projection;
    }

    /**
     * Returns the view matrix.
     * @return The view matrix.
     */
    public Matrix4 getView() {
        if(this.dirtyView) {
            this.view.setLookAt(this.position, Vector3.add(this.position, this.direction), this.up);
            this.dirtyView = false;
        }
        return this.view;
    }

    /**
     * Returns a matrix which is the product of view and projection
     * matrices.
     * @return The view/projection matrix.
     */
    public Matrix4 getViewProj() {
        if(this.dirtyViewProj) {
            Matrix4.mul(this.getProjection(), this.getView(), this.viewProj);
            this.dirtyViewProj = false;
        }
        return this.viewProj;
    }

    public Matrix4 getViewAtOrigin() {
        if(this.dirtyViewAtOrigin) {
            this.viewAtOrigin.setLookAt(Vector3.ZERO, this.direction, this.up);
            this.dirtyViewAtOrigin = false;
        }
        return this.viewAtOrigin;
    }

    /**
     * Returns a matrix which is the product of view and projection
     * matrices with the camera positioned at the origin (0, 0, 0).
     * @return The view/projection matrix at origin.
     */
    public Matrix4 getViewProjAtOrigin() {
        if(this.dirtyViewProjAtOrigin) {
            Matrix4.mul(this.getProjection(), this.getViewAtOrigin(), this.viewProjAtOrigin);
            this.dirtyViewProjAtOrigin = false;
        }
        return this.viewProjAtOrigin;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public Vector3 getUp() {
        return up;
    }

    public Vector3 getSide() {
        return this.side;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        this.dirtyView = true;
        this.dirtyViewProj = true;
    }

    public void setDirection(Vector3 direction) {
        this.direction = direction;
        Vector3.cross(this.direction, this.up, this.side);
        this.dirtyView = true;
        this.dirtyViewAtOrigin = true;
        this.dirtyViewProj = true;
        this.dirtyViewProjAtOrigin = true;
    }

    public void setUp(Vector3 up) {
        this.up = up;
        Vector3.cross(this.direction, this.up, this.side);
        this.dirtyView = true;
        this.dirtyViewProj = true;
        this.dirtyViewAtOrigin = true;
        this.dirtyViewProjAtOrigin = true;
    }

}
