package com.adrien.games.bagl.core;

public final class Vector2 {
	
	public static final Vector2 ZERO = new Vector2();
	public static final Vector2 UP = new Vector2(0, 1);
	public static final Vector2 DOWN = new Vector2(0, -1);
	public static final Vector2 RIGHT = new Vector2(1, 0);
	public static final Vector2 LEFT = new Vector2(-1, 0);
	
	private float x;
	private float y;
	
	public Vector2() {
		this.x = 0;
		this.y = 0;
	}
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2(Vector2 other) {
		this.x = other.getX();
		this.y = other.getY();
	}
	
	public boolean isZero() {
		return x == 0 && y == 0;
	}
	
	public float length() {
		return (float)Math.sqrt(x*x + y*y);
	}
	
	/**
	 * Normalise the current vector.
	 */
	public void normalise() {
		float length = length();
		
		this.x /= length;
		this.y /= length;
	}
	
	/**
	 * Normalise the Vector vector and store the result in result.
	 * The current vector is not modified.
	 * @param result The vector in which the result will be stored. 
	 */
	public void normalise(Vector2 result) {
		float length = length();
		
		result.setX(getX() / length);
		result.setY(getY() / length);
	}
	
	/**
	 * Adds a Vector2 to the current Vector2.
	 * @param other The vector to add to the current one.
	 */
	public void add(Vector2 other) {
		x += other.getX();
		y += other.getY();
	}
	
	/**
	 * Adds two Vector2 and returns a new Vector2.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @return A new Vector2 which is the addition of left and right.
	 */
	public static Vector2 add(Vector2 left, Vector2 right) {
		float _x = left.getX() + right.getX();
		float _y = left.getY() + right.getY();
		
		return new Vector2(_x, _y);
	}
	
	/**
	 * Adds two Vector2 and fill an existing Vector2 with the result.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @param result The vector in which the result will be stored.
	 */
	public static void add(Vector2 left, Vector2 right, Vector2 result) {
		float _x = left.getX() + right.getX();
		float _y = left.getY() + right.getY();
		
		result.setX(_x);
		result.setY(_y);
	}
	
	/**
	 * Subs a Vector2 to the current Vector2.
	 * @param other The Vector2 to substract to the current one.
	 */
	public void sub(Vector2 other) {
		x -= other.getX();
		y -= other.getY();
	}
	
	/**
	 * Subs two Vector2 and returns a new Vector2.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @return A new Vector2 which is the substraction of left and right.
	 */
	public static Vector2 sub(Vector2 left, Vector2 right) {
		float _x = left.getX() - right.getX();
		float _y = left.getY() - right.getY();
		
		return new Vector2(_x, _y);
	}
	
	/**
	 * Subs two Vector2 and fill an existing Vector2 with the result.
	 * @param left The first vector.
	 * @param right The second vector.
	 * @param result The vector in which the result will be stored
	 */
	public static void sub(Vector2 left, Vector2 right, Vector2 result) {
		float _x = left.getX() - right.getX();
		float _y = left.getY() - right.getY();
		result.setX(_x);
		result.setY(_y);
	}
	
	public void scale(float factor) {
		x *= factor;
		y *= factor;
	}
	
	public void scale(float factor, Vector2 result) {
		result.setX(x * factor);
		result.setY(y * factor);
	}
	
	public float dot(Vector2 other) {
		return this.x*other.getX() + this.y*other.getY();
	}
	
    /**
     * Rotates the current vector.
     * @param angle The rotation angle (in degrees).
     */
    public void rotate(float angle) {
    	double angleInRads = angle*Math.PI/180f;
    	double cos = Math.cos(angleInRads);
    	double sin = Math.sin(angleInRads);
    	double _x = this.x*cos - this.y*sin;
    	double _y = this.x*sin + this.y*cos;
    	this.setX((float)_x);
    	this.setY((float)_y);
    }
	
	@Override
	public String toString() {
		return "x:" + this.x + " y:" + this.y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}
}
