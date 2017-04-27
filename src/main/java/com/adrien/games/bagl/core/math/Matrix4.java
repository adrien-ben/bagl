package com.adrien.games.bagl.core.math;

/**
 * 4 by 4 matrix.
 *
 */
public final class Matrix4 {
	
	private static final int SIZE = 16;
	private static final int ROW_COUNT = 4;
	private static final int COLUMN_COUNT = 4;
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
	
	/**
	 * Operation buffer.
	 */
	private static final Matrix4 opBuffer = Matrix4.createZero();
	
	private final float[] matrix = new float[SIZE];
	
	/**
	 * Create an identity matrix.
	 */
	public Matrix4() {
		this.setIdentity();
	}
	
	/**
	 * Create a copy of another matrix.
	 * @param other The matrix to copy.
	 */
	public Matrix4(Matrix4 other) {
		for(int i = 0; i < SIZE; ++i) {
			this.matrix[i] = other.matrix[i];
		}
	}
	
	/**
	 * Create a matrix containing only zeros.
	 * @return A new matrix.
	 */
	public static Matrix4 createZero() {
		return new Matrix4().setZero();
	}
	
	/**
	 * Sets all components of the matrix to zero.
	 * @return This for chaining.
	 */
	public Matrix4 setZero() {
		this.matrix[M11] = 0;	this.matrix[M12] = 0; 	this.matrix[M13] = 0; 	this.matrix[M14] = 0;
		this.matrix[M21] = 0;	this.matrix[M22] = 0; 	this.matrix[M23] = 0; 	this.matrix[M24] = 0; 
		this.matrix[M31] = 0;	this.matrix[M32] = 0; 	this.matrix[M33] = 0; 	this.matrix[M34] = 0; 
		this.matrix[M41] = 0;	this.matrix[M42] = 0; 	this.matrix[M43] = 0; 	this.matrix[M44] = 0;
		return this;
	}
	
	/**
	 * Creates an identity matrix.
	 * @return A new matrix.
	 */
	public static Matrix4 createIdentity() {
		return new Matrix4();
	}
	
	/**
	 * Sets this matrix to a identity matrix. 
	 * @return This for chaining.
	 */
	public Matrix4 setIdentity() {
		this.matrix[M11] = 1;	this.matrix[M12] = 0; 	this.matrix[M13] = 0; 	this.matrix[M14] = 0;
		this.matrix[M21] = 0;	this.matrix[M22] = 1; 	this.matrix[M23] = 0; 	this.matrix[M24] = 0; 
		this.matrix[M31] = 0;	this.matrix[M32] = 0; 	this.matrix[M33] = 1; 	this.matrix[M34] = 0; 
		this.matrix[M41] = 0;	this.matrix[M42] = 0; 	this.matrix[M43] = 0; 	this.matrix[M44] = 1;
		return this;
	}
	
	/**
	 * Creates a translation matrix from a translation vector.
	 * @param translation The translation vector.
	 * @return A new matrix.
	 */
	public static Matrix4 createTranslation(Vector3 translation) {
		return new Matrix4().setTranslation(translation);
	}
	
	/**
	 * Changes this matrix into a translation matrix from a translation vector.
	 * @param translation The translation vector.
	 * @return This for chaining.
	 */
	public Matrix4 setTranslation(Vector3 translation) {		
		this.matrix[M11] = 1;	this.matrix[M12] = 0; 	this.matrix[M13] = 0; 	this.matrix[M14] = translation.getX();
		this.matrix[M21] = 0;	this.matrix[M22] = 1; 	this.matrix[M23] = 0; 	this.matrix[M24] = translation.getY(); 
		this.matrix[M31] = 0;	this.matrix[M32] = 0; 	this.matrix[M33] = 1; 	this.matrix[M34] = translation.getZ(); 
		this.matrix[M41] = 0;	this.matrix[M42] = 0; 	this.matrix[M43] = 0; 	this.matrix[M44] = 1;
		return this;
	}
	
