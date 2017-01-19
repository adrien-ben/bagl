#version 330

in vec2 passCoords;

out vec4 color;

uniform sampler2D uTexture;

void main()
{
    color = texture2D(uTexture, passCoords);
}