# baGL

baGL is an OpenGL framework that I use for educational purpose. It uses [LWJGL 3](https://www.lwjgl.org/) and [JOML](https://github.com/JOML-CI/JOML).

![Screenshot](images/screenshot_720p.bmp "Screenshot")

## Features

- Deferred Rendering
- Physically Based Rendering. See [Epic's paper](http://blog.selfshadow.com/publications/s2013-shading-course/karis/s2013_pbs_epic_notes_v2.pdf)
- IBL
- Shadow mapping
- Post processing (bloom, gamma correction and tone mapping, anti aliasing)
- Lights (directional lights, point lights and spot lights)
- HDR environment maps (from .hdr equirectangular images)
- Partial [glTF 2.0](https://github.com/KhronosGroup/glTF) support
- 3D Particles using Geometry Shader
- Sprite batching
- Scalable text using Signed Distance Field fonts. See [Valve's paper](http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf)
- Simple scene graph
- Shader #import directive

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

## Files

### ResourcePath

baGl provides a common interface to access regular files and resource path with the `ResourcePath` class.

```java
class Example {
    public static void main() {
        // Create a ResourcePath to a file
        var path = ResourcePath.get("/path_to_file/file.ext");
        // Create a ResourcePath to a resource
        var path = ResourcePath.get("classpath:/file.ext");
        // Then you can access the content with 
        var inputStream = path.openInputStream();
    }
}
```

### Shader files

Shader file are regular GLSL file except you can reference other files by using the #import directive.

```glsl
#version 330

#import "/shaders/common/maths.glsl"
#import "classpath:/library.glsl"

void main() {
}
```

You need to specify the absolute path of the referenced file for now. And you can use the `classpath:` prefix to 
reference resource files. The shader parser will take care of not including the same files several times.

## TODO

- Rendering
    - Animations
    - Sprites in 3D environment (debug icons, ...)
    - Area lights (sphere and tubes)
    - Fixing shadows
    - UI (third party ?)
- Assets management
- OpenGL state manager
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
- [NVidia's FXAA's paper](https://developer.download.nvidia.com/assets/gamedev/files/sdk/11/FXAA_WhitePaper.pdf) and [FXAA 3.11 source code](https://gist.github.com/kosua20/0c506b81b3812ac900048059d2383126)
