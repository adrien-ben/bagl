package com.adrien.games.bagl.core;

public final class Matrix4
{
	private static final int M11 = 0;
	private static final int M21 = 1;
	private static final int M31 = 2;
	private static final int M41 = 3;
	private static final int M12 = 4;
	private static final int M22 = 5;
	private static final int M32 = 6;
	private static final int M42 = 7;
	private static final int M13 = 8;
	private static final int M23 = 9;
	private static final int M33 = 10;
	private static final int M43 = 11;
	private static final int M14 = 12;
	private static final int M24 = 13;
	private static final int M34 = 14;
	private static final int M44 = 15;
	
	private float[] matrix;
	
	public Matrix4()
	{
		matrix = new float[16];
		setIdentity();
	}
	
	public Matrix4(Matrix4 other)
	{
		matrix = new float[16];
		for(int i = 0; i < 16; ++i)
		{
			matrix[i] = other.get()[i];
		}
	}
	
	public static Matrix4 createZero()
	{
		Matrix4 result = new Matrix4();
		result.setZero();
		return result;
	}
	
	public void setZero()
	{
		matrix[M11] = 0;	matrix[M12] = 0; 	matrix[M13] = 0; 	matrix[M14] = 0;
		matrix[M21] = 0;	matrix[M22] = 0; 	matrix[M23] = 0; 	matrix[M24] = 0; 
		matrix[M31] = 0;	matrix[M32] = 0; 	matrix[M33] = 0; 	matrix[M34] = 0; 
		matrix[M41] = 0;	matrix[M42] = 0; 	matrix[M43] = 0; 	matrix[M44] = 0; 
	}
	
	public static Matrix4 createIdentity()
	{
		return new Matrix4();
	}
	
	public void setIdentity()
	{
		matrix[M11] = 1;	matrix[M12] = 0; 	matrix[M13] = 0; 	matrix[M14] = 0;
		matrix[M21] = 0;	matrix[M22] = 1; 	matrix[M23] = 0; 	matrix[M24] = 0; 
		matrix[M31] = 0;	matrix[M32] = 0; 	matrix[M33] = 1; 	matrix[M34] = 0; 
		matrix[M41] = 0;	matrix[M42] = 0; 	matrix[M43] = 0; 	matrix[M44] = 1; 
	}
	
	public static Matrix4 createTranslation(Vector3 translation)
	{
		Matrix4 result = new Matrix4();
		result.setTranslation(translation);
		return result;
	}
	
	public void setTranslation(Vector3 translation)
	{		
		matrix[M11] = 1;	matrix[M12] = 0; 	matrix[M13] = 0; 	matrix[M14] = translation.getX();
		matrix[M21] = 0;	matrix[M22] = 1; 	matrix[M23] = 0; 	matrix[M24] = translation.getY(); 
		matrix[M31] = 0;	matrix[M32] = 0; 	matrix[M33] = 1; 	matrix[M34] = translation.getZ(); 
		matrix[M41] = 0;	matrix[M42] = 0; 	matrix[M43] = 0; 	matrix[M44] = 1; 
	}
	
	public static Matrix4 createScale(Vector3 scale)
	{
		Matrix4 result = new Matrix4();
		result.setScale(scale);
		return result;
	}
	
	public void setScale(Vector3 scale)
	{
		matrix[M11] = scale.getX();	matrix[M12] = 0; 			matrix[M13] = 0; 			matrix[M14] = 0;
		matrix[M21] = 0;			matrix[M22] = scale.getY(); matrix[M23] = 0; 			matrix[M24] = 0; 
		matrix[M31] = 0;			matrix[M32] = 0; 			matrix[M33] = scale.getZ();	matrix[M34] = 0; 
		matrix[M41] = 0;			matrix[M42] = 0; 			matrix[M43] = 0; 			matrix[M44] = 1; 
	}
	
