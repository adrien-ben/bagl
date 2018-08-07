#version 330

in vec2 passCoords;

out vec4 brightColor;

uniform sampler2D image;

void main() {
    brightColor = vec4(0.2*texture2D(image, passCoords).rgb, 1.0);
}
