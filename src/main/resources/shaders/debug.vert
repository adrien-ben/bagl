#version 330

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vColor;

out vec3 passColor;

uniform mat4 viewProj;

void main() {
	passColor = vColor;
	gl_Position = viewProj*vPosition;
}