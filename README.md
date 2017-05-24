# bagl

baGL is an OpengGL framework that I use for educational purpose. It uses [LWJGL 3](https://www.lwjgl.org/).

## Features

- Deferred Rendering
- Physically Based Rendering. See [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf)
- Shadow mapping
- Simple .obj and .mtl loaders
- Lights (ambient lights, directional lights, point lights and spot lights)
- Skyboxes
- 3D Particles using Geometry Shader
- Spritebatching
- Scalable text using Signed Distance Field fonts. See [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf)
- Simple scene graph

## Rendering

This framework uses a deferred renderer. Geometry and material data is first rendered into a framebuffer called the Guffer.
Then in a second pass the gbuffer is used to perform lighting calculations. The renderer uses physically based rendering.
The renderder supports ambient, directional, point, and spot lights.

### Gbuffer breakdown

The scene data is rendered in two RGBA8 textures and a 32F texture:

- Color(RGB) + Roughness(A)
- Normals(RGB) + Metallness(A)
- Depth (32F)

### PBR

The BRDF used is the same as Epic's Unreal Engine 4. Image Based Lighting is not yet implemented.

- Fresnel : Schlick with Epic's spherical gaussian approximation.
- Distribution : GGX/Trowbridge-Reitz
- Geometry : Schlick/GGX

### Materials

We use the roughness/metallic workflow. The materials are descibed as follows :

- Diffuse color (Color or Texture)
- Roughness ([0.0..1.0] or Texture)
- Metallic ([0.0..1.0] or Texture)
- Normal map (Texture, Optional)

### Lights

The renderder supports ambient, directional, point, and spot lights. Some area lights will be implemented (sphere and tube).
The lights attenuation in computed using Epic's inverse square function.

### Shadows

Basic shadow mapping is implemented. It only works for one directional light (The first found in the scene but it will be improved).

## TODO

- Improving scene graph
    - Game Objects with components(Mesh, Light, ...)
- Rendering
    - Animations
    - Sprites in 3D environment (particles, debug icons, ...)
    - IBL
    - Area lights (sphere and tubes)
- Review the Model/Mesh/Material model.
- 2D scene
- Assets managment
- OpenGL state manager
- And much more... :)

## References

- [Learn OpenGL.com](https://learnopengl.com/).
- [ThinMatrix's](https://www.youtube.com/user/ThinMatrix) youtube channel.
- [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf) about their PBR implementation.
- [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf) on SDF font rendering.
- [OpenGL reference pages](https://www.khronos.org/registry/OpenGL-Refpages/gl4/).
- [Wikipedia](https://www.wikipedia.org/).


