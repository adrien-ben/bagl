package com.adrien.games.bagl.parser.glsl;

import com.adrien.games.bagl.core.EngineException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GLSLParser {

    private static final Pattern ARRAY_PATTERN = Pattern.compile("^.+\\[.+]$");
    private static final Pattern ARRAY_SIZE_PATTERN = Pattern.compile("^.+\\[(.+)?]$");
    private static final Pattern ARRAY_NAME_PATTERN = Pattern.compile("^(.+)?\\[.+]$");
    private static final String INTEGER_TYPE = "int";

    private final HashMap<String, GLSLStructure> structures;
    private final ArrayList<GLSLAttribute> uniformAttributes;
    private final Map<String, GLSLConstant> constants;

    private final ArrayList<String> uniforms;

    public GLSLParser() {
        this.structures = new HashMap<>();
        this.uniformAttributes = new ArrayList<>();
        this.constants = new HashMap<>();
        this.uniforms = new ArrayList<>();
    }

    public void parse(String glslSource) {
        this.constructSourceSkeleton(glslSource);
        this.generateUniforms();
    }

    private void constructSourceSkeleton(String glslSource) {
        StringTokenizer tokenizer = new StringTokenizer(glslSource, " \r\n\t=;{");
        while(tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            switch (token) {
                case "uniform":
                    uniformAttributes.add(parseAttribute(tokenizer));
                    break;
                case "struct":
                    GLSLStructure structure = parseStructure(tokenizer);
                    structures.put(structure.getName(), structure);
                    break;
                case "const":
                    GLSLConstant constant = this.parseConstant(tokenizer);
                    constants.put(constant.getName(), constant);
                    break;
            }
        }
    }

    private GLSLAttribute parseAttribute(StringTokenizer tokenizer) {
        return new GLSLAttribute(tokenizer.nextToken(), tokenizer.nextToken());
    }

    private GLSLStructure parseStructure(StringTokenizer tokenizer) {
        String name = tokenizer.nextToken();
        ArrayList<GLSLAttribute> attributes = new ArrayList<>();
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
        return new GLSLConstant(type, name, (int)Float.parseFloat(value));
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
        ArrayList<String> structUniforms = new ArrayList<>();

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
            throw new EngineException("The constant '" + name + "' is not defined or is not an integer");
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

        strBldr.append("structures : \n");
        for(GLSLStructure struct : structures.values()) {
            strBldr.append("\t");
            strBldr.append(struct.toString());
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

    public HashMap<String, GLSLStructure> getStructure() {
        return structures;
    }

    public ArrayList<GLSLAttribute> getUniformAttributes() {
        return uniformAttributes;
    }

    public ArrayList<String> getUniforms() {
        return uniforms;
    }

}