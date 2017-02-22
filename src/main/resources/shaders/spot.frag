#version 330

in vec4 passPosition;
in vec3 passNormal;
in vec2 passCoords;

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

struct SpotLight {
	PointLight point;
	vec3 direction;
	float cutOff;
	float outerCutOff;
};

struct Material {
	float specularExponent;
	float specularIntensity;
};

uniform vec3 uEyePosition;
uniform SpotLight uLight;
uniform sampler2D uTexture;
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

void main()
{
	vec4 diffuse = vec4(0, 0, 0, 1);
	vec4 specular = vec4(0, 0, 0, 1);

	vec3 lightDirection = passPosition.xyz - uLight.point.position;
	float distance = length(lightDirection);
	lightDirection = normalize(lightDirection);
	float theta = dot(-uLight.direction, -lightDirection);
	if(theta > uLight.outerCutOff && distance <= uLight.point.range) {
		vec3 normal = normalize(passNormal);
		float epsilon = uLight.cutOff - uLight.outerCutOff;
		float intensity = clamp((theta - uLight.outerCutOff)/epsilon, 0, 1);
		float attenuation = computeAttenuation(distance, uLight.point.attenuation);
		diffuse += computeDiffuse(uLight.point.base, normal, lightDirection)*attenuation*intensity;
		specular += computeSpecular(uLight.point.base, uMaterial.specularIntensity, uMaterial.specularExponent, passPosition, normal, lightDirection)*attenuation*intensity;
	}
	
	color = texture2D(uTexture, passCoords)*diffuse + specular;
}