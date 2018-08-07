#version 330

in vec2 passCoords;

out vec4 finalColor;

uniform sampler2D image;
uniform sampler2D bloom;

void main() {
    vec3 color = texture2D(image, passCoords).rgb + texture2D(bloom, passCoords).rgb;
    finalColor = vec4(color, 1.0);
}
