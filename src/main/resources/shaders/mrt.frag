#version 330

layout (location = 0) out vec4 color1;
layout (location = 1) out vec4 color2;
layout (location = 2) out vec4 color3;

void main() {
    color1 = vec4(1, 0, 0, 1);
    color2 = vec4(0, 1, 0, 1);
    color3 = vec4(0, 0, 1, 1);
}