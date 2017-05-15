#version 330

in VertOut {
    vec2 coords;
    vec4 color;
} vertOut;

out vec4 color;

uniform sampler2D atlas;

const float width = 0.5;
const float smoothing = 0.05;

void main() {
    float alpha = smoothstep(1 - width - smoothing, 1 - width + smoothing, texture2D(atlas, vertOut.coords).a);
    color = vec4(vertOut.color.rgb, alpha*vertOut.color.a);
}