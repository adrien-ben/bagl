#version 330

in vec2 passCoords;

out vec4 finalColor;

uniform sampler2D image;
uniform bool horizontal;
float weight[] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 texOffset = 1.0 / textureSize(image, 0);
    vec3 result = texture2D(image, passCoords).rgb * weight[0];
    if (horizontal) {
        for(int i = 1; i < 5; i++) {
            result += texture2D(image, passCoords + vec2(texOffset.x * i, 0.0)).rgb * weight[i];
            result += texture2D(image, passCoords - vec2(texOffset.x * i, 0.0)).rgb * weight[i];
        }
    } else {
        for(int i = 1; i < 5; i++) {
            result += texture2D(image, passCoords + vec2(0.0, texOffset.y * i)).rgb * weight[i];
            result += texture2D(image, passCoords - vec2(0.0, texOffset.y * i)).rgb * weight[i];
        }
    }
    finalColor = vec4(result, 1.0);
}
