package com.adrien.games.bagl.rendering;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.EngineException;
import com.adrien.games.bagl.rendering.texture.Format;
import com.adrien.games.bagl.rendering.texture.Texture;
import com.adrien.games.bagl.rendering.texture.TextureParameters;
import com.adrien.games.bagl.rendering.texture.Wrap;

import java.util.Arrays;
import java.util.Objects;
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
    private Texture[] colorOutputs;
    private Texture depthTexture;

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
        this.colorOutputs = parameters.getColorOutputs().isEmpty() ? null : this.createColorOutputs(parameters, this.width, this.height);
        this.depthTexture = parameters.hadDepthStencil() ?
                new Texture(this.width, this.height, new TextureParameters().format(parameters.getDepthStencilTextureFormat())) : null;
        this.handle = this.createBuffer(this.colorOutputs, this.depthTexture, parameters.getDepthStencilTextureFormat() == Format.DEPTH_32F);
    }

    /**
     * Create one texture for each of the color output specified if the frame buffer parameters
     *
     * @param parameters The parameters of the frame buffer
     * @param width      The width of each texture
     * @param height     The height of each texture
     * @return An array of {@link Texture}
     */
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
     * Create the actual OpenGL frame buffer object and attache
     * the color buffer textures and the depth/stencil texture. If
     * the frame buffer is depth/stencil only, only depth/stencil texture is attached
     *
     * @param textures     The textures to use as color outputs
     * @param depthStencil The depth/stencil texture
     * @return The handle of the OpenGL frame buffer
     */
    private int createBuffer(final Texture[] textures, final Texture depthStencil, final boolean depthOnly) {
        final int bufferHandle = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferHandle);
        glDrawBuffer(GL_NONE);

        if (Objects.nonNull(depthStencil)) {
            final int attachment = depthOnly ? GL_DEPTH_ATTACHMENT : GL_DEPTH_STENCIL_ATTACHMENT;
            glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, depthStencil.getHandle(), 0);
        }

        if (Objects.nonNull(textures)) {
            for (int i = 0; i < textures.length; i++) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, textures[i].getHandle(), 0);
            }
            this.enableAllColorOutputs();
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
    public void enableColorOutputs(final int... channels) {
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
     * Copy the data of a frame buffer into this frame buffer
     * <p>
     * This method only copies the depth and/or stencil buffers, not the color buffers
     *
     * @param frameBuffer The frame buffer to copy
     * @param depth       Should it copy the depth buffer
     * @param stencil     Should it copy the stencil buffer
     * @throws EngineException if this is not bound or if both depth and stencil flags are false
     */
    public void copyFrom(final FrameBuffer frameBuffer, final boolean depth, final boolean stencil) {
        if (!this.isBound()) {
            throw new EngineException("You cannot blit a frame buffer to an unbound frame buffer");
        }
        if (!depth && !stencil) {
            throw new EngineException("Either depth or stencil has to be true if you want to blit a frame buffer");
        }
        int flags = (depth ? GL_DEPTH_BUFFER_BIT : 0) | (stencil ? GL_STENCIL_BUFFER_BIT : 0);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBuffer.getHandle());
        glBlitFramebuffer(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight(), 0, 0, this.width, this.height, flags, GL_NEAREST);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.handle);
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
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

        if (Objects.nonNull(this.colorOutputs)) {
            Arrays.stream(this.colorOutputs).forEach(Texture::destroy);
        }

        if (Objects.nonNull(this.depthTexture)) {
            this.depthTexture.destroy();
        }

        glDeleteFramebuffers(this.handle);
    }

    public Texture getColorTexture(final int index) {
        if (Objects.isNull(this.colorOutputs)) {
            throw new EngineException("This frame buffer has no color outputs");
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
        if (Objects.isNull(this.depthTexture)) {
            throw new EngineException("This frame buffer has no depth texture");
        }
        return this.depthTexture;
    }
}
