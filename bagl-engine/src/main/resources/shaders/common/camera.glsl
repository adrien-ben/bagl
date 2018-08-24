struct Camera {
	mat4 viewProj;
	mat4 view;
	mat4 invertedViewProj;
	vec3 position;
	float zNear;
	float zFar;
};