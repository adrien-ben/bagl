#version 330

layout(location = 0) in ivec4 vPosition;

out vec3 passPosition;

uniform mat4 viewProj;

void main() {
	passPosition = vPosition.xyz;
	gl_Position = (viewProj*vPosition).xyww;
}
