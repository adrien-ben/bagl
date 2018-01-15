package com.adrien.games.bagl.core.math;

/**
 * Quaternion class
 *
 * @author adrien
 */
public class Quaternion {

    private float a;
    private float i;
    private float j;
    private float k;

    public Quaternion() {
        this(0, 0, 0, 0);
    }

    public Quaternion(final float a, final float i, final float j, final float k) {
        this.a = a;
        this.i = i;
        this.j = j;
        this.k = k;
    }

    /**
     * Create a quaternion from a angle and a rotation angle
     *
     * @param angle  The rotation angle
     * @param vector The vector around which the rotation happens
     */
    public static Quaternion fromAngleAndVector(final float angle, final Vector3 vector) {
        float cosa = (float) Math.cos(angle / 2);
        float sina = (float) Math.sin(angle / 2);
        float i = vector.getX() * sina;
        float j = vector.getY() * sina;
        float k = vector.getZ() * sina;
        return new Quaternion(cosa, i, j, k).normalize();
    }

    /**
     * Create a quaternion from a Euler angle
     *
     * @param roll  Rotation around the x-axis
     * @param pitch Rotation around the y-axis
     * @param yaw   Rotation around the z-axis
     */
    public static Quaternion fromEuler(final float roll, final float pitch, final float yaw) {
        float cosroll = (float) Math.cos(roll / 2);
        float sinroll = (float) Math.sin(roll / 2);
        float cospitch = (float) Math.cos(pitch / 2);
        float sinpitch = (float) Math.sin(pitch / 2);
        float cosyaw = (float) Math.cos(yaw / 2);
        float sinyaw = (float) Math.sin(yaw / 2);

        float a = cosyaw * cosroll * cospitch + sinyaw * sinroll * sinpitch;
        float i = cosyaw * sinroll * cospitch - sinyaw * cosroll * sinpitch;
        float j = cosyaw * cosroll * sinpitch + sinyaw * sinroll * cospitch;
        float k = sinyaw * cosroll * cospitch - cosyaw * sinroll * sinpitch;

        return new Quaternion(a, i, j, k);
    }

    /**
     * Multiply to quaternions in the following order : this*q
     *
     * @param q The quaternion to multiply
     * @return This quaternion for chaining
     */
    public Quaternion mul(final Quaternion q) {
        float newA = this.a * q.getA() - this.i * q.getI() - this.j * q.getJ() - this.k * q.getK();
        float newI = this.a * q.getI() + this.i * q.getA() + this.j * q.getK() - this.k * q.getJ();
        float newJ = this.a * q.getJ() - this.i * q.getK() + this.j * q.getA() + this.k * q.getI();
        float newK = this.a * q.getK() + this.i * q.getJ() - this.j * q.getI() + this.k * q.getA();
        this.setAIJK(newA, newI, newJ, newK);
        return this;
    }

    /**
     * Multiply to quaternions
     *
     * @param left  The left operand of the product
     * @param right The right operand of the product
     * @return A {@link Quaternion}
     */
    public static Quaternion mul(final Quaternion left, final Quaternion right) {
        final Quaternion result = new Quaternion();
        result.set(left);
        result.mul(right);
        return result;
    }

    /**
     * Multiply to quaternions and store the result in a passed in quaternion
     *
     * @param left   The left operand of the product
     * @param right  The right operand of the product
     * @param result The quaternion in which to store the result
     */
    public static void mul(final Quaternion left, final Quaternion right, final Quaternion result) {
        result.set(left);
        result.mul(right);
    }

    /**
     * Conjugate the quaternion
     *
     * @return This for chaining
     */
    public Quaternion conjugate() {
        this.i = -this.i;
        this.j = -this.j;
        this.k = -this.k;
        return this;
    }

    /**
     * Computes the norm of the quaternion
     *
     * @return The norm
     */
    public float norm() {
        return (float) Math.sqrt(this.a * this.a + this.i * this.i + this.j * this.j + this.k * this.k);
    }

    /**
     * Normalize the quaternion
     *
     * @return This for chaining
     */
    public Quaternion normalize() {
        final float norm = this.norm();
        this.setAIJK(this.a / norm, this.i / norm, this.j / norm, this.k / norm);
        return this;
    }

    /**
     * Get the forward vector of the quaternion
     *
     * @return A {@link Vector3}
     */
    public Vector3 getDirection() {
        return new Vector3(
                2 * (this.i * this.k + this.a * this.j),
                2 * (this.j * this.k - this.a * this.i),
                1 - 2 * (this.i * this.i + this.j * this.j)
        );
    }

    /**
     * Set the components of the quaternion
     *
     * @param a A
     * @param i I
     * @param j J
     * @param k K
     * @return This for chaining
     */
    public Quaternion setAIJK(final float a, final float i, final float j, final float k) {
        this.a = a;
        this.i = i;
        this.j = j;
        this.k = k;
        return this;
    }

    /**
     * Copy the values of another quaternion
     *
     * @param other The quaternion to copy
     * @return This for chaining
     */
    public Quaternion set(final Quaternion other) {
        this.a = other.a;
        this.i = other.i;
        this.j = other.j;
        this.k = other.k;
        return this;
    }

    @Override
    public String toString() {
        return "a:" + this.a + " i:" + this.i + " j:" + this.j + " k:" + this.k;
    }

    public float getA() {
        return this.a;
    }

    public float getI() {
        return this.i;
    }

    public float getJ() {
        return this.j;
    }

    public float getK() {
        return this.k;
    }

    public void setA(float a) {
        this.a = a;
    }

    public void setI(float i) {
        this.i = i;
    }

    public void setJ(float j) {
        this.j = j;
    }

    public void setK(float k) {
        this.k = k;
    }

}
