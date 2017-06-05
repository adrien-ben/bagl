package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

/**
 * This class creates an OpenGL {@link FrameBuffer} object. (version >= 3.0).
 * <p>It binds a render buffer to if for the depth component and at least one texture (that you can 
 * retrieve for later use) as the color buffers. 
 * <br>When the frame buffer is bound OpenGL will render into this frame buffer. If no frame buffer
 * is bound OpenGL will then render in the default frame buffer.
 *
 * @author Adrien
 *
 */
public class FrameBuffer {

    private final int handle;
    private final int width;
    private final int height;
    private final boolean depthOnly;
    private final Texture[] colorOutputs;
    private final Texture depthTexture;

    /**
     * Creates a new {@link FrameBuffer}. This is a depth only frame buffer.
     * @param width The width of the frame buffer.
     * @param height The height of the frame buffer.
     */
    public FrameBuffer(int width, int height) {
        this(width, height, 0);
    }

    /**
     * Creates a new {@link FrameBuffer}. If colorOutputs is 0 then the
     * frame buffer will be depth only. The color output texture format
     * will be RGBA8.
     * @param width The width of the frame buffer.
     * @param height The height of the frame buffer.
     * @param colorOutputs The number of color outputs. 0 Means depth only.
     */
    public FrameBuffer(int width, int height, int colorOutputs) {
        this(width, height, FrameBufferParameters.generatesRGBA8Parameters(colorOutputs));
    }

    /**
     * Creates a new {@link FrameBuffer} from the passed in {@link FrameBufferParameters}.
     * @param width The width of the frame buffer.
     * @param height The height of the frame buffer.
     * @param parameters The parameters of the frame buffer.
     */
    public FrameBuffer(int width, int height, FrameBufferParameters parameters) {
        this.width = width;
        this.height = height;
        this.depthOnly = parameters.getColorOutputs().size() == 0;
        this.colorOutputs = this.depthOnly ? null : createColorOutputs(parameters, this.width, this.height);
        this.depthTexture = new Texture(this.width, this.height, new TextureParameters().format(Format.DEPTH_32F));
        this.handle = createBuffer(this.colorOutputs, this.depthOnly, this.depthTexture);
    }

    /**
     * Creates all textures to use as the different color outputs.
     * @param colorOutputs The number of texture to generate.
     * @param width The width of the frame buffer.
     * @param height The height of the frame buffer.
     * @return An array of {@link Texture}.
     */
    private static Texture[] createColorOutputs(int colorOutputs, int width, int height) {
        final Texture[] textures = new Texture[colorOutputs];
        for(int i = 0; i < colorOutputs; i++) {
            textures[i] = new Texture(width, height, new TextureParameters().format(Format.RGBA8));
        }
        return textures;
    }

    private static Texture[] createColorOutputs(FrameBufferParameters parameters, int width, int height) {
        final int colorOutputs = parameters.getColorOutputs().size();
        final Texture[] textures = new Texture[colorOutputs];
        for(int i = 0; i < colorOutputs; i++) {
            textures[i] = new Texture(width, height, new TextureParameters().format(parameters.getColorOutputs().get(i)));
        }
        return textures;
    }

    /**
     * Create the actual OpenGL frame buffer object and attaches
     * the color buffer textures and the depth render buffer. If
     * the frame buffer is depth only, only depth texture is attached.
     * @param textures The textures to use as color outputs.
     * @param depthOnly Is the frame buffer depth only.
     * @param depth The depth texture.
     * @return The handle of the OpengGL frame buffer.
     */
    private static int createBuffer(Texture[] textures, boolean depthOnly, Texture depth) {
        final int bufferHandle = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferHandle);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth.getHandle(), 0);
        if(!depthOnly) {
            for(int i = 0; i < textures.length; i++) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, textures[i].getHandle(), 0);
            }
            glDrawBuffers(generateBuffersToDraw(textures.length));
        } else {
            glDrawBuffer(GL_NONE);
            glReadBuffer(GL_NONE);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return bufferHandle;
    }

    /**
     * Generates an array containing the buffers to draw.
     * @param count The number of buffer to draw.
     * @return An array of integer.
     */
    private static int[] generateBuffersToDraw(int count) {
        final int[] buffers = new int[count];
        for(int i = 0; i < count; i++) {
            buffers[i] = GL_COLOR_ATTACHMENT0 + i;
        }
        return buffers;
    }

    /**
     * Clears the <b>currently bound</b> frame buffer.
     */
    public static void clear() {
        FrameBuffer.clear(Color.BLACK);
    }

    /**
     * Clears the <u>currently bound</u> frame buffer.
     * @param color The background color.
     */
    public static void clear(Color color) {
        Engine.setClearColor(color);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Binds the frame buffer.
     */
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.handle);
    }

    /**
     * Unbinds the currently bound frame buffer.
     */
    public static void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Releases resources.
     */
    public void destroy() {
        if(!this.depthOnly) {
            Arrays.stream(this.colorOutputs).forEach(Texture::destroy);
        }
        this.depthTexture.destroy();
        glDeleteFramebuffers(this.handle);
    }

    public Texture getColorTexture() {
        return this.getColorTexture(0);
    }

    public Texture getColorTexture(int index) {
        if(this.depthOnly) {
            throw new RuntimeException("This frame buffer is depth only. Color textures should not be queried.");
        }
        return this.colorOutputs[index];
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

    public Texture getDepthTexture() {
        return this.depthTexture;
    }

}
