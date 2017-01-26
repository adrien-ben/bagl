#version 330

in vec4 passPosition;
in vec3 passNormal;
in vec2 passCoords;

out vec4 color;

uniform sampler2D uTexture;

void main()
{
    color = vec4(0.5f * (normalize(passNormal) + 1.0f), 0.0);
}