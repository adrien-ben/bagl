#version 330

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
    sampler2D diffuseMap;
	float roughness;
	bool hasRoughnessMap;
	sampler2D roughnessMap;
	float metallic;
	bool hasMetallicMap;
	sampler2D metallicMap;
	bool hasNormalMap;
    sampler2D normalMap;
};

in vec2 passCoords;
in vec3 passNormal;
in mat3 passTBN;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;

uniform Material uMaterial;

void main() {
	colors.rgb = uMaterial.hasDiffuseMap ? texture2D(uMaterial.diffuseMap, passCoords).rgb : uMaterial.diffuseColor.rgb;
    colors.a = uMaterial.hasRoughnessMap ? texture2D(uMaterial.roughnessMap, passCoords).r : uMaterial.roughness;

	vec3 normal;
	if(uMaterial.hasNormalMap) {
		normal = normalize(texture2D(uMaterial.normalMap, passCoords).rgb*2 - 1);
		normal = normalize(passTBN*normal);
	} else {
		normal = normalize(passNormal);
	}

	normals.rgb = normal*0.5 + 0.5;
	normals.a = uMaterial.hasMetallicMap ? texture2D(uMaterial.metallicMap, passCoords).r : uMaterial.metallic;
}