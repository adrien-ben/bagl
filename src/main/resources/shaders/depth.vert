#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;

out vec2 passDepth;

struct Matrices
{
	mat4 viewProj;
};

uniform Matrices uMatrices;

void main()
{
	gl_Position = uMatrices.viewProj*vPosition;
	passDepth = gl_Position.zw;
}