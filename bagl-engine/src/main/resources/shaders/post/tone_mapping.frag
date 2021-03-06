#version 330

in vec2 passCoords;

out vec4 finalColor;

uniform sampler2D texture;

float luma(vec3 color) {
    return 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;
}

void main() {
    vec3 color = texture2D(texture, passCoords).rgb;

    //HDR
    color = color/(color + 1.0);

    //Gamma correction
    color = pow(color, vec3(1.0/2.2));

    // luma
    float luma = luma(color);

    finalColor = vec4(color, luma);
}