	/**
	 * Creates a scale matrix.
	 * @param scale The scale information.
	 * @return A new matrix
	 */
	public static Matrix4 createScale(Vector3 scale) {
		return new Matrix4().setScale(scale);
	}
	
	/**
	 * Changes this matrix into a scale matrix.
	 * @param scale The scale information.
	 * @return This for chaining.
	 */
	public Matrix4 setScale(Vector3 scale) {
		this.matrix[M11] = scale.getX();	this.matrix[M12] = 0; 				this.matrix[M13] = 0; 				this.matrix[M14] = 0;
		this.matrix[M21] = 0;				this.matrix[M22] = scale.getY(); 	this.matrix[M23] = 0; 				this.matrix[M24] = 0; 
		this.matrix[M31] = 0;				this.matrix[M32] = 0; 				this.matrix[M33] = scale.getZ();	this.matrix[M34] = 0; 
		this.matrix[M41] = 0;				this.matrix[M42] = 0; 				this.matrix[M43] = 0; 				this.matrix[M44] = 1;
		return this;
	}
	
	/**
	 * Create a rotation matrix.
	 * @param axis The rotation axis.
	 * @param angle The rotation angle.
	 * @return A new matrix.
	 */
	public static Matrix4 createRotation(Vector3 axis, float angle) {
		return new Matrix4().setRotation(axis, angle);
	}
	
	/**
	 * Changes this matrix into a rotation matrix.
	 * @param axis The rotation axis.
	 * @param angle The rotation angle in radians.
	 * @return This for chaining.
	 */
	public Matrix4 setRotation(Vector3 axis, float angle) {
		final float x = axis.getX();
		final float y = axis.getY();
		final float z = axis.getZ();
		
		final float cos = (float)Math.cos(angle);
		final float sin = (float)Math.sin(angle);
		
		this.matrix[M11] = x*x + (1 - x*x)*cos;		this.matrix[M12] = x*y*(1 - cos) - z*sin; 	this.matrix[M13] = x*z*(1 - cos) + y*sin; 	this.matrix[M14] = 0;
		this.matrix[M21] = x*y*(1 - cos) + z*sin;	this.matrix[M22] = y*y + (1 - y*y)*cos;		this.matrix[M23] = y*z*(1 - cos) - x*sin; 	this.matrix[M24] = 0; 
		this.matrix[M31] = x*z*(1 - cos) - y*sin;	this.matrix[M32] = y*z*(1 - cos) + x*sin;	this.matrix[M33] = z*z + (1 - z*z)*cos;	 	this.matrix[M34] = 0; 
		this.matrix[M41] = 0;						this.matrix[M42] = 0; 						this.matrix[M43] = 0; 						this.matrix[M44] = 1;
		return this;
	}
	
	/**
	 * Creates a rotation matrix from a quaternion.
	 * @param quaternion The quaternion.
	 * @return A new matrix.
	 */
	public static Matrix4 createRotation(Quaternion quaternion) {
		return new Matrix4().setRotation(quaternion);
	}
	
	/**
	 * Changes this matrix into a rotation matrix from a quaternion.
	 * @param quaternion The quaternion.
	 * @return This for chaining.
	 */
	public Matrix4 setRotation(Quaternion quaternion) {
		final float a = quaternion.getA();
		final float b = quaternion.getI();
		final float c = quaternion.getJ();
		final float d = quaternion.getK();
		
		this.matrix[M11] = 1 - 2*c*c - 2*d*d;	this.matrix[M12] = 2*b*c - 2*d*a; 		this.matrix[M13] = 2*b*d + 2*c*a;		this.matrix[M14] = 0;
		this.matrix[M21] = 2*b*c + 2*d*a;		this.matrix[M22] = 1 - 2*b*b - 2*d*d; 	this.matrix[M23] = 2*c*d - 2*b*a;		this.matrix[M24] = 0; 
		this.matrix[M31] = 2*b*d - 2*c*a; 		this.matrix[M32] = 2*c*d + 2*b*a; 		this.matrix[M33] = 1 - 2*b*b - 2*c*c;	this.matrix[M34] = 0; 
		this.matrix[M41] = 0;					this.matrix[M42] = 0; 					this.matrix[M43] = 0; 					this.matrix[M44] = 1;
		return this;
	}
	
