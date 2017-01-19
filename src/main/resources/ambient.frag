#version 330

in vec2 passCoords;

out vec4 color;

struct BaseLight
{
	vec3 color;
	float intensity;
};

uniform BaseLight uBaseLight;
uniform sampler2D uTexture;

void main()
{
	color = texture2D(uTexture, passCoords)*vec4(uBaseLight.color, 1.0)*uBaseLight.intensity;
}