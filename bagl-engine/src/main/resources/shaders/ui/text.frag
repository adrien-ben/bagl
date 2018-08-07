#version 330

in VertOut {
    vec2 coords;
    vec4 color;
} vertOut;

out vec4 color;

uniform sampler2D atlas;
uniform float thickness;
uniform float smoothing;

void main() {
    float alpha = smoothstep(1 - thickness - smoothing, 1 - thickness + smoothing, texture2D(atlas, vertOut.coords).a);
    color = vec4(vertOut.color.rgb, alpha*vertOut.color.a);
}