	public static Matrix4 createRotation(Vector3 axis, float angle)
	{
		Matrix4 result = new Matrix4();
		result.setRotation(axis, angle);
		return result;
	}
	
	public void setRotation(Vector3 axis, float angle)
	{
		float x = axis.getX();
		float y = axis.getY();
		float z = axis.getZ();
		
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		
		matrix[M11] = x*x + (1 - x*x)*cos;		matrix[M12] = x*y*(1 - cos) - z*sin; 	matrix[M13] = x*z*(1 - cos) + y*sin; 	matrix[M14] = 0;
		matrix[M21] = x*y*(1 - cos) + z*sin;	matrix[M22] = y*y + (1 - y*y)*cos;		matrix[M23] = y*z*(1 - cos) - x*sin; 	matrix[M24] = 0; 
		matrix[M31] = x*z*(1 - cos) - y*sin;	matrix[M32] = y*z*(1 - cos) + x*sin;	matrix[M33] = z*z + (1 - z*z)*cos;	 	matrix[M34] = 0; 
		matrix[M41] = 0;						matrix[M42] = 0; 						matrix[M43] = 0; 						matrix[M44] = 1; 	
	}
	
	public static Matrix4 createRotation(Vector3 direction, Vector3 up)
	{
		Matrix4 result = new Matrix4();
		result.setRotation(direction, up);
		return result;
	}
	
	public void setRotation(Vector3 direction, Vector3 up)
	{
		//forward vector
		Vector3 forward = new Vector3(direction);
		forward.normalise();
		
		//upward vector
		Vector3 u = new Vector3(up);
		u.normalise();

		//side vector
		Vector3 s = Vector3.cross(forward, u);
		s.normalise();
		
		u = Vector3.cross(s, forward);
		u.normalise();
		
		matrix[M11] = s.getX();			matrix[M12] = s.getY(); 		matrix[M13] = s.getZ();			matrix[M14] = 0;
		matrix[M21] = u.getX();			matrix[M22] = u.getY(); 		matrix[M23] = u.getZ();			matrix[M24] = 0; 
		matrix[M31] = -forward.getX(); 	matrix[M32] = -forward.getY(); 	matrix[M33] = -forward.getZ(); 	matrix[M34] = 0; 
		matrix[M41] = 0;				matrix[M42] = 0; 				matrix[M43] = 0; 				matrix[M44] = 1;		
		
	}
	
	public static Matrix4 createRotation(Quaternion quaternion)
	{
		Matrix4 result = new Matrix4();
		result.setRotation(quaternion);
		return result;
	}
	
	public void setRotation(Quaternion quaternion)
	{
		float a = quaternion.getA();
		float b = quaternion.getI();
		float c = quaternion.getJ();
		float d = quaternion.getK();
		
		matrix[M11] = 1 - 2*c*c - 2*d*d;	matrix[M12] = 2*b*c - 2*d*a; 		matrix[M13] = 2*b*d + 2*c*a;		matrix[M14] = 0;
		matrix[M21] = 2*b*c + 2*d*a;		matrix[M22] = 1 - 2*b*b - 2*d*d; 	matrix[M23] = 2*c*d - 2*b*a;		matrix[M24] = 0; 
		matrix[M31] = 2*b*d - 2*c*a; 		matrix[M32] = 2*c*d + 2*b*a; 		matrix[M33] = 1 - 2*b*b - 2*c*c;	matrix[M34] = 0; 
		matrix[M41] = 0;					matrix[M42] = 0; 					matrix[M43] = 0; 					matrix[M44] = 1;	
	}
		
	public static Matrix4 createPerspective(float fov, float aspectRatio, float zNear, float zFar)
	{
		Matrix4 result = new Matrix4();
		result.setPerspective(fov, aspectRatio, zNear, zFar);
		return result;
	}
	
