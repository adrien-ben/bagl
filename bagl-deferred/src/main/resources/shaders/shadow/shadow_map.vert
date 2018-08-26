#version 330

struct Joint {
    mat4 jointMatrix;
};

struct Matrices {
	mat4 world;
	mat4 viewProjection;
};

layout (location = 0) in vec4 vPosition;
layout (location = 2) in vec2 vCoords;
layout (location = 4) in ivec4 vJointsIds;
layout (location = 5) in vec4 vJointsWeights;

out vec2 passCoords;

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

    passCoords = vCoords;
	gl_Position = uMatrices.viewProjection*world*vPosition;
}
