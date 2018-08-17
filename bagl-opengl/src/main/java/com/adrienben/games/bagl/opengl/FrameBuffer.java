package com.adrienben.games.bagl.opengl;

import com.adrienben.games.bagl.core.Color;
import com.adrienben.games.bagl.core.exception.EngineException;
import com.adrienben.games.bagl.opengl.texture.Format;
import com.adrienben.games.bagl.opengl.texture.Texture2D;
import com.adrienben.games.bagl.opengl.texture.TextureParameters;
import com.adrienben.games.bagl.opengl.texture.Wrap;

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

    private final int width;
    private final int height;
    private final FrameBufferParameters parameters;
    private Texture2D[] colorOutputs;
    private Texture2D depthTexture;
    private final int handle;

    /**
     * Create a new {@link FrameBuffer}. This is a depth only frame buffer
     *
     * @param width  The width of the frame buffer
     * @param height The height of the frame buffer
     */
    public FrameBuffer(final int width, final int height) {
        this(width, height, FrameBufferParameters.getDefault());
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
        this.parameters = parameters;
        this.colorOutputs = parameters.getColorOutputs().isEmpty() ? null : this.createColorOutputs();
//        this.depthTexture = parameters.hadDepthStencil()
//                ? new Texture2D(this.width, this.height, TextureParameters.builder().format(parameters.getDepthStencilFormat()).compareFunction(parameters.getCompareFunction()).build())
//                : null;
        this.depthTexture = parameters.getDepthStencilTextureParameters().map(params -> new Texture2D(width, height, params)).orElse(null);
        this.handle = this.createBuffer();
    }

    /**
     * Create one texture for each of the color output specified if the frame buffer parameters
     *
     * @return An array of {@link Texture2D}
     */
    private Texture2D[] createColorOutputs() {
        final var colorOutputs = this.parameters.getColorOutputs().size();
        final var textures = new Texture2D[colorOutputs];
        final var params = TextureParameters.builder()
                .sWrap(Wrap.CLAMP_TO_EDGE)
                .tWrap(Wrap.CLAMP_TO_EDGE);
        for (var i = 0; i < colorOutputs; i++) {
            params.format(this.parameters.getColorOutputs().get(i));
            textures[i] = new Texture2D(this.width, this.height, params.build());
        }
        return textures;
    }

    /**
     * Create the actual OpenGL frame buffer object and attache
     * the color buffer textures and the depth/stencil texture. If
     * the frame buffer is depth/stencil only, only depth/stencil texture is attached
     *
     * @return The handle of the OpenGL frame buffer
     */
    private int createBuffer() {
        final var bufferHandle = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, bufferHandle);
        glDrawBuffer(GL_NONE);

        if (Objects.nonNull(this.depthTexture)) {
            final int attachment = this.isDepthOnly() ? GL_DEPTH_ATTACHMENT : GL_DEPTH_STENCIL_ATTACHMENT;
            glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, this.depthTexture.getHandle(), 0);
        }

        if (Objects.nonNull(this.colorOutputs)) {
            for (int i = 0; i < this.colorOutputs.length; i++) {
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_TEXTURE_2D, this.colorOutputs[i].getHandle(), 0);
            }
            this.enableAllColorOutputs();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return bufferHandle;
    }

    /**
     * Check if the frame buffer is depth only
     *
     * @return true if depth only false is depth/stencil
     */
    private boolean isDepthOnly() {
        return depthTexture.getParameters().getFormat() == Format.DEPTH_32F;
//        return this.parameters.getDepthStencilFormat() == Format.DEPTH_32F;
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
        final var flags = (depth ? GL_DEPTH_BUFFER_BIT : 0) | (stencil ? GL_STENCIL_BUFFER_BIT : 0);
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
        OpenGL.setClearColor(color);
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
            Arrays.stream(this.colorOutputs).forEach(Texture2D::destroy);
        }

        if (Objects.nonNull(this.depthTexture)) {
            this.depthTexture.destroy();
        }

        glDeleteFramebuffers(this.handle);
    }

    public Texture2D getColorTexture(final int index) {
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

    public Texture2D getDepthTexture() {
        if (Objects.isNull(this.depthTexture)) {
            throw new EngineException("This frame buffer has no depth texture");
        }
        return this.depthTexture;
    }
}
