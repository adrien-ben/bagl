#version 330

layout (location = 0) in vec4 vPosition;

uniform mat4 wvp;

void main() {
	gl_Position = wvp*vPosition;
}
