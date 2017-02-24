package com.adrien.games.bagl.rendering.vertex;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Vector2;
import com.adrien.games.bagl.core.Vector3;

public class VertexPositionColorTexture implements Vertex {

	public static VertexDescription DESCRIPTION = createVertexDescription();
	
	private Vector3 position;
	private Color color;
	private Vector2 coords;
	
	public VertexPositionColorTexture(Vector3 position, Color color, Vector2 coords) {
		this.position = position;
		this.color = color;
		this.coords = coords;
	}

	@Override
	public float[] getData(){
		return new float[]{ 
				position.getX(), position.getY(), position.getZ(),
				color.getRed(), color.getGreen(), color.getBlue(),
				coords.getX(),coords.getY() };
	}
	
	public Vector3 getPosition() {
		return position;
	}

	public Color getColor() {
		return color;
	}

	public Vector2 getCoords() {
		return coords;
	}
	
	private static VertexDescription createVertexDescription() {
		return new VertexDescription(
				new VertexElement[]
						{ new VertexElement(0, 3, 0), new VertexElement(1, 3, 3), new VertexElement(2, 2, 6) });
	}
	
}
