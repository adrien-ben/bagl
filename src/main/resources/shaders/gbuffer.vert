#version 330

struct Matrices {
	mat4 world;
	mat4 wvp;
};

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vCoords;

out vec2 passCoords;
out vec3 passNormal;
out vec4 passPosition;

uniform Matrices uMatrices;

void main() {
	passCoords = vCoords;
	passNormal = (uMatrices.world*vec4(vNormal, 0)).xyz;
	passPosition = uMatrices.world*vPosition;
	gl_Position = uMatrices.wvp*vPosition;
}