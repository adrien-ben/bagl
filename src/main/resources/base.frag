#version 330

in vec4 passColor;

out vec4 color;

uniform float uValue;

void main()
{
    color = vec4(passColor.rgb*uValue, 1.0);
}