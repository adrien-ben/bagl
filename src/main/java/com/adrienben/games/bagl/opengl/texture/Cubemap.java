package com.adrienben.games.bagl.opengl.texture;

import com.adrienben.games.bagl.core.exception.EngineException;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.*;

/**
 * OpenGL cube map
 *
 * @author adrien
 */
public class Cubemap {

    private final int handle;
    private final int width;
    private final int height;
    private final TextureParameters parameters;

    /**
     * Create a cubemap
     * <p>
     * The storage for pixel data is allocated but left empty
     *
     * @param width      The width of each face of the cubemap
     * @param height     The height of each face of the cubemap
     * @param parameters The texture parameters of the cubemap
     */
    public Cubemap(final int width, final int height, final TextureParameters parameters) {
        this.handle = glGenTextures();
        this.width = width;
        this.height = height;
        this.parameters = parameters;

        glBindTexture(GL_TEXTURE_CUBE_MAP, this.handle);
        for (var i = 0; i < 6; i++) {
            this.initCubemapFace(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, parameters.getMagFilter().getGlFilter());
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, parameters.getMinFilter().getGlFilter());
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        if (parameters.getMipmaps()) {
            GL30.glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        }
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    /**
     * Allocate memory for one of the face of the cubemap
     *
     * @param faceId The id of the face
     */
    private void initCubemapFace(final int faceId) {
        final var format = this.parameters.getFormat();
        if (format.getGlDataType() == GL_FLOAT) {
            glTexImage2D(faceId, 0, format.getGlInternalFormat(), this.width, this.height, 0, format.getGlFormat(), format.getGlDataType(), FloatBuffer.class.cast(null));
        } else if (format.getGlDataType() == GL_UNSIGNED_BYTE) {
            glTexImage2D(faceId, 0, format.getGlInternalFormat(), this.width, this.height, 0, format.getGlFormat(), format.getGlDataType(), ByteBuffer.class.cast(null));
        } else {
            throw new EngineException("Cannot initialize texture faceId for this data type (" + format.getGlDataType() + ")");
        }
    }

    /**
     * Release resources
     */
    public void destroy() {
        glDeleteTextures(this.handle);
    }

    /**
     * Bind the cubemap on the first texture unit
     */
    public void bind() {
        this.bind(0);
    }

    /**
     * Bind the cubemap
     *
     * @param unit Texture unit on which to bind the cubemap
     */
    public void bind(final int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, this.handle);
    }

    /**
     * Unbind the cube map from the first texture unit
     */
    public static void unbind() {
        Cubemap.unbind(0);
    }

    /**
     * Unbind the cubemap
     *
     * @param unit Texture unit from which to unbind the cubemap
     */
    public static void unbind(final int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    public int getHandle() {
        return this.handle;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public TextureParameters getParameters() {
        return this.parameters;
    }
}
