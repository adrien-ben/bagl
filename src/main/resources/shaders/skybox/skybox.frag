#version 330

in vec3 passPosition;

out vec4 finalColor;

uniform sampler2D equirectangularMap;

vec2 invATan = vec2(0.1591, 0.3183);
vec2 sampleShericalMap(vec3 position) {
    return 0.5 + (vec2(atan(position.z, position.x), asin(position.y)) * invATan);
}

// TODO: This is currently used in real time, but the smart thing to do here is to generate a cubemap from
// TODO: this equirectactular map once and then to sample this cubemap which is way faster
void main() {
    vec2 uv = sampleShericalMap(normalize(passPosition));
    vec3 color = texture2D(equirectangularMap, uv).rgb;
	finalColor = vec4(pow(color, vec3(2.2)), 1.0);
}
