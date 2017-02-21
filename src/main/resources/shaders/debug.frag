#version 330

in vec3 passColor;

out vec4 color;

void main() {
	color = vec4(passColor, 1);
}