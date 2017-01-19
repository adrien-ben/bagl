#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vCoords;

out vec2 passCoords;

struct Matrices
{
	mat4 model;
	mat4 mvp;
};

uniform Matrices uMatrices;

void main()
{
	passCoords = vCoords;

	gl_Position = uMatrices.mvp*vPosition;
}