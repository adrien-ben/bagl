package com.adrien.games.bagl.opengl.shader;

import com.adrien.games.bagl.core.exception.EngineException;
import com.adrien.games.bagl.core.io.ResourcePath;
import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ShaderSourceParser} test class.
 *
 * @author adrien
 */
class ShaderSourceParserTest {

    private static final String TEST_SHADER_FOLDER = "classpath:/shader_parser";

    private final ShaderSourceParser parser = new ShaderSourceParser();

    @Test
    void itShouldLoadShaderSourceWithoutDependencies() {
        final var source = parser.parse(ResourcePath.get(TEST_SHADER_FOLDER, "shader_without_dependencies.glsl"));
        final var expectedSource = new StringJoiner(System.lineSeparator(), "", System.lineSeparator())
                .add("#version 330")
                .add("void main() {}")
                .toString();
        assertEquals(expectedSource, source);
    }

    @Test
    void itShouldLoadShaderSourceWithSingleDependency() {
        final var source = parser.parse(ResourcePath.get(TEST_SHADER_FOLDER, "shader_with_single_dependency.glsl"));
        final var expectedSource = new StringJoiner(System.lineSeparator(), "", System.lineSeparator())
                .add("#version 330")
                .add("const int VALUE2 = 2;")
                .add("void main() {}")
                .toString();
        assertEquals(expectedSource, source);
    }

    @Test
    void itShouldLoadShaderSourceWithADependencyAppearingTwice() {
        final var source = parser.parse(ResourcePath.get(TEST_SHADER_FOLDER, "shader_with_dependency_appearing_twice.glsl"));
        final var expectedSource = new StringJoiner(System.lineSeparator(), "", System.lineSeparator())
                .add("#version 330")
                .add("const int VALUE2 = 2;")
                .add("const int VALUE1 = 1;")
                .add("void main() {}")
                .toString();
        assertEquals(expectedSource, source);
    }

    @Test
    void itShouldLoadShaderSourceWithSelfDependency() {
        final var source = parser.parse(ResourcePath.get(TEST_SHADER_FOLDER, "shader_with_self_dependency.glsl"));
        final var expectedSource = new StringJoiner(System.lineSeparator(), "", System.lineSeparator())
                .add("#version 330")
                .add("void main() {}")
                .toString();
        assertEquals(expectedSource, source);
    }

    @Test
    void itShouldFailToLoadShaderWithNonExistingDependency() {
        assertThrows(EngineException.class,
                () -> parser.parse(ResourcePath.get(TEST_SHADER_FOLDER, "shader_with_nonexisting_dependency.glsl")));
    }

    @Test
    void itShouldFailToLoadShaderWithInvalidDependency() {
        assertThrows(EngineException.class,
                () -> parser.parse(ResourcePath.get(TEST_SHADER_FOLDER, "shader_with_invalid_dependency.glsl")));
    }
}