#version 330

#import "classpath:/shaders/common/camera.glsl"

in VertOut {
	vec4 color;
	float size;
} vertOut[];

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out GeomOut {
    vec4 position;
	vec4 color;
	vec2 coords;
} geomOut;

uniform Camera uCamera;

void main() {
	vec3 position = gl_in[0].gl_Position.xyz;
	float size = vertOut[0].size*0.5;

	vec3 side = vec3(uCamera.view[0][0], uCamera.view[1][0], uCamera.view[2][0])*size;
	vec3 up = vec3(uCamera.view[0][1], uCamera.view[1][1], uCamera.view[2][1])*size;

	geomOut.color = vertOut[0].color;

    geomOut.coords = vec2(0, 0);
	geomOut.position = vec4(position - side - up, 1);
	gl_Position = uCamera.viewProj*vec4(position - side - up, 1);
	EmitVertex();
	geomOut.coords = vec2(1, 0);
	geomOut.position = vec4(position + side - up, 1);
	gl_Position = uCamera.viewProj*vec4(position + side - up, 1);
	EmitVertex();
	geomOut.coords = vec2(0, 1);
	geomOut.position = vec4(position - side + up, 1);
	gl_Position = uCamera.viewProj*vec4(position - side + up, 1);
	EmitVertex();
	geomOut.coords = vec2(1, 1);
	geomOut.position = vec4(position + side + up, 1);
	gl_Position = uCamera.viewProj*vec4(position + side + up, 1);
	EmitVertex();

	EndPrimitive();
}
