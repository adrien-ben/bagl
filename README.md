# baGL

baGL is an OpenGL framework that I use for educational purpose. It uses [LWJGL 3](https://www.lwjgl.org/).

## Features

- Deferred Rendering
- Physically Based Rendering. See [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf)
- Shadow mapping
- Basic post processing (bloom, gamma correction and tone mapping)
- Simple .obj and .mtl loaders
- Lights (ambient lights, directional lights, point lights and spot lights)
- HDR environment maps (from .hdr equirectangular images)
- IBL (diffuse irradiance only)
- 3D Particles using Geometry Shader
- Sprite batching
- Scalable text using Signed Distance Field fonts. See [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf)
- Simple scene graph

## Rendering

This framework uses a deferred renderer. Geometry and material data is first rendered into a frame buffer called the GBuffer (Geometry Buffer).
Then in a second pass the GBuffer is used to perform lighting calculations. The renderer uses physically based rendering.
Image Based Lighting is partially implemented (diffuse irradiance only)
The renderer supports ambient, directional, point, and spot lights.
The renderer performs a small post processing pass to apply bloom on bright spots, gamma correction and tone map from HDR to SDR.

### GBuffer breakdown

The scene data is rendered in three textures :

- Color(RGB) + Roughness(A) : RGBA8
- Normals(RGB) + Metalness(A) : RGBA16
- Depth (32F) : DEPTH_32F

### PBR

The BRDF used is the same as Epic's Unreal Engine 4. Image Based Lighting is not yet implemented.

- Fresnel : Schlick with Epic's spherical gaussian approximation.
- Distribution : GGX/Trowbridge-Reitz
- Geometry : Schlick/GGX

### Materials

We use the roughness/metallic workflow. The materials are described as follows :

- Diffuse color (Color or Texture)
- Roughness ([0.0..1.0] or Texture)
- Metallic ([0.0..1.0] or Texture)
- Normal map (Texture, Optional)

### Lights

The renderer supports ambient, directional, point, and spot lights. Some area lights will be implemented (sphere and tube).
The lights attenuation in computed using inverse square function as described in Epic's paper on their PBR implementation.

### Shadows

Basic shadow mapping is implemented. It only works for one directional light (The first found in the scene but it will be improved).

## TODO

- Improving scene graph
    - Game Objects with components(Mesh, Light, ...)
- Rendering
    - Animations
    - Sprites in 3D environment (debug icons, ...)
    - IBL (specular)
    - Area lights (sphere and tubes)
    - Improved shadows
- Review the Model/Mesh/Material model.
- 2D scene
- Assets management
- Replace the custom obj loader 
- OpenGL state manager
- Composed shaders (#import library.glsl)
- An overall review, some refactoring and code cleanup
- And much more... :)

## References

- [Learn OpenGL.com](https://learnopengl.com/).
- [ThinMatrix's](https://www.youtube.com/user/ThinMatrix) youtube channel.
- [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf) about their PBR implementation.
- [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf) on SDF font rendering.
- [OpenGL reference pages](https://www.khronos.org/registry/OpenGL-Refpages/gl4/).
- [Wikipedia](https://www.wikipedia.org/).


