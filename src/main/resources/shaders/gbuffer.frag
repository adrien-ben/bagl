#version 330

in vec2 passCoords;
in vec3 passNormal;

layout (location = 0) out vec4 colors;
layout (location = 1) out vec4 normals;

uniform sampler2D texture;

void main() {
	colors = texture2D(texture, passCoords);
	normals = vec4(normalize(passNormal)*0.5 + 0.5, 1);
}