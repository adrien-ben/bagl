#version 330

in vec4 passPosition;
in vec3 passNormal;
in vec2 passCoords;

out vec4 color;

struct BaseLight
{
	vec4 color;
	float intensity;
};

struct DirectionalLight
{
	BaseLight base;
	vec3 direction;
};

struct Material
{
	float specularExponent;
	float specularIntensity;
};

uniform vec3 uEyePosition;
uniform DirectionalLight uLight;
uniform sampler2D uTexture;
uniform Material uMaterial;

vec3 computeLight(BaseLight base, vec3 lightDirection, vec3 eyeToPosition, vec3 normal, Material material)
{
	vec3 result = vec3(0.0);

	lightDirection = normalize(lightDirection);
	normal = normalize(normal);
	
	float diffuseFactor = dot(normal, -lightDirection);
	
	if(diffuseFactor <= 0)
		return result;
		
	result = base.color.xyz*base.intensity*diffuseFactor;
	
	vec3 reflectedLightDirection = normalize(reflect(lightDirection, normal));
	eyeToPosition = normalize(eyeToPosition);
	float specularFactor = dot(reflectedLightDirection, -eyeToPosition);
	
	if(specularFactor <= 0)
		return result;
		
	specularFactor = pow(specularFactor, material.specularExponent)*material.specularIntensity;
	result += base.color.xyz*specularFactor;
	
	return result;
}

void main()
{	
	vec3 lightColor = computeLight(uLight.base, uLight.direction, passPosition.xyz - uEyePosition, passNormal, uMaterial);
	
	//final color
	color = texture2D(uTexture, passCoords)*vec4(lightColor, 1.0);
}