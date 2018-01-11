#version 330

layout(location = 0) in ivec4 vPosition;
layout(location = 2) in vec2 vCoords;

out vec2 passCoords;

void main() {
	passCoords = vCoords;
	gl_Position = vPosition;
}