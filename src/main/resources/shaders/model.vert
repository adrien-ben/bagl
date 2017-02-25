#version 330

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vCoords;
layout (location = 3) in vec3 vTangent;

out vec4 passPosition;
out vec3 passNormal;
out vec2 passCoords;
out mat3 passTBN;

struct Matrices {
	mat4 world;
	mat4 wvp;
};

uniform Matrices uMatrices;

void main() {

	vec3 tangent = normalize((uMatrices.world*vec4(vTangent, 0)).xyz);
	vec3 normal = normalize((uMatrices.world*vec4(vNormal, 0.0)).xyz);
	vec3 bitangent = cross(normal, tangent);
	passTBN = mat3(tangent, bitangent, normal);

	passPosition = uMatrices.world*vPosition;
	passNormal = normal;
	passCoords = vCoords;

	gl_Position = uMatrices.wvp*vPosition;
}
