package com.adrienben.games.bagl.opengl.texture;

import static com.adrienben.games.bagl.opengl.OpenGL.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30.GL_COMPARE_REF_TO_TEXTURE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * Base class for textures containing basic texture operations.
 *
 * @author adrien
 */
public abstract class Texture {

    private static final int DEFAULT_TEXTURE_UNIT = 0;
    private static final int NOT_BOUND_TEXTURE_UNIT = -1;

    private final Type type;
    private final TextureParameters parameters;
    private final int handle;
    private int textureUnit = NOT_BOUND_TEXTURE_UNIT;

    public Texture(final Type type, final TextureParameters parameters) {
        this.type = type;
        this.parameters = parameters;
        this.handle = glGenTextures();
    }

    protected void applyMipmapParameter() {
        if (getParameters().getMipmaps()) {
            glGenerateMipmap(getType().getGlCode());
        }
    }

    protected void applyMinFilterParameters() {
        setParameterI(type, GL_TEXTURE_MIN_FILTER, getParameters().getMinFilter().getGlFilter());
    }

    protected void applyMagFilterParameters() {
        setParameterI(type, GL_TEXTURE_MAG_FILTER, getParameters().getMagFilter().getGlFilter());
    }

    protected void applyAnisotropicParameter() {
        if (getParameters().getAnisotropic() > 0) {
            setParameterF(type, GL_TEXTURE_MAX_ANISOTROPY_EXT, getParameters().getAnisotropic());
        }
    }

    protected void applySWrapParameter() {
        setParameterI(type, GL_TEXTURE_WRAP_S, getParameters().getsWrap().getGlWrap());
    }

    protected void applyTWrapParameter() {
        setParameterI(type, GL_TEXTURE_WRAP_T, getParameters().gettWrap().getGlWrap());
    }

    protected void applyBorderColorParameter() {
        setParameterColor(type, GL_TEXTURE_BORDER_COLOR, parameters.getBorderColor());
    }

    protected void applyCompareFunctionParameters() {
        if (getParameters().getCompareFunction() != CompareFunction.NONE) {
            setParameterI(type, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
            setParameterI(type, GL_TEXTURE_COMPARE_FUNC, getParameters().getCompareFunction().getGlCode());
        }
    }

    /**
     * Destroy the wrapped OpenGL texture.
     * <p>
     * Unbind it first if bound.
     */
    public void destroy() {
        if (isBound()) {
            unbind();
        }
        glDeleteTextures(handle);
    }

    /**
     * Bind the texture on the texture unit {@value DEFAULT_TEXTURE_UNIT}.
     */
    public void bind() {
        bind(DEFAULT_TEXTURE_UNIT);
    }

    /**
     * Bind the texture on a texture unit.
     *
     * @param textureUnit The texture unit on which to bind the texture.
     */
    public void bind(final int textureUnit) {
        if (isBoundOn(textureUnit)) {
            return;
        }
        if (isBound()) {
            unbind();
        }
        bindTexture(this, textureUnit);
        this.textureUnit = textureUnit;
    }

    /**
     * Unbind the texture if bound.
     */
    public void unbind() {
        if (isBound()) {
            unbindTexture(this, textureUnit);
            textureUnit = NOT_BOUND_TEXTURE_UNIT;
        }
    }

    private boolean isBound() {
        return textureUnit != NOT_BOUND_TEXTURE_UNIT;
    }

    private boolean isBoundOn(final int textureUnit) {
        return this.textureUnit == textureUnit;
    }

    public Type getType() {
        return type;
    }

    public TextureParameters getParameters() {
        return parameters;
    }

    public int getHandle() {
        return handle;
    }
}
