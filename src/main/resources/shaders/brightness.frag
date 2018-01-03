#version 330

in vec2 passCoords;

layout (location = 0) out vec4 finalColor;
layout (location = 1) out vec4 brightColor;

uniform sampler2D image;

void main() {
    vec3 color = texture2D(image, passCoords).rgb;
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));

    if (brightness > 1.0) {
        brightColor = vec4(color, 1.0);
    } else {
        brightColor = vec4(0.0, 0.0, 0.0, 1.0);
    }

    finalColor = vec4(color, 1.0);
}
