package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Vector3;

public class VertexPosition implements Vertex {

	public static VertexDescription DESCRIPTION = createVertexDescription();

	private Vector3 position;

	public VertexPosition(Vector3 position) {
		this.position = position;
	}

	@Override
	public float[] getData() {
		return new float[] { position.getX(), position.getY(), position.getZ() };
	}

	public Vector3 getPosition() {
		return position;
	}

	private static VertexDescription createVertexDescription() {
		return new VertexDescription(
				new VertexElement[]
						{ new VertexElement(0, 3, 0) });
	}
	
}
