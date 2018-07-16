#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec4 vColor;

out vec4 passColor;

void main() {
    passColor = vColor;
    gl_Position = vPosition*2 - 1;
}