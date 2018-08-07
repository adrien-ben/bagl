# baGL - Tests

This module contains utility classes for unit tests.

## JUnit 5

Unit tests in baGL are written using Junit 5.

## OGLExtension

This module provides the `OGLExtension` class which is a Junit 5 extension that creates an OpenGL context before the tests
of a class using the extension are started. It is required to unit tests classes that make calls to the OpenGL API.

```java
@ExtendWith(OGLExtension.class)
class OpenGLRelatedTests {
    @Test
    void someTestThatMakeCallsToTheOpenGLApi() {
        // ...
    }
}
```