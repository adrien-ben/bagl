#version 330

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
	float specularIntensity;
	bool hasSpecularMap;
	sampler2D specularMap;
};

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
	normals = vec4(normalize(passNormal)*0.5 + 0.5, 1);
	if(uMaterial.hasSpecularMap) {
		colors.a = texture2D(uMaterial.specularMap, passCoords).r;
	} else {	
		colors.a = uMaterial.specularIntensity;
	}
}