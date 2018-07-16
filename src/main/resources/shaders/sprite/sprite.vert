#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 vColor;
layout (location = 2) in vec2 vCoords;

out vec2 passCoords;
out vec4 passColor;

uniform mat4 uCamera;

void main()
{
	passCoords = vCoords;
	passColor = vColor;
    gl_Position = uCamera*vPosition;
}