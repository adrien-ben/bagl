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

struct PointLight
{
	BaseLight base;
	vec3 position;
	vec3 attenuation;
	float range;
};

struct SpotLight
{
	PointLight point;
	vec3 direction;
	float cutOff;
};

struct Material
{
	float specularExponent;
	float specularIntensity;
};

uniform vec3 uEyePosition;
uniform SpotLight uLight;
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

vec3 computePoint(PointLight point, vec3 eyePosition, vec3 fragmentPosition, vec3 normal, Material material)
{
	vec3 result = vec3(0.0);
	
	vec3 lightDirection = fragmentPosition - point.position;
	float lightDistance = length(lightDirection);
	
	if(lightDistance > point.range)
		return result;
		
	result = computeLight(point.base, lightDirection, fragmentPosition - eyePosition, normal, material);
	
	float attenuation = point.attenuation.x + 
						lightDistance*point.attenuation.y + 
						lightDistance*lightDistance*point.attenuation.z + 
						0.00001;
					
	result /= attenuation;
	
	return result;
}

vec3 computeSpot(SpotLight spot, vec3 eyePosition, vec3 fragmentPosition, vec3 normal, Material material)
{
	vec3 result = vec3(0.0);
	
	vec3 lightDirection = normalize(fragmentPosition - spot.point.position);
	float spotFactor = dot(-lightDirection, -normalize(spot.direction));
	
	if(spotFactor < spot.cutOff)
		return result;
	
	result = computePoint(spot.point, eyePosition, fragmentPosition, normal, material);
	result *= (1.0 - ((1.0 - spotFactor) / (1.0 - spot.cutOff)));
	
	return result;
}

void main()
{
	vec3 lightColor = computeSpot(uLight, uEyePosition, passPosition.xyz, passNormal, uMaterial);
	
	color = texture2D(uTexture, passCoords)*vec4(lightColor, 1.0);
}