#version 330

struct Camera {
	mat4 vp;
	vec3 position;
};

struct Light {
	float intensity;
	vec4 color;
};

struct DirectionalLight {
	Light base;
	vec3 direction;
};

struct PointLight {
	Light base;
	vec3 position;
	float radius;
};

struct SpotLight {
	PointLight point;
	vec3 direction;
	float angle;
};

in vec2 passCoords;

out vec4 finalColor;

uniform Camera uCamera;
uniform Light uAmbient;
uniform DirectionalLight uDirectional;
uniform PointLight uPoints[1];
uniform SpotLight uSpots[1];

uniform sampler2D colors;
uniform sampler2D normals;
uniform sampler2D depth;

vec4 positionFromDepth(float depth) {
	depth = depth*2 - 1;
	vec4 screenSpace = vec4(passCoords*2 - 1, depth, 1); 
	vec4 position = inverse(uCamera.vp)*screenSpace;
	position.xyz /= position.w;
	return vec4(position.xyz, 1);
}

vec4 computeAmbient(Light light) {
	return vec4(light.color.xyz*light.intensity, 1);
}

vec4 computeDiffuse(Light light, vec3 unitNormal, vec3 unitLightDirection) {
	float diffuse = max(dot(unitNormal, -unitLightDirection), 0);
	return vec4(light.color.xyz*diffuse*light.intensity, 1);
}

vec4 computeSpecular(Light light, vec4 position, vec3 unitNormal, vec3 unitLightDirection) {
	vec3 viewDir = normalize(uCamera.position - position.xyz);
	vec3 refectDir = reflect(unitLightDirection, unitNormal);
	float specular = pow(max(dot(viewDir, refectDir), 0), 64);
	float specularIntensity = 0.5;
	return vec4(light.color.xyz*specular*light.intensity*specularIntensity, 1);
}

vec4 computeDirectional(vec3 normal, vec4 position) {
	vec3 lightDirection = normalize(uDirectional.direction);
	return computeDiffuse(uDirectional.base, normal, lightDirection) + computeSpecular(uDirectional.base, position, normal, lightDirection);
}

vec4 computePointLight(PointLight light, vec4 position, vec3 normal) {
	vec3 lightDirection = position.xyz - light.position;
	float distance = length(lightDirection);
	if(distance > light.radius) {
		return vec4(0, 0, 0, 1);
	}
	lightDirection = normalize(lightDirection);
	float attenuation = (1-distance/light.radius);
	return (computeDiffuse(light.base, normal, lightDirection) + computeSpecular(light.base, position, normal, lightDirection))*attenuation;
}

vec4 computeSpotLight(SpotLight light, vec4 position, vec3 normal) {
	vec3 lightDirection = normalize(position.xyz - light.point.position);
	float angle = dot(-light.direction, -lightDirection);
	float maxAngle = cos(radians(light.angle));
	if(angle <= maxAngle) {
		return vec4(0, 0, 0, 1);
	}
	return computePointLight(light.point, position, normal);
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
		
		vec4 ambient = computeAmbient(uAmbient);
		vec4 directional = computeDirectional(normal, position);
		vec4 point = computePointLight(uPoints[0], position, normal);
		vec4 spot = computeSpotLight(uSpots[0], position, normal);
		
		finalColor = (ambient + directional + point + spot)*color;
	} 
}