	/**
	 * Creates a perspective matrix.
	 * @param fov The field of view is radians.
	 * @param aspectRatio The aspect ratio.
	 * @param zNear The distance of the near plane. Should be at least 1.
	 * @param zFar The distance of the far plane.
	 * @return A new matrix.
	 */
	public static Matrix4 createPerspective(float fov, float aspectRatio, float zNear, float zFar) {
		return new Matrix4().setPerspective(fov, aspectRatio, zNear, zFar);
	}
	
	/**
	 * Changes this matrix into a perspective matrix.
	 * @param fov The field of view is radians.
	 * @param aspectRatio The aspect ratio.
	 * @param zNear The distance of the near plane. Should be at least 1.
	 * @param zFar The distance of the far plane.
	 * @return This for chaining.
	 */
	public Matrix4 setPerspective(float fov, float aspectRatio, float zNear, float zFar) {
		final float f = 1f/(float)Math.tan(fov/2f);
		this.matrix[M11] = f/aspectRatio;	this.matrix[M12] = 0; 	this.matrix[M13] = 0; 								this.matrix[M14] = 0;
		this.matrix[M21] = 0;				this.matrix[M22] = f; 	this.matrix[M23] = 0; 								this.matrix[M24] = 0; 
		this.matrix[M31] = 0;				this.matrix[M32] = 0; 	this.matrix[M33] = (zFar + zNear)/(zNear - zFar); 	this.matrix[M34] = (2*zFar*zNear)/(zNear - zFar); 
		this.matrix[M41] = 0;				this.matrix[M42] = 0; 	this.matrix[M43] = -1; 								this.matrix[M44] = 0;
		return this;
	}
	
	/**
	 * Create a lookat matrix.
	 * @param position The position of the camera.
	 * @param target The target point of the camera.
	 * @param up The upward vector of the camera.
	 * @return A new matrix.
	 */
	public static Matrix4 createLookAt(Vector3 position, Vector3 target, Vector3 up) {
		return new Matrix4().setLookAt(position, target, up);
	}
	
	/**
	 * Changes this matrix into a lookat matrix.
	 * @param position The position of the camera.
	 * @param target The target point of the camera.
	 * @param up The upward vector of the camera.
	 * @return This for chaining.
	 */
	public Matrix4 setLookAt(Vector3 position, Vector3 target, Vector3 up) {
		//forward vector
		final Vector3 f = Vector3.sub(target, position);
		f.normalise();
		
		//upward vector
		Vector3 u = new Vector3(up);
		u.normalise();

		//side vector
		final Vector3 s = Vector3.cross(f, u);
		s.normalise();
		
		u = Vector3.cross(s, f);
		u.normalise();
		
		final float ipx = -position.getX();
		final float ipy = -position.getY();
		final float ipz = -position.getZ();
		
		this.matrix[M11] = s.getX();	this.matrix[M12] = s.getY(); 	this.matrix[M13] = s.getZ();	this.matrix[M14] = ipx*getM11() + ipy*getM12() + ipz*getM13();
		this.matrix[M21] = u.getX();	this.matrix[M22] = u.getY(); 	this.matrix[M23] = u.getZ();	this.matrix[M24] = ipx*getM21() + ipy*getM22() + ipz*getM23(); 
		this.matrix[M31] = -f.getX(); 	this.matrix[M32] = -f.getY(); 	this.matrix[M33] = -f.getZ(); 	this.matrix[M34] = ipx*getM31() + ipy*getM32() + ipz*getM33(); 
		this.matrix[M41] = 0;			this.matrix[M42] = 0; 			this.matrix[M43] = 0; 			this.matrix[M44] = 1;		
		return this;
	}

