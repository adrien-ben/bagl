package com.adrien.games.bagl.parser.model;

public class Face {
	
	private final int positionIndex;
	private final int normalIndex;
	private final int coordsIndex;
	
	public Face(int positionIndex, int normalIndex, int coordsIndex) {
		this.positionIndex = positionIndex;
		this.normalIndex = normalIndex;
		this.coordsIndex = coordsIndex;
	}
	
	public int getPositionIndex() {
		return positionIndex;
	}
	
	public int getNormalIndex() {
		return normalIndex;
	}
	
	public int getCoordsIndex() {
		return coordsIndex;
	}
	
}
