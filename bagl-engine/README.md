# baGL - Engine

This module is the engine. It manages the window, inputs, and the game loop. It also contains the scene graph and the 
asset management system.

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

- minFilter : [Filter](src/main/java/com/adrienben/games/bagl/opengl/texture/Filter.java)
- magFilter : [Filter](src/main/java/com/adrienben/games/bagl/opengl/texture/Filter.java)
- sWrap : [Wrap](src/main/java/com/adrienben/games/bagl/opengl/texture/Wrap.java)
- tWrap - [Wrap](src/main/java/com/adrienben/games/bagl/opengl/texture/Wrap.java)
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
