package com.adrien.games.bagl.core.math;

import java.util.Objects;

/**
 * Represents a 2-dimensional vector.
 */
public final class Vector2 {

	public static final Vector2 ZERO = new Vector2();
	public static final Vector2 UP = new Vector2(0, 1);
	public static final Vector2 DOWN = new Vector2(0, -1);
	public static final Vector2 RIGHT = new Vector2(1, 0);
	public static final Vector2 LEFT = new Vector2(-1, 0);

	/**
	 * x value of the vector.
	 */
	private float x;

	/**
	 * y value of the vector.
	 */
	private float y;

	public Vector2() {
		this.x = 0;
		this.y = 0;
	}

	public Vector2(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2(final Vector2 other) {
		this.x = other.getX();
		this.y = other.getY();
	}

	/**
	 * Checks is a vector is equal to zero.
	 *
	 * @return true if is equal to zero.
	 */
	public boolean isZero() {
		return this.x == 0 && this.y == 0;
	}

	/**
	 * Computes and returns the length of the vector.
	 *
	 * @return The length of the vector.
	 */
	public float length() {
		return (float) Math.sqrt(this.x * this.x + this.y * this.y);
	}

	/**
	 * Normalizes the current vector.
	 */
	public void normalize() {
		final float length = length();
		this.x /= length;
		this.y /= length;
	}

	/**
	 * Normalise the Vector vector and store the result in result.
	 * The current vector is not modified.
	 *
	 * @param result The vector in which the result will be stored.
	 */
	public void normalize(final Vector2 result) {
		final float length = length();
		result.setX(getX() / length);
		result.setY(getY() / length);
	}

	/**
	 * Adds a Vector2 to the current Vector2.
	 *
	 * @param other The vector to add to the current one.
	 */
	public void add(final Vector2 other) {
		this.x += other.getX();
		this.y += other.getY();
	}

	/**
	 * Adds two Vector2 and returns a new Vector2.
	 *
	 * @param left  The first vector.
	 * @param right The second vector.
	 * @return A new Vector2 which is the addition of left and right.
	 */
	public static Vector2 add(final Vector2 left, final Vector2 right) {
		final float xSum = left.getX() + right.getX();
		final float ySum = left.getY() + right.getY();
		return new Vector2(xSum, ySum);
	}

	/**
	 * Adds two Vector2 and fill an existing Vector2 with the result.
	 *
	 * @param left   The first vector.
	 * @param right  The second vector.
	 * @param result The vector in which the result will be stored.
	 */
	public static void add(final Vector2 left, final Vector2 right, final Vector2 result) {
		result.setX(left.getX() + right.getX());
		result.setY(left.getY() + right.getY());
	}

	/**
	 * Subs a Vector2 to the current Vector2.
	 *
	 * @param other The Vector2 to substract to the current one.
	 */
	public void sub(final Vector2 other) {
		this.x -= other.getX();
		this.y -= other.getY();
	}

	/**
	 * Subs two Vector2 and returns a new Vector2.
	 *
	 * @param left  The first vector.
	 * @param right The second vector.
	 * @return A new Vector2 which is the substraction of left and right.
	 */
	public static Vector2 sub(final Vector2 left, final Vector2 right) {
		float xSub = left.getX() - right.getX();
		float ySub = left.getY() - right.getY();
		return new Vector2(xSub, ySub);
	}

	/**
	 * Subs two Vector2 and fill an existing Vector2 with the result.
	 *
	 * @param left   The first vector.
	 * @param right  The second vector.
	 * @param result The vector in which the result will be stored
	 */
	public static void sub(final Vector2 left, final Vector2 right, final Vector2 result) {
		result.setX(left.getX() - right.getX());
		result.setY(left.getY() - right.getY());
	}

	/**
	 * Scales this vector.
	 *
	 * @param factor The factor by which to scale the vector.
	 */
	public void scale(final float factor) {
		this.x *= factor;
		this.y *= factor;
	}

	/**
	 * Scales a vector and stores the result in another one.
	 *
	 * @param factor The vector to scale.
	 * @param result The vector where to store the scaled result.
	 */
	public void scale(final float factor, final Vector2 result) {
		result.setX(this.x * factor);
		result.setY(this.y * factor);
	}

	/**
	 * Computes the dot product between this vector and another one.
	 *
	 * @param other The other vector.
	 * @return The dot product of the two vectors
	 */
	public float dot(final Vector2 other) {
		return this.x * other.getX() + this.y * other.getY();
	}

	/**
	 * Rotates the current vector.
	 *
	 * @param angleInDegrees The rotation angle (in degrees).
	 */
	public void rotate(final float angleInDegrees) {
		final double angleInRads = angleInDegrees * Math.PI / 180f;
		final double cos = Math.cos(angleInRads);
		final double sin = Math.sin(angleInRads);
		this.setX((float) (this.x * cos - this.y * sin));
		this.setY((float) (this.x * sin + this.y * cos));
	}

	/**
	 * Sets the vectors components to the values of another vector.
	 *
	 * @param other The vector to copy the value from.
	 */
	public void set(final Vector2 other) {
		this.setXY(other.x, other.y);
	}

	/**
	 * Sets the vectors x and y components.
	 *
	 * @param x The x component.
	 * @param y The y component.
	 */
	public void setXY(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || !Vector2.class.isInstance(other)) {
			return false;
		}
		final Vector2 otherVector = Vector2.class.cast(other);
		return Float.compare(otherVector.x, x) == 0 &&
				Float.compare(otherVector.y, y) == 0;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "x:" + this.x + " y:" + this.y;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
}