	/**
	 * Creates an orthographic projection matrix
	 * @param left The left border. 
	 * @param right The right border.
	 * @param bottom The bottom border.
	 * @param top The top border.
	 * @return A new matrix.
	 */
	public static Matrix4 createOrthographic(float left, float right, float bottom, float top) {
		return new Matrix4().setOrthographic(left, right, bottom, top);
	}
	
	/**
	 * Changes this matrix into an orthographic projection matrix. 
	 * @param left The left border. 
	 * @param right The right border.
	 * @param bottom The bottom border.
	 * @param top The top border.
	 * @return This for chaining.
	 */
	public Matrix4 setOrthographic(float left, float right, float bottom, float top) {
		final float near = 0f;
		final float far = 1000f;
		
		this.matrix[M11] = 2f/(right - left);	this.matrix[M12] = 0; 					this.matrix[M13] = 0; 					this.matrix[M14] = -(right + left)/(right - left);
		this.matrix[M21] = 0;					this.matrix[M22] = 2f/(top - bottom); 	this.matrix[M23] = 0; 					this.matrix[M24] = -(top + bottom)/(top - bottom); 
		this.matrix[M31] = 0;					this.matrix[M32] = 0; 					this.matrix[M33] = -2f/(far - near); 	this.matrix[M34] = -(far + near)/(far - near); 
		this.matrix[M41] = 0;					this.matrix[M42] = 0; 					this.matrix[M43] = 0; 					this.matrix[M44] = 1;
		return this;
	}
	
	/**
	 * Removes the translation component of this matrix.
	 * @return a new matrix
	 */
	public Matrix4 removeTranslation() {
		final Matrix4 result = new Matrix4();
		this.removeTranslation(result);
		return result;
	}
	
	/**
	 * Removes the translation component of this matrix and
	 * stores the result in another matrix. This matrix is
	 * not changed.
	 * @param result The matrix where to store the result.
	 */
	public void removeTranslation(Matrix4 result) {
		result.set(this);
		result.setM14(0);
		result.setM24(0);
		result.setM34(0);
		result.setM44(1);
	}
	
	/**
	 * Multiplies two matrices as follows : left*right.
	 * @param left The left matrix. Will not be changed.
	 * @param right The right matrix. Will not be changed.
	 * @return A new matrix containing the result of the product.
	 */
	public static Matrix4 mul(Matrix4 left, Matrix4 right) {
		final Matrix4 res = new Matrix4();
		for(int row = 0; row < ROW_COUNT; ++row) {
			for(int column = 0; column < COLUMN_COUNT; ++column) {
				float value = 0;
				for(int i = 0; i < 4; ++i) {
					value += left.get(row + 1, i + 1)*right.get(i + 1, column + 1);
				}
				res.set(row + 1, column + 1, value);
			}
		}
		return res;
	}
	
	/**
	 * Multiplies two matrices as follows : left*right and stores
	 * the result in a given matrix.
	 * @param left The left matrix. Will not be changed.
	 * @param right The right matrix. Will not be changed.
	 * @param result The matrix where to store the result.
	 */
	public static void mul(Matrix4 left, Matrix4 right, Matrix4 result) {
		for(int row = 0; row < ROW_COUNT; ++row) {
			for(int column = 0; column < COLUMN_COUNT; ++column) {
				float value = 0;
				for(int i = 0; i < 4; ++i) {
					value += left.get(row + 1, i + 1)*right.get(i + 1, column + 1);
				}
				opBuffer.set(row + 1, column + 1, value);
			}
		}
		result.set(opBuffer);
	}
	
