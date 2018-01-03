#version 330

in vec3 passPosition;

out vec4 finalColor;

uniform samplerCube cubemap;

void main() {
    // tonemap the sdr skybox to hdr
    // TODO : use actual HDR skyboxes instead
	vec3 color = texture(cubemap, passPosition).rgb;
	finalColor = vec4(pow(color, vec3(2.2)), 1.0);
}
