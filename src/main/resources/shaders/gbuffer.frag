#version 330

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
	float specularIntensity;
};

in vec2 passCoords;
in vec3 passNormal;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;
layout (location = 2) out vec4 shininess;

uniform Material uMaterial;

void main() {
	if(uMaterial.hasDiffuseMap) {	
		colors = texture2D(uMaterial.diffuseMap, passCoords);
	} else {
		colors = uMaterial.diffuseColor;
	}
	normals = vec4(normalize(passNormal)*0.5 + 0.5, 1);
	shininess = vec4(uMaterial.specularIntensity, uMaterial.specularIntensity, uMaterial.specularIntensity, 1);
}