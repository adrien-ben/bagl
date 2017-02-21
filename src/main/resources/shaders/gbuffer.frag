#version 330

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
};

in vec2 passCoords;
in vec3 passNormal;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;

uniform Material uMaterial;

void main() {
	if(uMaterial.hasDiffuseMap) {	
		colors = texture2D(uMaterial.diffuseMap, passCoords);
	} else {
		colors = uMaterial.diffuseColor;
	}
	normals = vec4(normalize(passNormal)*0.5 + 0.5, 1);
}