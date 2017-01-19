package com.adrien.games.bagl.core;

public class Camera
{
	private Vector3 position;
	private Vector3 direction;
	private Vector3 up;

	private float fov;
	private float aspectRatio;
	private float zNear;
	private float zFar;
	
	private Matrix4 projection;
	private Matrix4 view;
	private Matrix4 viewProj;
	
	private boolean dirtyProj;
	private boolean dirtyView;
	
	public Camera(Vector3 position, Vector3 direction, Vector3 up, float fovRads, float aspectRatio, float zNear, float zFar)
	{
		this.position = position;
		this.direction = direction;
		this.up = up;
		
		this.fov = fovRads;
		this.aspectRatio = aspectRatio;
		this.zNear = zNear;
		this.zFar = zFar;
		
		this.projection = Matrix4.createPerspective(fov, aspectRatio, zNear, zFar);
		this.view = Matrix4.createLookAt(position, Vector3.add(position, direction), up);
		this.viewProj = Matrix4.mul(projection, view);
		
		this.dirtyProj = false;
		this.dirtyView = false;
	}

	public Vector3 getPosition()
	{
		return position;
	}

	public Vector3 getDirection()
	{
		return direction;
	}

	public Vector3 getUp()
	{
		return up;
	}

	public Matrix4 getProjection()
	{
		if(dirtyProj)
		{
			projection.setPerspective(fov, aspectRatio, zNear, zFar);
			dirtyProj = false;
		}
		return projection;
	}

	public Matrix4 getView()
	{
		if(dirtyView)
		{
			view.setLookAt(position, Vector3.add(position, direction), up);
			dirtyView = false;
		}
		return view;
	}

	public Matrix4 getViewProj()
	{
		if(dirtyProj || dirtyView)
		{
			Matrix4.mul(getProjection(), getView(), viewProj);
		}
		return viewProj;
	}

	public void setPosition(Vector3 position)
	{
		this.position = position;
		dirtyView = true;
	}

	public void setDirection(Vector3 direction)
	{
		this.direction = direction;
		dirtyView = true;
	}

	public void setUp(Vector3 up)
	{
		this.up = up;
		dirtyView = true;
	}
	
	
	
}
