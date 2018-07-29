# baGL

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/50b75bdb99474b3d8c6f47e49876d90b)](https://www.codacy.com/app/adrien.bennadji/bagl?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=adrien-ben/bagl&amp;utm_campaign=Badge_Grade)

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
- Basic assets management

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

## Assets management

baGL includes basic asset management. You can declare all assets in a json file and use the `AssetStore` class the handle them. Each asset is loaded when 
requested for the first time. By default the file loaded is `classpath:/assets.json`.

You can change the asset descriptor file path by setting the `assets_descriptor_path` in the config.properties file.

### Asset types

There are several types of assets.

- TEXTURE
- FONT
- MODEL
- SCENE

### Assets descriptor syntax 

Here is an example of the json file containing the assets description.

```json
{
  "assets": [
    {
      "id": "texture_0",
      "type": "TEXTURE",
      "path": "classpath:/texture_0.png",
      "parameters": {
        "minFilter": "NEAREST",
        "magFilter": "NEAREST"
      }
    }
  ]
}
```

Asset will always need an id, a type and a path. For some asset types you will be able to define parameters.

### Asset parameters

This section contains the details of the possible parameters for asset which accept them.

#### Texture parameters

- minFilter : [Filter](src/main/java/com/adrien/games/bagl/rendering/texture/Filter.java)
- magFilter : [Filter](src/main/java/com/adrien/games/bagl/rendering/texture/Filter.java)
- sWrap : [Wrap](src/main/java/com/adrien/games/bagl/rendering/texture/Wrap.java)
- tWrap - [Wrap](src/main/java/com/adrien/games/bagl/rendering/texture/Wrap.java)
- anisotropic : int - level of anisotropic filtering
- mipmaps : boolean - if true mipmaps are generated for the textures

### Requesting an asset

```java
class Example extends DefaultGame {
    private Font font;
    @Override public void init() {
        font = getAssetStore().getAsset("segoe", Font.class);
    }
}
```

> Note that the class extends DefaultGame. This is important because getAssetStore is a protected method of DefaultGame.

## Scene model

This section details the scene model.

### Game Object

A scene is composed of game objects. A game object is an entity which is represented by :

- An Id (used for querying objects)
- A set of tags (used for querying objects)
- A transform
- A set of components
- A set of child objects

### Components

A component represent a facet of a game object. Each component will hold data and or logic that will 
have an effect on its parent game object. baGL provides a set of components. They are used for scene
rendering :

- CameraComponent : hold the camera that will be used as the point of view when rendering the scene
- DirectionalLightComponent : hold a directional light
- PointLightComponent : hold a point light
- SpotLightComponent : hold a spot light
- EnvironmentComponent : hold the environment maps that will be used for IBL computing
- ModelComponent : hold a 3D model
- ParticleComponent : hold a particle emitter

### Scene

A scene is an acyclic graph of game objects. When updated, the graph is traversed from the root to the 
leaves and each node is updated. When a node is updated its transform is recomputed relatively to the
transform of its parent.

### Json Model

Scenes can be loaded from json files. Here is an example of a scene file without components and no children.

```json
{
  "children": [
    {
      "id": "main_camera",
      "tags": ["camera", "required"],
      "transform": {
        "translation": {
          "x": -1.8,
          "y": 3.0,
          "z": -3.2
        },
        "rotations": [
          {
            "x": 0.0,
            "y": 30.0,
            "z": 0.0
          }
        ],
        "scale": {
          "x": 1.0,
          "y": 1.0,
          "z": 1.0
        }
      },
      "children": [],
      "components": []
    }
  ]
}
```

#### CameraComponent model

```json
{
  "type": "camera",
  "fov": 60.0,
  "near": 0.1,
  "far": 1000.0,
  "enableController": true
}
```

#### DirectionalLightComponent model

```json
{
  "type": "directional_light",
  "intensity": 0.8,
  "color": {
    "r": 1.0,
    "g": 1.0,
    "b": 1.0
  }
}

```

#### PointLightComponent model

```json
{
  "type": "point_light",
  "intensity": 0.8,
  "color": {
    "r": 0.0,
    "g": 1.0,
    "b": 0.0
  },
  "radius": 3.0
}
```

#### SpotLightComponent model

```json
{
  "type": "spot_light",
  "intensity": 10.0,
  "color": {
    "r": 1.0,
    "g": 0.0,
    "b": 0.0
  },
  "radius": 20.0,
  "angle": 20.0,
  "edge": 5.0
}
```

#### EnvironmentComponent model

```json
{
  "type": "environment",
  "path": "classpath:/envmaps/beach.hdr"
}
```

#### ModelComponent model

```json
{
  "type": "model",
  "path": "classpath:/models/helmet/helmet.glb"
}
```

> path can be replaced by id. id must contain the id of an asset that as been declared in the asset descriptor file.

#### ParticleComponent model

```json
{
    "type": "particles",
    "texturePath": "classpath:/smoke.png",
    "startColor": {
        "r": 1.0,
        "g": 1.0,
        "b": 1.0,
        "a": 1.0
    },
    "endColor": {
        "r": 1.0,
        "g": 1.0,
        "b": 1.0,
        "a": 0.0
    },
    "blendMode": "ADDITIVE",
    "rate": 0.05,
    "batchSize": 5,
    "initializer": {
        "position": {
            "min": {
                "x": -1.0,
                "y": 0.0,
                "z": -1.0
            },
            "max": {
                "x": 1.0,
                "y": 0.0,
                "z": 1.0
            }
        },
        "direction": {
            "min": {
                "x": -1.0,
                "y": 1.0,
                "z": -1.0
            },
            "max": {
                "x": 1.0,
                "y": 1.0,
                "z": 1.0
            }
        },
        "size": {
            "min": 0.5,
            "max": 1.0
        },
        "speed": {
            "min": 4.0,
            "max": 5.0
        },
        "ttl": {
            "min": 1.6,
            "max": 2.0
        }
    }
}
```

> texturePath can be replaced by textureId. textureId must contain the id of a texture that as been declared in the asset descriptor file.

#### Extending the model

You can extend the scene model by creating new components and extending the `SceneLoader` class.

To add a new component you will need to create a class that extend `Component` and implement the `update` method. 
For example let's create a teleporter component that will teleport its parent object at a fixed interval.

```java
public class TeleporterComponent extends Component {

    private final float rate;
    private final float radius;
    private float timeSinceLastTeleportation = 0.0f;

    // Constructor

    @Override
    public void update(final Time time) {
        timeSinceLastTeleportation += time.getElapsedTime();
        if (timeSinceLastTeleportation >= rate) {
            final var newPosition = generateNewPosition();
            getParentObject().getLocalTransform().setTranslation(newPosition);
            timeSinceLastTeleportation -= rate;
        }
    }
}
```

Then you will need to create a class representing the json model.

```java
public class TeleporterJson {
    private final float rate;
    private final float radius;
    // Getters
}
```

Finally you will have to tell the SceneLoader how to generate the Component from the Json model.

```java
class ComponentExample {
    public void loadScene() {
        final var sceneLoader = new SceneLoader();
        sceneLoader.getComponentFactory().addComponentCreationCommand("teleporter", TeleporterJson.class, this::mapTeleporter);
        final var scene = sceneLoader.load(ResourcePath.get("..."));
    }
    
    private TeleporterComponent mapTeleporter(final TeleporterJson teleporterJson) {
        return new TeleporterComponent(teleporterJson.getRate(), teleporterJson.getRadius());
    }
}
```

Since the fields of `TeleporterComponent` and `TeleporterJson` are the same you could have skipped the json model class
creation. Then you would have called the `addComponentCreationCommand` as follows :

```java
class ComponentExample {
    public void loadScene() {
        final var sceneLoader = new SceneLoader();
        sceneLoader.getComponentFactory().addComponentCreationCommand("teleporter", TeleporterComponent.class);
        final var scene = sceneLoader.load(ResourcePath.get("..."));
    }
}
```

You can now declare the teleporter component in the json file.

```json
{
  "type": "teleporter",
  "rate": 2.0,
  "radius": 1.0
}
```

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
