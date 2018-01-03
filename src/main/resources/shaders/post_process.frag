#version 330

in vec2 passCoords;

out vec4 finalColor;

uniform sampler2D image;

void main() {

    vec3 color = texture2D(image, passCoords).rgb;

    //HDR
    color = color/(color + 1.0);

    //Gamma correction
    color = pow(color, vec3(1.0/2.2));

    finalColor = vec4(color, 1.0);
}
