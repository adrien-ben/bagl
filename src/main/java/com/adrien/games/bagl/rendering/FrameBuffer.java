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

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.*;

/**
 * This class creates an OpenGL {@link FrameBuffer} object. (version >= 3.0).
 * <p>It binds a render buffer to if for the depth component and at least one texture (that you can
 * retrieve for later use) as the color buffers.
 * <br>When the frame buffer is bound OpenGL will render into this frame buffer. If no frame buffer
 * is bound OpenGL will then render in the default frame buffer.
 * <p>
 * TODO: Allow the use of render buffer attachments alongside texture, this is useful when you don't need to read from one of the channel
 * TODO:    for example id tou need depth testing but wont read yourself the depth data
 * <p>
 * TODO: Check is the buffer is bound before performing any action on it ? Or leave it to the user ?
 *
 * @author Adrien
 */
public class FrameBuffer {

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
            glDrawBuffers(this.generateBuffersToDraw(textures.length));
        } else {
            glDrawBuffer(GL_NONE);
// TODO: check glReadBuffer behavior
//            glReadBuffer(GL_NONE);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return bufferHandle;
    }

    /**
     * Generate an array containing the buffers to draw
     *
     * @param count The number of buffer to draw
     * @return An array of integer
     */
    private int[] generateBuffersToDraw(final int count) {
        final int[] buffers = new int[count];
        for (int i = 0; i < count; i++) {
            buffers[i] = GL_COLOR_ATTACHMENT0 + i;
        }
        return buffers;
    }

    /**
     * Add a color output to this frame buffer
     * <p>
     * The frame buffer <b>must</b> be bound !
     * Each time you add a color output to the frame buffer it becomes the
     * only one enabled for writing. If you want to enable all channels,
     * use {@link FrameBuffer#enableAllColorOutputs} method.
     * <p>
     * It can be useful to render different scenes in the same frame buffer :
     * <pre>
     * final FrameBuffer frameBuffer = new FrameBuffer(800, 600);
     * ...
     * // in render code
     * frameBuffer.bind();
     * for(int i = 0; i < 2; i++) {
     *     frameBuffer.addColorOutput(Format.RGBA8);
     *     FrameBuffer.clear()
     *
     *     // render you scene
     * }
     * FrameBuffer.unbind();
     * </pre>
     *
     * @param format The format the the color output
     */
    public void addColorOutput(final Format format) {
        if (Objects.isNull(this.colorOutputs)) {
            this.colorOutputs = new Texture[1];
            this.depthOnly = false;
        } else {
            this.colorOutputs = Arrays.copyOf(this.colorOutputs, this.colorOutputs.length + 1);
        }

        final Texture texture = new Texture(this.width, this.height, new TextureParameters().format(format).sWrap(Wrap.CLAMP_TO_EDGE)
                .tWrap(Wrap.CLAMP_TO_EDGE));
        this.colorOutputs[this.colorOutputs.length - 1] = texture;
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + this.colorOutputs.length - 1, GL_TEXTURE_2D, texture.getHandle(), 0);
        glDrawBuffer(GL_COLOR_ATTACHMENT0 + this.colorOutputs.length - 1);
        glBindFramebuffer(GL_FRAMEBUFFER, this.handle);
    }

    /**
     * Enables all color outputs for writing
     * <p>
     * The frame buffer <b>must</b> be bound !
     */
    public void enableAllColorOutputs() {
        glDrawBuffers(this.generateBuffersToDraw(this.colorOutputs.length));
    }

    /**
     * Clear the <b>currently bound</b> frame buffer
     */
    public static void clear() {
        FrameBuffer.clear(Color.BLACK);
    }

    /**
     * Clear the <u>currently bound</u> frame buffer
     *
     * @param color The background color
     */
    public static void clear(final Color color) {
        Engine.setClearColor(color);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Bind the frame buffer
     */
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.handle);
    }

    /**
     * Unbind the currently bound frame buffer
     */
    public static void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * Release resources
     */
    public void destroy() {
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
