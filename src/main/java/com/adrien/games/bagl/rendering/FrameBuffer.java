package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.Wrap;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

/**
 * This class creates an OpenGL {@link FrameBuffer} object. (version >= 3.0)
 * <br>When the frame buffer is bound OpenGL will render into this frame buffer. If no frame buffer
 * is bound OpenGL will then render in the default frame buffer
 * <p>
 * TODO: Allow the use of render buffer attachments alongside texture, this is useful when you don't need to read from one of the channel
 * TODO:    for example id tou need depth testing but wont read yourself the depth data
 *
 * @author Adrien
 */
public class FrameBuffer {

    /** Used to keep track of the bound frame buffer */
    private static int boundBuffer = 0;

    private final int handle;
    private final int width;
    private final int height;
    private boolean depthOnly;
    private Texture[] colorOutputs;
    private final Texture depthTexture;

    /**
     * Create a new {@link FrameBuffer}. This is a depth only frame buffer
     *
     * @param width  The width of the frame buffer
     * @param height The height of the frame buffer
     */
    public FrameBuffer(final int width, final int height) {
        this(width, height, new FrameBufferParameters());
    }

    /**
     * Create a new {@link FrameBuffer} from the passed in {@link FrameBufferParameters}
     *
     * @param width      The width of the frame buffer
     * @param height     The height of the frame buffer
     * @param parameters The parameters of the frame buffer
     */
    public FrameBuffer(final int width, final int height, final FrameBufferParameters parameters) {
        this.width = width;
        this.height = height;
        this.depthOnly = parameters.getColorOutputs().size() == 0;
        this.colorOutputs = this.depthOnly ? null : this.createColorOutputs(parameters, this.width, this.height);
        this.depthTexture = new Texture(this.width, this.height, new TextureParameters().format(parameters.getDepthTextureFormat()));
        this.handle = this.createBuffer(this.colorOutputs, this.depthOnly, this.depthTexture);
    }

    private Texture[] createColorOutputs(final FrameBufferParameters parameters, final int width, final int height) {
        final int colorOutputs = parameters.getColorOutputs().size();
        final Texture[] textures = new Texture[colorOutputs];
        for (int i = 0; i < colorOutputs; i++) {
            textures[i] = new Texture(width, height, new TextureParameters().format(parameters.getColorOutputs().get(i))
                    .sWrap(Wrap.CLAMP_TO_EDGE).tWrap(Wrap.CLAMP_TO_EDGE));
        }
        return textures;
    }

    /**
     * Create the actual OpenGL frame buffer object and attaches
     * the color buffer textures and the depth render buffer. If
     * the frame buffer is depth only, only depth texture is attached
     *
     * @param textures  The textures to use as color outputs
     * @param depthOnly Is the frame buffer depth only
     * @param depth     The depth texture
     * @return The handle of the OpenGL frame buffer
     */
    private int createBuffer(final Texture[] textures, final boolean depthOnly, final Texture depth) {
        final int bufferHandle = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferHandle);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth.getHandle(), 0);
        if (!depthOnly) {
            for (int i = 0; i < textures.length; i++) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, textures[i].getHandle(), 0);
            }
            this.enableAllColorOutputs();
        } else {
            glDrawBuffer(GL_NONE);
            // TODO: check glReadBuffer behavior
            //            glReadBuffer(GL_NONE);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return bufferHandle;
    }

    /**
     * Enable one or more color outputs for writing
     *
     * @param channels The channels of the outputs (0 for the first channel)
     * @throws EngineException if the frame buffer is not bound
     */
    public void enableColorOutputs(int... channels) {
        if (!this.isBound()) {
            throw new EngineException("You cannot enable color outputs on a frame buffer that is not currently bound");
        }
        glDrawBuffers(IntStream.of(channels).map(channel -> GL_COLOR_ATTACHMENT0 + channel).toArray());
    }

    /**
     * Enables all color outputs for writing
     *
     * @throws EngineException if the frame buffer is not bound
     */
    public void enableAllColorOutputs() {
        if (!this.isBound()) {
            throw new EngineException("You cannot enable color outputs on a frame buffer that is not currently bound");
        }
        glDrawBuffers(IntStream.range(0, this.colorOutputs.length).map(channel -> GL_COLOR_ATTACHMENT0 + channel).toArray());
    }

    /**
     * Disable writing for all color outputs
     *
     * @throws EngineException if the frame buffer is not bound
     */
    public void disableAllColorOutputs() {
        if (!this.isBound()) {
            throw new EngineException("You cannot disable color outputs on a frame buffer that is not currently bound");
        }
        glDrawBuffer(GL_NONE);
    }

    /**
     * Clear this frame buffer
     *
     * @throws EngineException if the frame buffer is not bound
     */
    public void clear() {
        this.clear(Color.BLACK);
    }

    /**
     * Clear this frame buffer
     *
     * @param color The background color
     * @throws EngineException if the frame buffer is not bound
     */
    public void clear(final Color color) {
        if (!this.isBound()) {
            throw new EngineException("You cannot clear a frame buffer that is not currently bound");
        }
        Engine.setClearColor(color);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Bind this frame buffer
     * <p>
     * Only one frame buffer can be bound at a time, so a previously
     * bound frame buffer would be unbound
     */
    public void bind() {
        if (!this.isBound()) {
            glBindFramebuffer(GL_FRAMEBUFFER, this.handle);
            FrameBuffer.boundBuffer = this.handle;
        }
    }

    /**
     * Unbind the currently bound frame buffer
     * <p>
     * When you unbind a frame buffer, the default frame buffer is automatically
     * bound
     *
     * @throws EngineException if the frame buffer is not bound
     */
    public void unbind() {
        if (!this.isBound()) {
            throw new EngineException("You cannot unbind a frame buffer that is not currently bound");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        FrameBuffer.boundBuffer = 0;
    }

    /**
     * Check if this buffer is the currently bound buffer
     *
     * @return true if this buffer is bound
     */
    private boolean isBound() {
        return this.handle == FrameBuffer.boundBuffer;
    }

    /**
     * Release resources
     */
    public void destroy() {
        if (this.isBound()) {
            this.unbind();
        }

        if (!this.depthOnly) {
            Arrays.stream(this.colorOutputs).forEach(Texture::destroy);
        }
        this.depthTexture.destroy();
        glDeleteFramebuffers(this.handle);
    }

    public Texture getColorTexture() {
        return this.getColorTexture(0);
    }

    public Texture getColorTexture(final int index) {
        if (this.depthOnly) {
            throw new EngineException("This frame buffer is depth only. Color textures should not be queried");
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
