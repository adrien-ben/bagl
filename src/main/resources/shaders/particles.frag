#version 330

in GeomOut {
	vec4 color;
	vec2 coords;
} geomOut;

out vec4 color;

uniform sampler2D uTexture;
uniform bool hasTexture;

void main() {
	color = geomOut.color;
	if(hasTexture) {
	    color *= texture2D(uTexture, geomOut.coords);
	}
}