	/**
	 * Returns the value of a component of the matrix
	 * @param row The row of the component.
	 * @param column The column of the component.
	 * @return The value of the component.
	 */
	public float get(int row, int column) {
		this.checkBounds(row, column);
		return this.matrix[this.getIndex(row, column)];
	}
	
	/**
	 * Sets the value of a component of the matrix.
	 * @param row The row of the component.
	 * @param column The column of the component.
	 * @param value The value to set.
	 */
	public void set(int row, int column, float value) {
		this.checkBounds(row, column);
		this.matrix[this.getIndex(row, column)] = value;
	}
	
	private void checkBounds(final int row, final int column) {
		if(row < 1 || row > ROW_COUNT || column < 1 || column > COLUMN_COUNT) {			
			throw new IllegalArgumentException("The position '" + row + ", " + column + " is illegal.");
		}
	}
	
	private int getIndex(int row, int column) {
		return (column - 1) * COLUMN_COUNT + (row - 1);
	}
	
	/**
	 * Returns the float buffer of the matrix.
	 * @return The float buffer of the matrix.
	 */
	public float[] get() {
		return this.matrix;
	}
	
	/**
	 * Copy another matrix into this one.
	 * @param other The matrix to copy.
	 */
	public void set(Matrix4 other) {
		for(int i = 0; i < SIZE; ++i) {
			this.matrix[i] = other.matrix[i];
		}
	}

	@Override
	public String toString() {
		return new StringBuilder().append(getM11()).append(' ').append(getM12()).append(' ').append(getM13()).append(' ').append(getM14()).append('\n')
			.append(getM21()).append(' ').append(getM22()).append(' ').append(getM23()).append(' ').append(getM24()).append('\n')
			.append(getM31()).append(' ').append(getM32()).append(' ').append(getM33()).append(' ').append(getM34()).append('\n')
			.append(getM41()).append(' ').append(getM42()).append(' ').append(getM43()).append(' ').append(getM44()).toString();
	}
	
	public float getM11() {
		return matrix[M11];
	}

	public float getM21() {
		return matrix[M21];
	}

	public float getM31() {
		return matrix[M31];
	}

	public float getM41() {
		return matrix[M41];
	}

	public float getM12() {
		return matrix[M12];
	}

	public float getM22() {
		return matrix[M22];
	}

	public float getM32() {
		return matrix[M32];
	}

	public float getM42() {
		return matrix[M42];
	}

	public float getM13() {
		return matrix[M13];
	}

	public float getM23() {
		return matrix[M23];
	}

	public float getM33() {
		return matrix[M33];
	}

	public float getM43() {
		return matrix[M43];
	}

	public float getM14() {
		return matrix[M14];
	}

	public float getM24() {
		return matrix[M24];
	}

	public float getM34() {
		return matrix[M34];
	}

	public float getM44() {
		return matrix[M44];
	}
	
	public void setM11(float value) {
		matrix[M11] = value;
	}

	public void setM21(float value) {
		matrix[M21] = value;
	}

	public void setM31(float value) {
		matrix[M31] = value;
	}

	public void setM41(float value) {
		matrix[M41] = value;
	}

	public void setM12(float value) {
		matrix[M12] = value;
	}

	public void setM22(float value) {
		matrix[M22] = value;
	}

	public void setM32(float value) {
		matrix[M32] = value;
	}

	public void setM42(float value) {
		matrix[M42] = value;
	}

	public void setM13(float value) {
		matrix[M13] = value;
	}

	public void setM23(float value) {
		matrix[M23] = value;
	}

	public void setM33(float value) {
		matrix[M33] = value;
	}

	public void setM43(float value) {
		matrix[M43] = value;
	}

	public void setM14(float value) {
		matrix[M14] = value;
	}

	public void setM24(float value) {
		matrix[M24] = value;
	}

	public void setM34(float value) {
		matrix[M34] = value;
	}

	public void setM44(float value) {
		matrix[M44] = value;
	}
	
}
