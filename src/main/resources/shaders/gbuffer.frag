#version 330

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
	float shininess;
	bool hasSpecularMap;
	sampler2D specularMap;
	float glossiness;
};

const int MAX_GLOSSINESS = 256;

in vec2 passCoords;
in vec3 passNormal;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;

uniform Material uMaterial;

void main() {
	if(uMaterial.hasDiffuseMap) {	
		colors.rgb = texture2D(uMaterial.diffuseMap, passCoords).rgb;
	} else {
		colors.rgb = uMaterial.diffuseColor.rgb;
	}
	normals.rgb = normalize(passNormal)*0.5 + 0.5;
	if(uMaterial.hasSpecularMap) {
		colors.a = texture2D(uMaterial.specularMap, passCoords).r;
	} else {	
		colors.a = uMaterial.shininess;
	}
	normals.a = uMaterial.glossiness/MAX_GLOSSINESS;
}