package com.adrien.games.bagl.parser.glsl;

import java.util.ArrayList;

public class GLSLStructure {

	private String name;
	private ArrayList<GLSLAttribute> attributes;

	public GLSLStructure(String name, ArrayList<GLSLAttribute> attributes) {

		if(name == null || attributes == null) {
			throw new NullPointerException();
		}

		this.name = name;
		this.attributes = attributes;
	}

	@Override
	public String toString() {

		StringBuilder strBldr = new StringBuilder();

		strBldr.append(name);
		strBldr.append(" { ");
		for(GLSLAttribute attribute : attributes) {
			strBldr.append(attribute.toString());
		}
		strBldr.append(" }");

		return strBldr.toString();
	}

	public String getName() { 
		return name; 
	}
	
	public ArrayList<GLSLAttribute> getAttributes() { 
		return attributes; 
	}
	
}