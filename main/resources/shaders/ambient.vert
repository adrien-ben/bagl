#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vCoords;

out vec2 passCoords;

struct Matrices
{
	mat4 world;
	mat4 wvp;
};

uniform Matrices uMatrices;

void main()
{
	passCoords = vCoords;
	gl_Position = uMatrices.wvp*vPosition;
}