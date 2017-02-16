#version 330

in vec2 passCoords;
in vec3 passNormal;
in vec4 passPosition;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;
layout (location = 2) out vec4 positions;

uniform sampler2D texture;

void main() {
	colors = texture2D(texture, passCoords);
	normals = vec4(normalize(passNormal)*0.5 + 0.5, 1);
	positions = passPosition;
}