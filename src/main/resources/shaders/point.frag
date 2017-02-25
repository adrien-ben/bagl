#version 330

in vec4 passPosition;
in vec3 passNormal;
in vec2 passCoords;
in mat3 passTBN;

out vec4 color;

struct BaseLight {
	vec4 color;
	float intensity;
};

struct PointLight {
	BaseLight base;
	vec3 position;
	vec3 attenuation;
	float range;
};

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
	float shininess;
	bool hasSpecularMap;
	sampler2D specularMap;
	float glossiness;
	bool hasBumpMap;
	sampler2D bumpMap;
};

uniform vec3 uEyePosition;
uniform PointLight uLight;
uniform Material uMaterial;

float computeAttenuation(float distance, vec3 attenuation) {
	return 1/(attenuation.x + attenuation.y*distance + attenuation.z*distance*distance);
}

vec4 computeDiffuse(BaseLight light, vec3 unitNormal, vec3 unitLightDirection) {
	float diffuse = max(dot(unitNormal, -unitLightDirection), 0);
	return vec4(light.color.xyz*diffuse*light.intensity, 1);
}

vec4 computeSpecular(BaseLight light, float shininess, float glossiness, vec4 position, vec3 unitNormal, vec3 unitLightDirection) {
	vec3 viewDir = normalize(uEyePosition - position.xyz);
	vec3 halfway = normalize(viewDir - unitLightDirection);
	float specular = pow(max(dot(halfway, unitNormal), 0), glossiness);
	return vec4(light.color.xyz*specular*light.intensity*shininess, 1);
}

void main() {
	vec4 diffuse = vec4(0, 0, 0, 1);
	vec4 specular = vec4(0, 0, 0, 1);
	vec4 fragColor = uMaterial.hasDiffuseMap ? texture2D(uMaterial.diffuseMap, passCoords) : uMaterial.diffuseColor;
	float shininess = uMaterial.hasSpecularMap ? texture2D(uMaterial.specularMap, passCoords).r : uMaterial.shininess;
	
	vec3 lightDirection = passPosition.xyz - uLight.position;
	float distance = length(lightDirection);
	if(distance <= uLight.range) {
		vec3 normal;
		if(uMaterial.hasBumpMap) {
			normal = normalize(texture2D(uMaterial.bumpMap, passCoords).rgb*2 - 1);
			normal = normalize(passTBN*normal);
		} else {
			normal = normalize(passNormal);
		}
		lightDirection = normalize(lightDirection);
		float attenuation = computeAttenuation(distance, uLight.attenuation);
		diffuse += computeDiffuse(uLight.base, normal, lightDirection)*attenuation;
		specular += computeSpecular(uLight.base, shininess, uMaterial.glossiness, passPosition, normal, lightDirection)*attenuation;
	}

	color = fragColor*diffuse + specular;
}
