#version 330

in vec2 passCoords;

out vec4 color;

struct BaseLight {
	vec4 color;
	float intensity;
};

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

uniform BaseLight uBaseLight;
uniform Material uMaterial;

void main() {
	vec4 fragColor = uMaterial.hasDiffuseMap ? texture2D(uMaterial.diffuseMap, passCoords) : uMaterial.diffuseColor;
	color = fragColor*uBaseLight.color*uBaseLight.intensity;
}
