#version 330

in vec3 passPosition;

out vec4 finalColor;

uniform samplerCube cubemap;

void main() {
    vec3 color = texture(cubemap, normalize(passPosition)).rgb;
	finalColor = vec4(color, 1.0);
}
