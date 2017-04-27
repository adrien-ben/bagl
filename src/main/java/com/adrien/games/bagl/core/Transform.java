package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;

/**
 * A transform represents a translation, rotation and scaling in space.
 *
 */
public class Transform {
	
	private Vector3 translation;
	private Quaternion rotation;
	private Vector3 scale;
	private Matrix4 transform;
	private boolean isDirty;
	
	private Matrix4 transBuff;
	private Matrix4 rotBuff;
	private Matrix4 scaleBuff;
	
	public Transform() {
		this.translation = new Vector3();
		this.rotation = new Quaternion(1, 0, 0, 0);
		this.scale = new Vector3(1, 1, 1);
		this.transform = Matrix4.createIdentity();
		this.isDirty = false;
		
		this.transBuff = Matrix4.createZero();
		this.rotBuff = Matrix4.createZero();
		this.scaleBuff = Matrix4.createZero();
	}
	
	public void transform(Transform transform) {		
		final Matrix4 tm = transform.getTransformMatrix();
		this.translation.transform(tm, 1);
		this.scale.transform(tm, 0);
		this.rotation.mul(transform.getRotation());
		this.isDirty = true;
	}
	
	public static void transform(Transform toTransform, Transform transform, Transform result) {
		final Matrix4 tm = transform.getTransformMatrix();
		Vector3.transform(tm, toTransform.translation, 1, result.translation);
		Vector3.mul(toTransform.scale, transform.scale, result.scale);
		Quaternion.mul(toTransform.rotation, transform.rotation, result.rotation);
		result.isDirty = true;
	}
	
	public Matrix4 getTransformMatrix() {
		if(this.isDirty) {
			this.transBuff.setTranslation(this.translation);
			this.rotBuff.setRotation(this.rotation);
			this.scaleBuff.setScale(this.scale);
			
			Matrix4.mul(this.transBuff, this.rotBuff, this.transform);
			Matrix4.mul(this.transform, this.scaleBuff, this.transform);
			
			this.isDirty = false;
		}
		return this.transform;
	}
	
	public Vector3 getTranslation() {
		return translation;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public Vector3 getScale() {
		return scale;
	}

	public void setTranslation(Vector3 translation) {
		this.translation = translation;
		this.isDirty = true;
	}

	public void setRotation(Quaternion rotation) {
		this.rotation = rotation;
		this.isDirty = true;
	}

	public void setScale(Vector3 scale) {
		this.scale = scale;
		this.isDirty = true;
	}
	
}
