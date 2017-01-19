package com.adrien.games.bagl.core;


public final class Vector4
{
	private float x;
	private float y;
	private float z;
	private float w;
	
	public Vector4()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.w = 1;
	}
	
	public Vector4(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Vector4(Vector3 xyz, float w)
	{
		this.x = xyz.getX();
		this.y = xyz.getY();
		this.z = xyz.getZ();
		this.w = w;
	}
	
	public Vector4(Vector4 other)
	{
		this.x = other.getX();
		this.y = other.getY();
		this.z = other.getZ();
		this.w = other.getW();
	}
	
	public boolean isZero()
	{
		return x == 0 && y == 0 && z == 0 && w == 0;
	}

	/**
	 * Transform the current vector by the Matrix4 matrix.
	 * @param matrix The transformation matrix.
	 */
	public void transform(Matrix4 matrix)
	{
		float _x = matrix.getM11()*getX() + matrix.getM12()*getY() + matrix.getM13()*getZ() + matrix.getM14()*getW();
		float _y = matrix.getM21()*getX() + matrix.getM22()*getY() + matrix.getM23()*getZ() + matrix.getM24()*getW();
		float _z = matrix.getM31()*getX() + matrix.getM32()*getY() + matrix.getM33()*getZ() + matrix.getM34()*getW();
		float _w = matrix.getM41()*getX() + matrix.getM42()*getY() + matrix.getM43()*getZ() + matrix.getM44()*getW();
		
		setXYZW(_x, _y, _z, _w);
	}
	
	/**
	 * Return a vector which is the tranformation of the passed in vector by the passed in Matrix.
	 * @param matrix The transformation matrix.
	 * @param vector The vector to be tranformed.
	 * @return
	 */
	public static Vector4 transform(Matrix4 matrix, Vector4 vector)
	{
		float _x = matrix.getM11()*vector.getX() + matrix.getM12()*vector.getY() + matrix.getM13()*vector.getZ() + matrix.getM14()*vector.getW();
		float _y = matrix.getM21()*vector.getX() + matrix.getM22()*vector.getY() + matrix.getM23()*vector.getZ() + matrix.getM24()*vector.getW();
		float _z = matrix.getM31()*vector.getX() + matrix.getM32()*vector.getY() + matrix.getM33()*vector.getZ() + matrix.getM34()*vector.getW();
		float _w = matrix.getM41()*vector.getX() + matrix.getM42()*vector.getY() + matrix.getM43()*vector.getZ() + matrix.getM44()*vector.getW();

		return new Vector4(_x,  _y,  _z,  _w);
	}
	
	/**
	 * Return a vector which is the tranformation of the passed in vector by the passed in Matrix.
	 * @param matrix The transformation matrix.
	 * @param vector The vector to be tranformed.
	 * @return
	 */
	public static void transform(Matrix4 matrix, Vector4 vector, Vector4 result)
	{
		float _x = matrix.getM11()*vector.getX() + matrix.getM12()*vector.getY() + matrix.getM13()*vector.getZ() + matrix.getM14()*vector.getW();
		float _y = matrix.getM21()*vector.getX() + matrix.getM22()*vector.getY() + matrix.getM23()*vector.getZ() + matrix.getM24()*vector.getW();
		float _z = matrix.getM31()*vector.getX() + matrix.getM32()*vector.getY() + matrix.getM33()*vector.getZ() + matrix.getM34()*vector.getW();
		float _w = matrix.getM41()*vector.getX() + matrix.getM42()*vector.getY() + matrix.getM43()*vector.getZ() + matrix.getM44()*vector.getW();

		result.setXYZW(_x, _y, _z, _w);
	}

	
	@Override
	public String toString()
	{
		return "x:" + this.x + " y:" + this.y + " z:" + this.z + " w:" + this.w;
	}
	
	public Vector3 getXYZ()
	{
		return new Vector3(x, y, z);
	}
	
	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public float getZ()
	{
		return z;
	}
	
	public float getW()
	{
		return w;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public void setY(float y)
	{
		this.y = y;
	}

	public void setZ(float z)
	{
		this.z = z;
	}

	public void setW(float w)
	{
		this.w = w;
	}
	
	public void setXYZW(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
}
