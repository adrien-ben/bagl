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

## Generic key frame based animations

baGL provides a generic API to construct key frame based animations.

### Key frames

The key frame class is used to represent the state of some data at a given time.

### Interpolator

The Interpolator functional interface defines a contract to interpolate between the data of two key frames.

### Animator

The animator is responsible for animating a target over time. It hold a reference to a target to animate and the list
of key frames representing the states of its target over time. It needs to be passed an interpolator, a target updater
and a supplier that will be used to cache the current interpolated state of the target data.

### TargetUpdater

The TargetUpdater functional interface defines the contract for updating some target with some data. It will be used
to apply the current state the data to the animated target.

### Animation

The animation is responsible for managing the current animation time. It exposes an API to allow the used to control the
animation playback (play, pause, stop, ...).

The animation is a collection of animators that will be executed over time.

### Example

Let's see an example of an animated character.

```java
class AnimationExample {
    
    static class Character {
        private Vector2f position = new Vector2f();
        void moveTo(Vector2f position) {
            this.position.set(position);
        }
        // omitted accessors
    }

    public static void main(String[] args) throws InterruptedException {
        
        // The character to animate
        var john = new Character();
        
        // The list of john's position keyframes
        var keyFrames = List.of(
                new KeyFrame<>(0, new Vector2f(-10, 0)), // Position at the start of animation
                new KeyFrame<>(10, new Vector2f(10, 0))); // Position at the end of animation
                
        // The animator responsible for animating john's position
        var animator = Animator.<Character, Vector2f>builder()
                .target(john)
                .keyFrames(keyFrames)
                .interpolator(Vector2f::lerp) // Linear interpolator
                .targetUpdater(Character::moveTo) // Update john by setting its position
                .currentValueSupplier(Vector2f::new)  // Cache supplier for the interpolated value
                .build();
        
        // The actual animation
        var animation = Animation.<Character>builder().animator(Vector2f.class, animator).build();
        animation.play(); // Paused by default

        // The main loop
        var time = new Time();
        while (true) {
            time.update(); // Update timer
            animation.step(time); // Step animation
            System.out.println("John's position: " + john.getPosition());
            Thread.sleep(1000);
        }
    }
}
```

The animation consists of moving `john` from (-10, 0) to (10, 0) in ten seconds. Once the animation is created, we
loop every second and use the `step` method to advance the animation. Here is the output of the example.

```text
John's position: (-1,000E+1  0,000E+0)
John's position: (-7,828E+0  0,000E+0)
John's position: (-5,827E+0  0,000E+0)
John's position: (-3,827E+0  0,000E+0)
John's position: (-1,826E+0  0,000E+0)
...
John's position: (1,000E+1  0,000E+0)
```

> Note that we had to call the `play` method before starting the loop. By default animations are paused.

> Here we just used one animator but we could have used more of them. Either animating the same property
> or others. For example if john had a height attribute we could have made him grow from 0 to 5 then shrink
> back to its original height from 5 to 10.