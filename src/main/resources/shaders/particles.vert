#version 330

layout(location = 0) in vec3 vPosition;
layout(location = 1) in vec4 vColor;
layout(location = 2) in float vSize;

out VertOut {
	vec4 color;
	float size;
} vertOut;

void main() {
	gl_Position = vec4(vPosition, 1);
	vertOut.color = vColor;
	vertOut.size = vSize;
}
