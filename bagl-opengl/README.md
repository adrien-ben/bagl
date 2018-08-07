# baGL - OpenGL

This module contains the OpenGL wrapper classes and utility classes to make the construction of OpenGL objects easier.

## Shader files

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