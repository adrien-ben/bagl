package com.adrien.games.bagl.core.math;

/**
 * Tree-dimensional math vector. 
 *
 */
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
		this(0, 0, 0);
	}

	public Vector3(Vector3 other) {
		this(other.x, other.y, other.z);
	}
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}
	
	/**
	 * Computes and returns the squared length of the vector.
	 * @return The length of the vector squared.
	 */
	public float squareLength() {
		return x*x + y*y + z*z;
	}
	
	/**
	 * Computes and return the length of the vector.
	 * @return The length of the vector.
	 */
	public float length() {
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
		
	/**
	 * Normalizes the current vector.
	 * @return This for chaining.
	 */
	public Vector3 normalise() {
		float length = length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}
	
	/**
	 * Normalise the Vector vector and store the result in result.
	 * The current vector is not modified.
	 * @param result The vector in which the result will be stored. 
	 */
	public void normalise(Vector3 result) {
		float length = length();
		result.setXYZ(this.x/length, this.y/length, this.z/length);
	}
	
	/**
	 * Adds a Vector3 to the current Vector3.
	 * @param other The vector to add to the current one.
	 * @return This for chaining.
	 */
	public Vector3 add(Vector3 other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}
	
	/**
	 * Adds two Vector3 and returns a new Vector3.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @return A new Vector3 which is the addition of left and right.
	 */
	public static Vector3 add(Vector3 left, Vector3 right) {
		return new Vector3(left).add(right);
	}
	
	/**
	 * Adds two Vector3 and fill an existing Vector3 with the result.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @param result The vector in which the result will be stored.
	 */
	public static void add(Vector3 left, Vector3 right, Vector3 result) {
		result.set(left).add(right);
	}
	
	/**
	 * Subs a Vector3 to the current Vector3.
	 * @param other The Vector3 to substract to the current one.
	 * @return This for chaining.
	 */
	public Vector3 sub(Vector3 other) {
		this.x -= other.getX();
		this.y -= other.getY();
		this.z -= other.getZ();
		return this;
	}
	
	/**
	 * Subs two Vector3 and returns a new Vector3.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @return A new Vector3 which is the substraction of left and right.
	 */
	public static Vector3 sub(Vector3 left, Vector3 right) {
		return new Vector3(left).sub(right);
	}
	
	/**
	 * Subs two Vector3 and fill an existing Vector3 with the result.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @param result The vector in which the result will be stored
	 */
	public static void sub(Vector3 left, Vector3 right, Vector3 result) {
		result.set(left).sub(right);
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
	public static Vector3 mul(Vector3 left, Vector3 right) {
		return new Vector3(left).mul(right);
	}
	
	/**
	 * Multiplies two vector and store the result in another one.
	 * @param left The left vector. Will not be changed.
	 * @param right The right vector. Will not be changed.
	 * @param result The vector where to store the result.
	 */
	public static void mul(Vector3 left, Vector3 right, Vector3 result) {
		result.set(left).mul(right);
	}
	
	/**
	 * Multiplies a vector by a scalar.
	 * @param factor The scalar.
	 * @return This for chaining.
	 */
	public Vector3 scale(float factor) {
		this.x *= factor;
		this.y *= factor;
		this.z *= factor;
		return this;
	}
	
	/**
	 * Multiplies a vector by a scalar and stores
	 * the result in another vector.
	 * @param factor The scalar.
	 * @param result The vector where to store the result.
	 */
	public void scale(float factor, Vector3 result) {
		result.set(this).scale(factor);
	}
	
	/**
	 * Averages this vector with another vector.
	 * @param other The vector to use to average this one.
	 * @return This for chaining.
	 */
	public Vector3 average(Vector3 other) {
		this.x = (this.x + other.x)/2;
		this.y = (this.y + other.y)/2;
		this.z = (this.z + other.z)/2;
		return this;
	}
	
	/**
	 * Averages this vector with another vector and stores the result
	 * in another vector.
	 * @param other The vector to use to average this one.
	 * @param result The vector where to store the result.
	 */
	public void average(Vector3 other, Vector3 result) {
		result.set(this).average(other);
	}
	
	/**
	 * Computes the cross product of two vectors.
	 * @param left The left vector.
	 * @param right The right vector.
	 * @return A new vector containing the cross product.
	 */
	public static Vector3 cross(Vector3 left, Vector3 right) {
		float _x = left.getY()*right.getZ() - left.getZ()*right.getY();
		float _y = left.getZ()*right.getX() - left.getX()*right.getZ();
		float _z = left.getX()*right.getY() - left.getY()*right.getX();
		return new Vector3(_x, _y, _z);
	}
	
	/**
	 * Computes the cross product of two vectors and
	 * stores the result in another vector.
	 * @param left The left vector.
	 * @param right The right vector.
	 * @param result The vector where to store the result.
	 */
	public static void cross(Vector3 left, Vector3 right, Vector3 result) {
		float _x = left.getY()*right.getZ() - left.getZ()*right.getY();
		float _y = left.getZ()*right.getX() - left.getX()*right.getZ();
		float _z = left.getX()*right.getY() - left.getY()*right.getX();
		result.setXYZ(_x, _y, _z);
	}
	
	/**
	 * Computes the dot product between this vector and another.
	 * @param other The other vector.
	 * @return The dot products of the two vectors.
	 */
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
	
	/**
	 * Sets the components of this vector.
	 * @param x The x component.
	 * @param y The y component.
	 * @param z The z component.
	 * @return This for chaining.
	 */
	public Vector3 setXYZ(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	/**
	 * Sets the components of this vector by copying
	 * those of another vector.
	 * @param other The vector to copy.
	 * @return This for chaining.
	 */
	public Vector3 set(Vector3 other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof Vector3)) {
			return false;
		}
		Vector3 other = (Vector3) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x) 
				|| Float.floatToIntBits(y) != Float.floatToIntBits(other.y) 
				|| Float.floatToIntBits(z) != Float.floatToIntBits(other.z)) {
			return false;
		}
		return true;
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

	public Vector3 setX(float x) {
		this.x = x;
		return this;
	}

	public Vector3 setY(float y) {
		this.y = y;
		return this;
	}

	public Vector3 setZ(float z) {
		this.z = z;
		return this;
	}
	
}
