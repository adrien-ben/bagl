#version 330

layout(location = 0) in vec2 vPosition;
layout(location = 1) in vec2 vCoords;
layout(location = 2) in vec4 vColor;

out VertOut {
    vec2 coords;
    vec4 color;
} vertOut;

void main() {
    vertOut.coords = vCoords;
    vertOut.color = vColor;
	gl_Position = vec4(vPosition, 0.0, 1.0);
}
