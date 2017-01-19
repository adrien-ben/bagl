package com.adrien.games.bagl.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class GLSLParser {

	private String version;
	private HashMap<String, GLSLStructure> structures;
	private ArrayList<GLSLAttribute> inAttributes;
	private ArrayList<GLSLAttribute> outAttributes;
	private ArrayList<GLSLAttribute> uniformAttributes;
	private ArrayList<String> uniforms;

	public GLSLParser() {

		this.structures = new HashMap<String, GLSLStructure>();
		this.inAttributes = new ArrayList<GLSLAttribute>();
		this.outAttributes = new ArrayList<GLSLAttribute>();
		this.uniformAttributes = new ArrayList<GLSLAttribute>();
		this.uniforms = new ArrayList<String>();

	}

	public Boolean parse(String glslSource) {

		if(!constructSourceSkeleton(glslSource)) {
			return false;
		}
		generateUniforms();

		return true;
	}

	private Boolean constructSourceSkeleton(String glslSource) {

		StringTokenizer tokenizer = new StringTokenizer(glslSource, " \r\n\t;{");

		while(tokenizer.hasMoreTokens()) {

			String token = tokenizer.nextToken();

			if(token.equals("#version")) {
				version = tokenizer.nextToken();
			} else if(token.equals("in")) {
				inAttributes.add(parseAttribute(tokenizer));
			} else if(token.equals("out")) {
				outAttributes.add(parseAttribute(tokenizer));
			} else if(token.equals("uniform")) {
				uniformAttributes.add(parseAttribute(tokenizer));
			} else if(token.equals("struct")) {
				GLSLStructure structure = parseStructure(tokenizer);
				structures.put(structure.getName(), structure);
			}
		}

		return true;
	}

	private GLSLAttribute parseAttribute(StringTokenizer tokenizer) {

		return new GLSLAttribute(tokenizer.nextToken(), tokenizer.nextToken());
	}

	private GLSLStructure parseStructure(StringTokenizer tokenizer) {

		String name = tokenizer.nextToken();
		ArrayList<GLSLAttribute> attributes = new ArrayList<GLSLAttribute>();
		Boolean hasMoreAttributes = true;

		while(hasMoreAttributes) {
			String attrType = tokenizer.nextToken();

			if(attrType.equals("}")) {
				hasMoreAttributes = false;
			} else {
				String attrName = tokenizer.nextToken();
				attributes.add(new GLSLAttribute(attrType, attrName));
			}
		}

		return new GLSLStructure(name, attributes);
	}

	private void generateUniforms() {

		for(GLSLAttribute uniform : uniformAttributes) {
			GLSLStructure struct = structures.get(uniform.getType());
			String uniformName = uniform.getName();
			if(struct == null) {
				uniforms.add(uniformName);
			} else {
				ArrayList<String> nestedUniforms = generateUniformsFormStructure(uniformName, struct);
				uniforms.addAll(nestedUniforms);
			}
		}
	}

	private ArrayList<String> generateUniformsFormStructure(String uniformName, GLSLStructure structure) {

		ArrayList<String> structUniforms = new ArrayList<String>();

		for(GLSLAttribute attribute : structure.getAttributes()) {
			GLSLStructure struct = structures.get(attribute.getType());
			if(struct == null) {
				structUniforms.add(uniformName + "." + attribute.getName());
			} else { //nested structure
				ArrayList<String> nestedUniforms = generateUniformsFormStructure(attribute.getName(), struct);
				for(String nestedUniform : nestedUniforms) {
					structUniforms.add(uniformName + "." + nestedUniform);
				}
			}
		}

		return structUniforms;
	}

	@Override
	public String toString() {

		StringBuilder strBldr = new StringBuilder();

		if(version != null) {
			strBldr.append("version : ");
			strBldr.append(version);
			strBldr.append("\n\n");	
		}

		strBldr.append("structures : \n");
		for(GLSLStructure struct : structures.values()) {
			strBldr.append("\t");
			strBldr.append(struct.toString());
			strBldr.append("\n");
		}
		strBldr.append("\n");

		strBldr.append("inputs : \n");
		for(GLSLAttribute inAttr : inAttributes) {
			strBldr.append("\t");
			strBldr.append(inAttr.toString());
			strBldr.append("\n");
		}
		strBldr.append("\n");

		strBldr.append("outputs : \n");
		for(GLSLAttribute outAttr : outAttributes) {
			strBldr.append("\t");
			strBldr.append(outAttr.toString());
			strBldr.append("\n");
		}
		strBldr.append("\n");

		strBldr.append("uniforms : \n");
		for(GLSLAttribute uniformAttr : uniformAttributes) {
			strBldr.append("\t");
			strBldr.append(uniformAttr.toString());
			strBldr.append("\n");
		}
		strBldr.append("\n");

		strBldr.append("uniform keys : \n");
		for(String uniform : uniforms) {
			strBldr.append("\t");
			strBldr.append(uniform);
			strBldr.append("\n");
		}

		return strBldr.toString();
	}

	public String getVersion() { return version; }
	public HashMap<String, GLSLStructure> getStructure() { return structures; }
	public ArrayList<GLSLAttribute> getIns() { return inAttributes; }
	public ArrayList<GLSLAttribute> getOuts() { return outAttributes; }
	public ArrayList<GLSLAttribute> getUniformAttributes() { return uniformAttributes; }
	public ArrayList<String> getUniforms() { return uniforms; }

}