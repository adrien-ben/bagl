#version 330

struct Light {
	float intensity;
	vec4 color;
};

struct PointLight {
	Light base;
	vec3 position;
	float radius;
};

in vec2 passCoords;

out vec4 finalColor;

uniform mat4 viewProj;
uniform Light uAmbient;
uniform PointLight uPoints[1];

uniform sampler2D colors;
uniform sampler2D normals;
uniform sampler2D depth;

vec4 positionFromDepth(float depth) {
	depth = depth*2 - 1;
	vec4 screenSpace = vec4(passCoords*2 - 1, depth, 1); 
	vec4 position = inverse(viewProj)*screenSpace;
	position.xyz /= position.w;
	return vec4(position.xyz, 1);
}

vec4 computeAmbient(Light light) {
	return vec4(light.color.xyz*light.intensity, 1);
}

vec4 computePointLight(PointLight light, vec4 position, vec3 normal) {
	vec3 toLight = light.position - position.xyz;
	float distance = length(toLight);
	float diffuse = dot(normal, normalize(toLight));
	if(distance > light.radius || diffuse < 0) {
		return vec4(0, 0, 0, 1);
	}
	return vec4(light.base.color.xyz*diffuse*light.base.intensity, 1);
}

void main() {
	vec3 normal = texture2D(normals, passCoords).xyz;
	if(normal.x == 0 && normal.y == 0 && normal.z == 0) {
		finalColor = vec4(0, 0, 0, 1);
	} else {
		vec3 normal = texture2D(normals, passCoords).xyz*2 - 1;
		vec4 color = texture2D(colors, passCoords);
		float depthValue = texture2D(depth, passCoords).r;
		vec4 position = positionFromDepth(depthValue); 
		
		vec4 ambient = color*computeAmbient(uAmbient);
		vec4 point = color*computePointLight(uPoints[0], position, normal);
		
		finalColor = ambient + point;
	} 
}
