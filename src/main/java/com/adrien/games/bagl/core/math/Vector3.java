package com.adrien.games.bagl.core.math;

public final class Vector3 {
	
	public static final Vector3 ZERO = new Vector3();
	public static final Vector3 UP = new Vector3(0, 1, 0);
	public static final Vector3 DOWN = new Vector3(0, -1, 0);
	public static final Vector3 RIGHT = new Vector3(1, 0, 0);
	public static final Vector3 LEFT = new Vector3(-1, 0, 0);
	public static final Vector3 FORWARD = new Vector3(0, 0, -1);
	public static final Vector3 BACKWARD = new Vector3(0, 0, 1);
	
	private float x;
	private float y;
	private float z;
	
	public Vector3() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3(Vector3 other) {
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
	}
	
	public boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}
	
	public float length() {
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
		
	/**
	 * Normalise the current vector.
	 */
	public void normalise() {
		float length = length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
	}
	
	/**
	 * Normalise the Vector vector and store the result in result.
	 * The current vector is not modified.
	 * @param result The vector in which the result will be stored. 
	 */
	public void normalise(Vector3 result) {
		float length = length();
		result.setX(getX() / length);
		result.setY(getY() / length);
		result.setZ(getZ() / length);
	}
	
	/**
	 * Adds a Vector3 to the current Vector3.
	 * @param other The vector to add to the current one.
	 */
	public void add(Vector3 other) {
		x += other.getX();
		y += other.getY();
		z += other.getZ();
	}
	
	/**
	 * Adds two Vector3 and returns a new Vector3.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @return A new Vector3 which is the addition of left and right.
	 */
	public static Vector3 add(Vector3 left, Vector3 right) {
		float _x = left.getX() + right.getX();
		float _y = left.getY() + right.getY();
		float _z = left.getZ() + right.getZ();
		return new Vector3(_x, _y, _z);
	}
	
	/**
	 * Adds two Vector3 and fill an existing Vector3 with the result.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @param result The vector in which the result will be stored.
	 */
	public static void add(Vector3 left, Vector3 right, Vector3 result) {
		float _x = left.getX() + right.getX();
		float _y = left.getY() + right.getY();
		float _z = left.getZ() + right.getZ();
		result.setXYZ(_x, _y, _z);
	}
	
	/**
	 * Subs a Vector3 to the current Vector3.
	 * @param other The Vector3 to substract to the current one.
	 */
	public void sub(Vector3 other) {
		x -= other.getX();
		y -= other.getY();
		z -= other.getZ();
	}
	
	/**
	 * Subs two Vector3 and returns a new Vector3.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @return A new Vector3 which is the substraction of left and right.
	 */
	public static Vector3 sub(Vector3 left, Vector3 right) {
		float _x = left.getX() - right.getX();
		float _y = left.getY() - right.getY();
		float _z = left.getZ() - right.getZ();
		return new Vector3(_x, _y, _z);
	}
	
	/**
	 * Subs two Vector3 and fill an existing Vector3 with the result.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @param result The vector in which the result will be stored
	 */
	public static void sub(Vector3 left, Vector3 right, Vector3 result) {
		float _x = left.getX() - right.getX();
		float _y = left.getY() - right.getY();
		float _z = left.getZ() - right.getZ();
		result.setXYZ(_x, _y, _z);
	}
	
	/**
	 * Multiplies this vector by another one.
	 * @param other The vector to multiply to this one.
	 * @return This for chaining.
	 */
	public Vector3 mul(Vector3 other) {
		this.x *= other.x;
		this.y *= other.y;
		this.z *= other.z;
		return this;
	}
	
	/**
	 * Multiplies two vector.
	 * @param left The left vector.
	 * @param right The right vector.
	 * @return A new vector which is the product of two vectors.
	 */
	public Vector3 mul(Vector3 left, Vector3 right) {
		return new Vector3(left.x*right.x, left.y*right.y, left.z*right.z);
	}
	
	/**
	 * Multiplies two vector and store the result in another one.
	 * @param left The left vector. Will not be changed.
	 * @param right The right vector. Will not be changed.
	 * @param result The vector where to store the result.
	 */
	public void mul(Vector3 left, Vector3 right, Vector3 result) {
		result.setXYZ(left.x*right.x, left.y*right.y, left.z*right.z);
	}
	
	public void scale(float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
	}
	
	public void scale(float factor, Vector3 result) {
		result.setX(x * factor);
		result.setY(y * factor);
		result.setZ(z * factor);
	}
	
	public void average(Vector3 other) {
		x = (x + other.x)/2;
		y = (y + other.y)/2;
		z = (z + other.z)/2;
	}
	
	public void average(Vector3 other, Vector3 result) {
		result.setX((x + other.x)/2);
		result.setY((y + other.y)/2);
		result.setZ((z + other.z)/2);
	}
	
	public static Vector3 cross(Vector3 left, Vector3 right) {
		float _x = left.getY()*right.getZ() - left.getZ()*right.getY();
		float _y = left.getZ()*right.getX() - left.getX()*right.getZ();
		float _z = left.getX()*right.getY() - left.getY()*right.getX();
		return new Vector3(_x, _y, _z);
	}
	
	public static void cross(Vector3 left, Vector3 right, Vector3 result) {
		float _x = left.getY()*right.getZ() - left.getZ()*right.getY();
		float _y = left.getZ()*right.getX() - left.getX()*right.getZ();
		float _z = left.getX()*right.getY() - left.getY()*right.getX();
		result.setXYZ(_x, _y, _z);
	}
	
	public float dot(Vector3 other) {
		return this.x*other.getX() + this.y*other.getY() + this.z*other.getZ();
	}
	
	/**
	 * Transform the current vector by the Matrix4 matrix.
	 * @param matrix The transformation matrix.
	 */
	public void transform(Matrix4 matrix, float w) {
		float _x = matrix.getM11()*getX() + matrix.getM12()*getY() + matrix.getM13()*getZ() + w*matrix.getM14();
		float _y = matrix.getM21()*getX() + matrix.getM22()*getY() + matrix.getM23()*getZ() + w*matrix.getM24();
		float _z = matrix.getM31()*getX() + matrix.getM32()*getY() + matrix.getM33()*getZ() + w*matrix.getM34();
		setXYZ(_x, _y, _z);
	}
	
	/**
	 * Return a vector which is the tranformation of the passed in vector by the passed in Matrix.
	 * @param matrix The transformation matrix.
	 * @param vector The vector to be tranformed.
	 * @return
	 */
	public static Vector3 transform(Matrix4 matrix, Vector3 vector, float w) {
		float _x = matrix.getM11()*vector.getX() + matrix.getM12()*vector.getY() + matrix.getM13()*vector.getZ() + w*matrix.getM14();
		float _y = matrix.getM21()*vector.getX() + matrix.getM22()*vector.getY() + matrix.getM23()*vector.getZ() + w*matrix.getM24();
		float _z = matrix.getM31()*vector.getX() + matrix.getM32()*vector.getY() + matrix.getM33()*vector.getZ() + w*matrix.getM34();
		return new Vector3(_x, _y, _z);
	}
	
	/**
	 * Return a vector which is the tranformation of the passed in vector by the passed in Matrix.
	 * @param matrix The transformation matrix.
	 * @param vector The vector to be tranformed.
	 * @return
	 */
	public static void transform(Matrix4 matrix, Vector3 vector, float w, Vector3 result) {
		float _x = matrix.getM11()*vector.getX() + matrix.getM12()*vector.getY() + matrix.getM13()*vector.getZ() + w*matrix.getM14();
		float _y = matrix.getM21()*vector.getX() + matrix.getM22()*vector.getY() + matrix.getM23()*vector.getZ() + w*matrix.getM24();
		float _z = matrix.getM31()*vector.getX() + matrix.getM32()*vector.getY() + matrix.getM33()*vector.getZ() + w*matrix.getM34();
		result.setXYZ(_x, _y, _z);
	}
	
	@Override
	public String toString() {
		return "x:" + this.x + " y:" + this.y + " z:" + this.z;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setZ(float z) {
		this.z = z;
	}
	
	public void setXYZ(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
}
