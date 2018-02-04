# baGL

baGL is an OpenGL framework that I use for educational purpose. It uses [LWJGL 3](https://www.lwjgl.org/) and [JOML](https://github.com/JOML-CI/JOML).

## Features

- Deferred Rendering
- Physically Based Rendering. See [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf)
- IBL
- Shadow mapping
- Post processing (bloom, gamma correction and tone mapping)
- Lights (directional lights, point lights and spot lights)
- HDR environment maps (from .hdr equirectangular images)
- Partial [glTF 2.0](https://github.com/KhronosGroup/glTF) support
- 3D Particles using Geometry Shader
- Sprite batching
- Scalable text using Signed Distance Field fonts. See [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf)
- Simple scene graph

## Rendering

This framework uses a deferred renderer. Geometry and material data is first rendered into a frame buffer called the GBuffer (Geometry Buffer).
Then in a second pass the GBuffer is used to perform lighting calculations. The renderer uses physically based rendering with IBL.
The renderer supports directional, point, and spot lights.
The renderer performs a post processing pass to apply bloom on bright spots, gamma correction and tone map from HDR to SDR.

### GBuffer breakdown

The scene data is rendered in three textures :

- Color(RGB) + Roughness(A) : RGBA8
- Normals(RGB) + Metalness(A) : RGBA16
- Depth (32F) : DEPTH_32F
- Emissive (RBG) : RGB16

### PBR

The BRDF used is the same as Epic's Unreal Engine 4 :

- Fresnel : Schlick with Epic's spherical gaussian approximation.
- Distribution : GGX/Trowbridge-Reitz
- Geometry : Schlick/GGX

### Materials

We use the roughness/metallic workflow. The materials are described as follows :

- Diffuse color (Color or Texture)
- Roughness [0.0..1.0]
- Metallic [0.0..1.0]
- ORM (Occlusion/Roughness/Metalness) map (Texture, Optional) 
- Normal map (Texture, Optional)
- Emissive color (Color or Texture)

For ORM maps, roughness must be put in the green channel and metalness in the blue channel.
Ambient occlusion maps are not yet supported but when they are, data will be read from the red channel of the same map.

### Models

Partial support of glTF 2.0 is implemented. For now only static mesh rendering are handle. Sparse accessors are not supported. 

### Lights

The renderer supports directional, point, and spot lights. Some area lights will be implemented (sphere and tube).
The lights attenuation in computed using inverse square function as described in Epic's paper on their PBR implementation.

### Shadows

Basic shadow mapping is implemented. It only works for one directional light (The first found in the scene but it will be improved).

## TODO

- Rendering
    - Animations
    - Sprites in 3D environment (debug icons, ...)
    - Area lights (sphere and tubes)
    - Fixing shadows
    - Some form of anti-aliasing
    - UI (third party ?)
    - Fix emissive map artifacts
- Assets management
- OpenGL state manager
- Composed shaders (#import library.glsl)
- Complete glTF 2.0 support
- An overall review, some refactoring and code cleanup
- And much more... :)

## References

- [Learn OpenGL.com](https://learnopengl.com/)
- [ThinMatrix's](https://www.youtube.com/user/ThinMatrix) youtube channel
- [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf) about their PBR implementation
- [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf) on SDF font rendering
- [OpenGL reference pages](https://www.khronos.org/registry/OpenGL-Refpages/gl4/)
- [Wikipedia](https://www.wikipedia.org/)


