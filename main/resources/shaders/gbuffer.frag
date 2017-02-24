#version 330

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
	float shininess;
	bool hasSpecularMap;
	sampler2D specularMap;
	float glossiness;
	bool hasBumpMap;
	sampler2D bumpMap;
};

const float MAX_GLOSSINESS = 512;

in vec2 passCoords;
in vec3 passNormal;
in mat3 passTBN;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;

uniform Material uMaterial;

void main() {
	colors.rgb = uMaterial.hasDiffuseMap ? texture2D(uMaterial.diffuseMap, passCoords).rgb : uMaterial.diffuseColor.rgb;
	colors.a = uMaterial.hasSpecularMap ? texture2D(uMaterial.specularMap, passCoords).r : uMaterial.shininess;
	
	vec3 normal;
	if(uMaterial.hasBumpMap) {
		normal = normalize(texture2D(uMaterial.bumpMap, passCoords).rgb*2 - 1);
		normal = normalize(passTBN*normal);
	} else {
		normal = normalize(passNormal);
	}

	normals.rgb = normal*0.5 + 0.5;
	normals.a = uMaterial.glossiness/MAX_GLOSSINESS;
}