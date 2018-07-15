#version 330

in vec2 passCoords;

out vec4 finalColor;

uniform sampler2D texture;

void main() {
    finalColor = texture2D(texture, passCoords);
}
