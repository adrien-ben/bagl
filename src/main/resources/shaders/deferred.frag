#version 330

in vec2 passCoords;

out vec4 finalColor;

uniform mat4 viewProj;
uniform sampler2D colors;
uniform sampler2D normals;
uniform sampler2D depth;

vec4 screenSpaceFromDepth(float depthValue, vec2 texCoords) {
	return vec4(texCoords*2 - 1, depthValue, 1);
}

void main() {
	vec3 normal = texture2D(normals, passCoords).xyz;
	if(normal.x == 0 && normal.y == 0 && normal.z == 0) {
		finalColor = vec4(0, 0, 0, 1);
	} else {
		vec3 normal = texture2D(normals, passCoords).xyz*2 - 1;
		vec4 color = texture2D(colors, passCoords);
		float depthValue = texture2D(depth, passCoords).r*2 - 1;
		vec4 ssPosition = screenSpaceFromDepth(depthValue, passCoords);
		vec4 position = inverse(viewProj)*ssPosition;
		finalColor = vec4(position.xyz/position.w, 1);
	} 

}
