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

- Diffuse color (Color or Texture2D)
- Roughness [0.0..1.0]
- Metallic [0.0..1.0]
- Roughness/Metallic map (Texture2D, Optional) 
- Normal map (Texture2D, Optional)
- Emissive color (Color or Texture2D)
- Double sided flag

For Roughness/Metallic maps, roughness must be put in the green channel and metalness in the blue channel.
For double sided materials, normals will be automatically inverted during the fragment shader stage.

#### Transparency

Transparency of materials is partially supported. Materials with fully opaque and transparent parts are supported.
Blending is NOT supported.

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

Cascaded Shadow Mapping is implemented for directional lights. It only works for one directional light (The first found in the scene but it will be improved).

#### Frustum split

Before spitting the view frustum it will be clipped so its depth is equal to the value set for the `shadow_max_distance` parameter. If the value is greater than the current
view frustum depth then the parameter has no effect.

The method used to compute the split values is the one described in [CPU Gems3](https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html). It mixes a logarithmic
and uniform distribution for the split values. The distribution between the logarithmic and uniform terms can be controlled via the `shadow_cascade_split_lambda` parameter.
0.0 means fully uniform and 1.0 means fully logarithmic.

#### View-Projection matrix

The view-projection matrix for each cascade is computed by computing the sphere around each cascade frustum. I use the sphere because it is rotation invariant which means 
the size of the shadow map will remains constant.

Once the sphere is computed, at 'lookAt' matrix is computed as if positioned at the edge of the sphere and looking toward the center of that sphere. Then an orthographic 
projection is created to match the sphere bounds but with a near plane placed at 0 and the far place at the diameter of the sphere.

Finally the resulting matrix is offset so it is aligned with world space texels. This is done to avoid shimmering of shadows edges. 

#### Cascade selection

When rendering the shadows the correct is selected by computing the depth of the pixel and comparing it to the split values of the cascades.

#### Configuration

Shadow can be configuring with the following parameters :

- shadow_map_resolution : to control the resolution of the shadow map
- shadow_max_distance : to control the max distance from the camera position at which the shadow will render
- shadow_cascade_split_lambda : to control the distribution between uniform and logarithmic terms when splitting the view frustum
- shadow_polygon_offset_units : to control depth bias

The last parameters control the depth bias applied when rendering shadow maps. This is used to avoid shadow acne issues.
Possible working value is 0.4. See [glPolygonOffset](https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glPolygonOffset.xhtml).

### Post Processing

The renderer uses a simple post processing pipeline that apply bloom, anti-aliasing, tone mapping and gamma correction. Bloom and 
fxaa can be disabled. Bloom by setting bloom_enabled to false and `fxaa` by setting `fxaa_quality` to DISABLED.

#### Anti Aliasing

The algorithm used for anti aliasing is the Fast Approximate Anti Aliasing (FXAA) in its version 3.11. 

I use nvidia's [FXAA 3.11 source code](https://gist.github.com/kosua20/0c506b81b3812ac900048059d2383126) from which I removed all the thing I do not need
(console and DX implementations for example).