	public void setPerspective(float fov, float aspectRatio, float zNear, float zFar)
	{
		float f = 1f/(float)Math.tan(fov/2f);
		
		matrix[M11] = f/aspectRatio;	matrix[M12] = 0; 	matrix[M13] = 0; 								matrix[M14] = 0;
		matrix[M21] = 0;				matrix[M22] = f; 	matrix[M23] = 0; 								matrix[M24] = 0; 
		matrix[M31] = 0;				matrix[M32] = 0; 	matrix[M33] = (zFar + zNear)/(zNear - zFar); 	matrix[M34] = (2*zFar*zNear)/(zNear - zFar); 
		matrix[M41] = 0;				matrix[M42] = 0; 	matrix[M43] = -1; 								matrix[M44] = 0; 
	}
	
	public static Matrix4 createLookAt(Vector3 position, Vector3 target, Vector3 up)
	{
		Matrix4 result = new Matrix4();
		result.setLookAt(position, target, up);
		return result;
	}
	
	public void setLookAt(Vector3 position, Vector3 target, Vector3 up)
	{
		//forward vector
		Vector3 f = Vector3.sub(target, position);
		f.normalise();
		
		//upward vector
		Vector3 u = new Vector3(up);
		u.normalise();

		//side vector
		Vector3 s = Vector3.cross(f, u);
		s.normalise();
		
		u = Vector3.cross(s, f);
		u.normalise();
		
		float ipx = -position.getX();
		float ipy = -position.getY();
		float ipz = -position.getZ();
		
		matrix[M11] = s.getX();		matrix[M12] = s.getY(); 	matrix[M13] = s.getZ();		matrix[M14] = ipx*getM11() + ipy*getM12() + ipz*getM13();
		matrix[M21] = u.getX();		matrix[M22] = u.getY(); 	matrix[M23] = u.getZ();		matrix[M24] = ipx*getM21() + ipy*getM22() + ipz*getM23(); 
		matrix[M31] = -f.getX(); 	matrix[M32] = -f.getY(); 	matrix[M33] = -f.getZ(); 	matrix[M34] = ipx*getM31() + ipy*getM32() + ipz*getM33(); 
		matrix[M41] = 0;			matrix[M42] = 0; 			matrix[M43] = 0; 			matrix[M44] = 1;		
		
	}

	public static Matrix4 createOrthographic(float left, float right, float bottom, float top)
	{
		Matrix4 result = new Matrix4();
		result.setOrthographic(left, right, bottom, top);
		return result;
	}
	
	public void setOrthographic(float left, float right, float bottom, float top)
	{
		float near = 0.1f;
		float far = 1000f;
		
		matrix[M11] = 2f/(right - left);	matrix[M12] = 0; 					matrix[M13] = 0; 					matrix[M14] = -(right + left)/(right - left);
		matrix[M21] = 0;					matrix[M22] = 2f/(top - bottom); 	matrix[M23] = 0; 					matrix[M24] = -(top + bottom)/(top - bottom); 
		matrix[M31] = 0;					matrix[M32] = 0; 					matrix[M33] = -2f/(far - near); 	matrix[M34] = -(far + near)/(far - near); 
		matrix[M41] = 0;					matrix[M42] = 0; 					matrix[M43] = 0; 					matrix[M44] = 1; 
	}
	
	public static Matrix4 mul(Matrix4 left, Matrix4 right)
	{
		Matrix4 res = new Matrix4();
		
		for(int row = 0; row < 4; ++row)
		{
			for(int column = 0; column < 4; ++column)
			{
				float value = 0;
				
				for(int i = 0; i < 4; ++i)
				{
					value += left.get(row + 1, i + 1)*right.get(i + 1, column + 1);
				}
				
				res.set(row + 1, column + 1, value);
				
			}
		}
		
		return res;
	}
	
