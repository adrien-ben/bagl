# baGL - Deferred Scene Renderer

This module contains a scene renderer that implements Physically Based Rendering with IBL.

## Rendering

This framework uses a deferred renderer. Geometry and material data is first rendered into a frame buffer called the GBuffer (Geometry Buffer).
Then in a second pass the GBuffer is used to perform lighting calculations. The renderer uses physically based rendering with IBL.
The renderer supports directional, point, and spot lights.
The renderer performs a post processing pass to apply bloom on bright spots, gamma correction, tone map from HDR to SDR and apply fxaa.

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
- Double sided flag

For ORM maps, roughness must be put in the green channel and metalness in the blue channel.
Ambient occlusion maps are not yet supported but when they are, data will be read from the red channel of the same map.
For double sided materials, normals will be automatically inverted during the fragment shader stage.

### Models

Partial support of glTF 2.0 is implemented. For now only static mesh rendering are handle. Sparse accessors are not supported.

Model are represented by a graph. The root (represented by the Model class) can contains several nodes (represented by the 
ModelNode class). Each node can have its own children. A node contains transform data and can contain one or more meshes.

> Normals and tangents are not computed after load. So make sur that models you import already contain those. Models without
> normals won't be lit and models using normal mapping but whose tangent are not provided will be either not lit or incorrectly lit.

### Lights

The renderer supports directional, point, and spot lights. Some area lights will be implemented (sphere and tube).
The lights attenuation in computed using inverse square function as described in Epic's paper on their PBR implementation.

### Shadows

Basic shadow mapping is implemented. It only works for one directional light (The first found in the scene but it will be improved).

### Post Processing

The renderer uses a simple post processing pipeline that apply bloom, anti-aliasing, tone mapping and gamma correction.

#### Anti Aliasing

The algorithm used for anti aliasing is the Fast Approximate Anti Aliasing (FXAA) in its version 3.11. 

I use nvidia's [FXAA 3.11 source code](https://gist.github.com/kosua20/0c506b81b3812ac900048059d2383126) from which I removed all the thing I do not need
(console and DX implementations for example).