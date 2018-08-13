package com.adrienben.games.bagl.opengl.texture;

import com.adrienben.games.bagl.core.exception.EngineException;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;

/**
 * OpenGL cube map
 *
 * @author adrien
 */
public class Cubemap extends Texture {

    private final int width;
    private final int height;

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
        super(Type.CUBE_MAP, parameters);
        this.width = width;
        this.height = height;
        bind();
        allocateCubeMapFaces();
        applyTextureParameters();
        unbind();
    }

    private void allocateCubeMapFaces() {
        for (var i = 0; i < 6; i++) {
            initCubemapFace(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
        }
    }

    /**
     * Allocate memory for one of the face of the cubemap
     *
     * @param faceId The id of the face
     */
    private void initCubemapFace(final int faceId) {
        final var format = getParameters().getFormat();
        if (format.getGlDataType() == GL_FLOAT) {
            glTexImage2D(faceId, 0, format.getGlInternalFormat(), this.width, this.height, 0, format.getGlFormat(), format.getGlDataType(), (FloatBuffer) null);
        } else if (format.getGlDataType() == GL_UNSIGNED_BYTE) {
            glTexImage2D(faceId, 0, format.getGlInternalFormat(), this.width, this.height, 0, format.getGlFormat(), format.getGlDataType(), (ByteBuffer) null);
        } else {
            throw new EngineException("Cannot initialize texture faceId for this data type (" + format.getGlDataType() + ")");
        }
    }

    private void applyTextureParameters() {
        applyMinFilterParameters();
        applyMagFilterParameters();
        setParameterI(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        setParameterI(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        setParameterI(GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        applyMipmapParameter();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
