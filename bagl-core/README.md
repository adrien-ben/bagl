# baGL - Core

This is the core module of baGL it contains all the classes that will be used by all the other module like exceptions, 
maths, validations, io and utility classes.

## ResourcePath

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