#version 330

struct Matrices {
	mat4 world;
	mat4 wvp;
};

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vCoords;
layout (location = 3) in vec3 vTangent;

out vec2 passCoords;
out vec3 passNormal;
out mat3 passTBN;

uniform Matrices uMatrices;

void main() {
	vec3 tangent = normalize(vec3(uMatrices.world*vec4(vTangent, 0)));
	vec3 normal = normalize(vec3(uMatrices.world*vec4(vNormal, 0)));
	vec3 bitangent = cross(normal, tangent);
	
	passTBN = mat3(tangent, bitangent, normal); 	
	passCoords = vCoords;
	passNormal = normal;
	
	gl_Position = uMatrices.wvp*vPosition;
}