#version 330

struct Camera {
	mat4 vp;
	vec3 position;
};

struct Attenuation {
	float constant;
	float linear;
	float quadratic;
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
	Attenuation attenuation;
};

struct SpotLight {
	PointLight point;
	vec3 direction;
	float cutOff;
	float outerCutOff;
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

float computeAttenuation(float distance, Attenuation attenuation) {
	return 1/(attenuation.constant + attenuation.linear*distance + attenuation.quadratic*distance*distance);
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
	float specular = pow(max(dot(viewDir, refectDir), 0), 32);
	float specularIntensity = 1;
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

	float attenuation = computeAttenuation(distance, light.attenuation);

	return computeDiffuse(light.base, normal, lightDirection)*attenuation
			+ computeSpecular(light.base, position, normal, lightDirection)*attenuation;
}

vec4 computeSpotLight(SpotLight light, vec4 position, vec3 normal) {
	vec3 lightDirection = normalize(position.xyz - light.point.position);

	float theta = dot(-light.direction, -lightDirection);
	if(theta <= light.outerCutOff) {
		return vec4(0, 0, 0, 1);
	}

	float epsilon = light.cutOff - light.outerCutOff;
	float intensity = clamp((theta - light.outerCutOff)/epsilon, 0, 1);
	return computePointLight(light.point, position, normal)*intensity;
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
