#version 330

in GeomOut {
	vec3 color;
} geomOut;

out vec4 color;

void main() {
	color = vec4(geomOut.color.rgb, 1);
}
