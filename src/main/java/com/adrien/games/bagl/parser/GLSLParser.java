package com.adrien.games.bagl.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GLSLParser {

	private static final Pattern ARRAY_PATTERN = Pattern.compile("^.+\\[.+\\]$");
	private static final Pattern ARRAY_SIZE_PATTERN = Pattern.compile("^.+\\[(.+)?\\]$");
	private static final Pattern ARRAY_NAME_PATTERN = Pattern.compile("^(.+)?\\[.+\\]$");
	private static final String INTEGER_TYPE = "int";
	
	private String version;
	private HashMap<String, GLSLStructure> structures;
	private ArrayList<GLSLAttribute> inAttributes;
	private ArrayList<GLSLAttribute> outAttributes;
	private ArrayList<GLSLAttribute> uniformAttributes;
	private Map<String, GLSLConstant> constants;
	
	private ArrayList<String> uniforms;

	public GLSLParser() {
		this.structures = new HashMap<String, GLSLStructure>();
		this.inAttributes = new ArrayList<GLSLAttribute>();
		this.outAttributes = new ArrayList<GLSLAttribute>();
		this.uniformAttributes = new ArrayList<GLSLAttribute>();
		this.constants = new HashMap<>();
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
		StringTokenizer tokenizer = new StringTokenizer(glslSource, " \r\n\t=;{");
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
			} else if("const".equals(token)) {
				GLSLConstant constant = this.parseConstant(tokenizer);
				constants.put(constant.getName(), constant);
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

	private GLSLConstant parseConstant(StringTokenizer tokenizer) {
		String type = tokenizer.nextToken();
		String name = tokenizer.nextToken();
		String value = tokenizer.nextToken();
		return new GLSLConstant(type, name, Integer.parseInt(value));
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
		
		boolean isArray = this.isArray(uniformName);
		int size = isArray ? this.getArraySize(uniformName) : 1;
		if(isArray) {			
			uniformName = this.getArrayName(uniformName);
		}
		
		for(int i = 0; i < size; i++) {
			String finalUniformName = uniformName;
			if(isArray) {
				finalUniformName += "[" + i + "]";
			}
			for(GLSLAttribute attribute : structure.getAttributes()) {
				GLSLStructure struct = structures.get(attribute.getType());
				if(struct == null) {
					structUniforms.add(finalUniformName + "." + attribute.getName());
				} else { //nested structure
					ArrayList<String> nestedUniforms = generateUniformsFormStructure(attribute.getName(), struct);
					for(String nestedUniform : nestedUniforms) {
						structUniforms.add(finalUniformName + "." + nestedUniform);
					}
				}
			}
		}
		return structUniforms;
	}

	private boolean isArray(String uniformName) {
		return ARRAY_PATTERN.matcher(uniformName).matches();
	}
	
	private int getArraySize(String uniformName) {
		Matcher matcher = ARRAY_SIZE_PATTERN.matcher(uniformName);
		matcher.matches();
		String size = matcher.group(1);
		if(size.matches("\\d+")) {
			return Integer.parseInt(matcher.group(1));
		} else {
			return getIntegerConstantValue(size);
		}
	}
	
	private int getIntegerConstantValue(String name) {
		GLSLConstant constant = constants.get(name);
		if(Objects.isNull(constant) || !INTEGER_TYPE.equals(constant.getType())) {
			throw new RuntimeException("The constant '" + name + "' is not defined or is not an integer");
		} else {
			return constant.getValue();
		}
	}
	
	private String getArrayName(String uniformName) {
		Matcher matcher = ARRAY_NAME_PATTERN.matcher(uniformName);
		matcher.matches();
		return matcher.group(1);
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

	public String getVersion() { 
		return version; 
	}
	
	public HashMap<String, GLSLStructure> getStructure() { 
		return structures; 
	}
	
	public ArrayList<GLSLAttribute> getIns() { 
		return inAttributes; 
	}
	
	public ArrayList<GLSLAttribute> getOuts() { 
		return outAttributes; 
	}
	
	public ArrayList<GLSLAttribute> getUniformAttributes() { 
		return uniformAttributes; 
	}
	
	public ArrayList<String> getUniforms() { 
		return uniforms; 
	}

}