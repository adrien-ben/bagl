#version 330

#import "classpath:/shaders/common/material.glsl"

in vec2 passCoords;

out vec4 color;

uniform Material uMaterial;

void main() {
    // discard transparent pixels
    if(isTransparent(uMaterial, passCoords)) {
        discard;
    }

	color = vec4(1, 1, 1, 1);
}
