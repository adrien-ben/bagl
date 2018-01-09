#version 330

in vec3 passPosition;

out vec4 finalColor;

uniform sampler2D equirectangularMap;

vec2 invATan = vec2(0.1591, 0.3183);
vec2 sampleShericalMap(vec3 position) {
    return 0.5 + (vec2(atan(position.z, position.x), asin(position.y)) * invATan);
}

void main() {
    vec2 uv = sampleShericalMap(normalize(passPosition));
    vec3 color = texture2D(equirectangularMap, uv).rgb;
	finalColor = vec4(color, 1.0);
}
