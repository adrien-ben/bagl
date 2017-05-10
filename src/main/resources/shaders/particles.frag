#version 330

in GeomOut {
	vec4 color;
} geomOut;

out vec4 color;

void main() {
	color = geomOut.color;
}
