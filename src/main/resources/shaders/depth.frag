#version 330

in vec2 passDepth;

out vec4 color;

void main()
{
	float depth = passDepth.x/passDepth.y; 
    color = vec4(depth, depth, depth, 0.0);
}