#version 330

in vec4 passColor;
in vec2 passCoords;

out vec4 color;

uniform sampler2D texture;

void main() {
	color = passColor;
	color.a = texture2D(texture, passCoords).a;
}