#version 330

in vec4 passPosition;
in vec3 passNormal;
in vec2 passCoords;

out vec4 color;

struct BaseLight {
	vec4 color;
	float intensity;
};

struct DirectionalLight {
	BaseLight base;
	vec3 direction;
};

struct Material {
	vec4 diffuseColor;
	bool hasDiffuseMap;
	sampler2D diffuseMap;
	float shininess;
	bool hasSpecularMap;
	sampler2D specularMap;
	float glossiness;
};

uniform vec3 uEyePosition;
uniform DirectionalLight uLight;
uniform Material uMaterial;

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
	vec3 normal = normalize(passNormal);
	vec4 fragColor = uMaterial.hasDiffuseMap ? texture2D(uMaterial.diffuseMap, passCoords) : uMaterial.diffuseColor;

	//directional lights
	vec3 lightDirection = normalize(uLight.direction);
	vec4 diffuse = computeDiffuse(uLight.base, normal, lightDirection);
	vec4 specular = computeSpecular(uLight.base, uMaterial.shininess, uMaterial.glossiness, passPosition, normal, lightDirection);
	
	//final color
	color = fragColor*diffuse + specular;
}