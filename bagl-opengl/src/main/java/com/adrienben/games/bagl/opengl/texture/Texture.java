package com.adrienben.games.bagl.opengl.texture;

import com.adrienben.games.bagl.opengl.OpenGL;

import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30.GL_COMPARE_REF_TO_TEXTURE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

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
        setParameterI(GL_TEXTURE_MIN_FILTER, getParameters().getMinFilter().getGlFilter());
    }

    protected void applyMagFilterParameters() {
        setParameterI(GL_TEXTURE_MAG_FILTER, getParameters().getMagFilter().getGlFilter());
    }

    protected void applyAnisotropicParameter() {
        if (getParameters().getAnisotropic() > 0) {
            setParameterF(GL_TEXTURE_MAX_ANISOTROPY_EXT, getParameters().getAnisotropic());
        }
    }

    protected void applySWrapParameters() {
        setParameterI(GL_TEXTURE_WRAP_S, getParameters().getsWrap().getGlWrap());
    }

    protected void applyTWrapParameters() {
        setParameterI(GL_TEXTURE_WRAP_T, getParameters().gettWrap().getGlWrap());
    }

    protected void applyCompareFunctionParameters() {
        if (getParameters().getCompareFunction() != CompareFunction.NONE) {
            setParameterI(GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
            setParameterI(GL_TEXTURE_COMPARE_FUNC, getParameters().getCompareFunction().getGlCode());
        }
    }

    protected void setParameterI(final int parameterName, final int parameterValue) {
        glTexParameteri(type.getGlCode(), parameterName, parameterValue);
    }

    protected void setParameterF(final int parameterName, final float parameterValue) {
        glTexParameterf(type.getGlCode(), parameterName, parameterValue);
    }

    public void destroy() {
        if (textureUnit != NOT_BOUND_TEXTURE_UNIT) {
            unbind();
        }
        glDeleteTextures(handle);
    }

    public void bind() {
        bind(DEFAULT_TEXTURE_UNIT);
    }

    public void bind(final int textureUnit) {
        if (isBoundOn(textureUnit)) {
            return;
        }
        if (isBound()) {
            unbind();
        }
        OpenGL.bindTexture(this, textureUnit);
        this.textureUnit = textureUnit;
    }

    public void unbind() {
        if (isBound()) {
            OpenGL.unbindTexture(this, textureUnit);
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
