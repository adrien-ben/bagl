#version 330

struct Joint {
    mat4 jointMatrix;
};

struct Matrices {
	mat4 world;
	mat4 viewProjection;
};

layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec3 vNormal;
layout (location = 2) in vec2 vCoords;
layout (location = 3) in vec3 vTangent;
layout (location = 4) in ivec4 vJointsIds;
layout (location = 5) in vec4 vJointsWeights;

out vec2 passCoords;
out vec3 passNormal;
out mat3 passTBN;

uniform Matrices uMatrices;
uniform bool uIsSkinned;
uniform Joint uJoints[128];

void main() {

    mat4 world = uMatrices.world;
	if(uIsSkinned) {
        world *= uJoints[vJointsIds.x].jointMatrix * vJointsWeights.x
            + uJoints[vJointsIds.y].jointMatrix * vJointsWeights.y
            + uJoints[vJointsIds.z].jointMatrix * vJointsWeights.z
            + uJoints[vJointsIds.w].jointMatrix * vJointsWeights.w;
	}

	vec3 tangent = normalize(vec3(world*vec4(vTangent, 1.0)));
	vec3 normal = normalize(vec3(world*vec4(vNormal, 0.0)));

	tangent = normalize(tangent - dot(tangent, normal)*normal);

	vec3 bitangent = cross(normal, tangent);
	
	passTBN = mat3(tangent, bitangent, normal); 	
	passCoords = vCoords;
	passNormal = normal;

	gl_Position = uMatrices.viewProjection*world*vPosition;
}