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

const float MAX_GLOSSINESS = 512;

in vec2 passCoords;
in vec3 passNormal;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;

uniform Material uMaterial;

void main() {
	colors.rgb = uMaterial.hasDiffuseMap ? texture2D(uMaterial.diffuseMap, passCoords).rgb : uMaterial.diffuseColor.rgb;
	colors.a = uMaterial.hasSpecularMap ? texture2D(uMaterial.specularMap, passCoords).r : uMaterial.shininess;
	normals.rgb = normalize(passNormal)*0.5 + 0.5;
	normals.a = uMaterial.glossiness/MAX_GLOSSINESS;
}