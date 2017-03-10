package com.adrien.games.bagl.core;

import com.adrien.games.bagl.core.math.Matrix4;
import com.adrien.games.bagl.core.math.Quaternion;
import com.adrien.games.bagl.core.math.Vector3;

public class Transform {
	
	private Vector3 position;
	private Quaternion rotation;
	private Vector3 scale;
	
	private Matrix4 translationM;
	private Matrix4 rotationM;
	private Matrix4 scaleM;
	private Matrix4 transformM;
	
	public Transform() {
		position = new Vector3();
		rotation = new Quaternion();
		scale = new Vector3(1, 1, 1);
		
		translationM = Matrix4.createIdentity();
		rotationM = Matrix4.createIdentity();
		scaleM = Matrix4.createIdentity();
		transformM = Matrix4.createIdentity();
	}
	
	public void transform(Transform transform) {
		Matrix4 transformMatrix = transform.getTransformMatrix();
		Vector3.transform(transformMatrix, position, 1, position);
		Quaternion.mul(transform.getRotation(), rotation, rotation);
		Vector3.transform(transformMatrix, scale, 0, scale);
	}
	
	public static void transform(Transform toTransform, Transform transform, Transform result) {
		Matrix4 transformMatrix = transform.getTransformMatrix();
		Vector3.transform(transformMatrix, toTransform.getPosition(), 1, result.getPosition());
		Quaternion.mul(transform.getRotation(), toTransform.getRotation(), result.getRotation());
		Vector3.transform(transformMatrix, toTransform.getScale(), 0, result.getScale());
	}

	public Matrix4 getTransformMatrix() {
		translationM.setTranslation(position);
		rotationM.setRotation(rotation);
		scaleM.setScale(scale);
		
		Matrix4.mul(translationM, rotationM, transformM);
		Matrix4.mul(transformM, scaleM, transformM);

		return transformM;
	}
	
	public Vector3 getPosition() {
		return position;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	public Vector3 getScale() {
		return scale;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}

	public void setRotation(Quaternion rotation) {
		this.rotation = rotation;
	}

	public void setScale(Vector3 scale) {
		this.scale = scale;
	}
	
}