	public static void mul(Matrix4 left, Matrix4 right, Matrix4 result)
	{
		for(int row = 0; row < 4; ++row)
		{
			for(int column = 0; column < 4; ++column)
			{
				float value = 0;
				
				for(int i = 0; i < 4; ++i)
				{
					value += left.get(row + 1, i + 1)*right.get(i + 1, column + 1);
				}
				result.set(row + 1, column + 1, value);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getM11()); sb.append(' '); sb.append(getM12()); sb.append(' '); sb.append(getM13()); sb.append(' '); sb.append(getM14()); sb.append('\n');
		sb.append(getM21()); sb.append(' '); sb.append(getM22()); sb.append(' '); sb.append(getM23()); sb.append(' '); sb.append(getM24()); sb.append('\n');
		sb.append(getM31()); sb.append(' '); sb.append(getM32()); sb.append(' '); sb.append(getM33()); sb.append(' '); sb.append(getM34()); sb.append('\n');
		sb.append(getM41()); sb.append(' '); sb.append(getM42()); sb.append(' '); sb.append(getM43()); sb.append(' '); sb.append(getM44());
		
		return sb.toString();
	}
	
	public float[] get()
	{
		return matrix;
	}
	
	public float get(int row, int column)
	{
		if(row < 1 || row > 4 || column < 1 || column > 4)
			throw new IllegalArgumentException();
		
		int index = (column - 1) * 4 + (row - 1);

		return matrix[index];
	}
	
	public float getM11()
	{
		return matrix[M11];
	}

	public float getM21()
	{
		return matrix[M21];
	}

	public float getM31()
	{
		return matrix[M31];
	}

	public float getM41()
	{
		return matrix[M41];
	}

	public float getM12()
	{
		return matrix[M12];
	}

	public float getM22()
	{
		return matrix[M22];
	}

	public float getM32()
	{
		return matrix[M32];
	}

	public float getM42()
	{
		return matrix[M42];
	}

	public float getM13()
	{
		return matrix[M13];
	}

	public float getM23()
	{
		return matrix[M23];
	}

	public float getM33()
	{
		return matrix[M33];
	}

	public float getM43()
	{
		return matrix[M43];
	}

	public float getM14()
	{
		return matrix[M14];
	}

	public float getM24()
	{
		return matrix[M24];
	}

	public float getM34()
	{
		return matrix[M34];
	}

	public float getM44()
	{
		return matrix[M44];
	}
	
	public void set(Matrix4 other)
	{
		for(int i = 0; i < 16; ++i)
		{
			matrix[i] = other.get()[i];
		}
	}
	
	public void set(int row, int column, float value)
	{
		if(row < 1 || row > 4 || column < 1 || column > 4)
			throw new IllegalArgumentException();
		
		int index = (column - 1) * 4 + (row - 1);

		matrix[index] = value;
	}
	
	public void setM11(float value)
	{
		matrix[M11] = value;
	}

	public void setM21(float value)
	{
		matrix[M21] = value;
	}

	public void setM31(float value)
	{
		matrix[M31] = value;
	}

	public void setM41(float value)
	{
		matrix[M41] = value;
	}

	public void setM12(float value)
	{
		matrix[M12] = value;
	}

	public void setM22(float value)
	{
		matrix[M22] = value;
	}

	public void setM32(float value)
	{
		matrix[M32] = value;
	}

	public void setM42(float value)
	{
		matrix[M42] = value;
	}

	public void setM13(float value)
	{
		matrix[M13] = value;
	}

	public void setM23(float value)
	{
		matrix[M23] = value;
	}

	public void setM33(float value)
	{
		matrix[M33] = value;
	}

	public void setM43(float value)
	{
		matrix[M43] = value;
	}

	public void setM14(float value)
	{
		matrix[M14] = value;
	}

	public void setM24(float value)
	{
		matrix[M24] = value;
	}

	public void setM34(float value)
	{
		matrix[M34] = value;
	}

	public void setM44(float value)
	{
		matrix[M44] = value;
	}
	
}
