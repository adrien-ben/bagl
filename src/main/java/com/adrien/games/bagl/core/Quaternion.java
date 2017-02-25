package com.adrien.games.bagl.core;

public class Quaternion {
	
	private float a;
	private float i;
	private float j;
	private float k;
	
	public Quaternion() {
		this.a = 0;
		this.i = 0;
		this.j = 0;
		this.k = 0;
	}
	
	public Quaternion(float a, float i, float j, float k) {
		this.a = a;
		this.i = i;
		this.j = j;
		this.k = k;
	}
	
	public Quaternion(float a, Vector3 vector) {
		this.a = a;
		this.i = vector.getX();
		this.j = vector.getY();
		this.k = vector.getZ();
	}

	public void mul(Quaternion q) {
		float _a = a*q.getA() - i*q.getI() - j*q.getJ() - k*q.getK();
		float _i = a*q.getI() + i*q.getA() + j*q.getK() - k*q.getJ();
		float _j = a*q.getJ() - i*q.getK() + j*q.getA() + k*q.getI();
		float _k = a*q.getK() + i*q.getJ() - j*q.getI() + k*q.getA();
		
		setAIJK(_a, _i, _j, _k);
	}
	
	public static Quaternion mul(Quaternion left, Quaternion right) {
		Quaternion result = new Quaternion();
		result.set(left);
		result.mul(right);
		return result;
	}
	
	public static void mul(Quaternion left, Quaternion right, Quaternion result) {
		result.set(left);
		result.mul(right);
	}
	
	public void conjugate() {
		i = -i;
		j = -j;
		k = -k;
	}
	
	public void conjugate(Quaternion result) {
		result.setAIJK(a, -i, -j, -k);
	}
	
	public float norm() {
		return (float)Math.sqrt(a*a + i*i + j*j + k*k);
	}
	
	@Override
	public String toString() {
		return "a:" + this.a + " i:" + this.i + " j:" + this.j + " k:" + this.k;
	}
	
	public float getA() {
		return a;
	}

	public float getI() {
		return i;
	}

	public float getJ() {
		return j;
	}

	public float getK() {
		return k;
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
	
	public void setAIJK(float a, float i, float j, float k) {
		this.a = a;
		this.i = i;
		this.j = j;
		this.k = k;
	}
	
	public void set(Quaternion other) {
		a = other.getA();
		i = other.getI();
		j = other.getJ();
		k = other.getK();
	}
	
}
