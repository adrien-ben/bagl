#version 330

in vec3 passPosition;

out vec4 finalColor;

uniform samplerCube cubemap;

void main() {
    // apply gamma correction on the skybox texture and boost color
	vec3 color = texture(cubemap, passPosition).rgb;
	finalColor = vec4(pow(color, vec3(2.2))*3, 1.0);
}
