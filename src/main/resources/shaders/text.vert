#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 vColor;
layout (location = 2) in vec2 vCoords;

out vec4 passColor;
out vec2 passCoords;

uniform mat4 uCamera;

void main() {
	passColor = vColor;
	passCoords = vCoords;
	gl_Position = uCamera*vPosition;
}