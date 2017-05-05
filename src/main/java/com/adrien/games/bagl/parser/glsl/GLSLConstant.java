package com.adrien.games.bagl.parser.glsl;

public class GLSLConstant {
	
	private final String type;
	private final String name;
	private final int value;
	
	public GLSLConstant(String type, String name, int value) {
		this.type = type;
		this.name = name;
		this.value = value;
	}

	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}

	public int getValue() {
		return value;
	}
	
}
