package com.jwindustries.isitvegan.graphics;

import android.opengl.GLES30;
import android.util.Log;

import java.io.Closeable;

/**
 * A framebuffer associated with a texture.
 */
public class Framebuffer implements Closeable {
    private final int[] framebufferId = {0};
    private final Texture colorTexture;
    private int width = -1;
    private int height = -1;

    /**
     * Constructs a {@link Framebuffer} which renders internally to a texture.
     *
     * <p>In order to render to the {@link Framebuffer}, use {@link Render#draw(Mesh, Shader,
     * Framebuffer)}.
     */
    public Framebuffer(int width, int height) {
        try {
            colorTexture = new Texture(
                    Texture.Target.TEXTURE_2D,
                    Texture.WrapMode.CLAMP_TO_EDGE,
                    /*useMipmaps=*/ false
            );

            // Set initial dimensions.
            resize(width, height);

            // Create framebuffer object and bind to the color textures.
            GLES30.glGenFramebuffers(1, framebufferId, 0);
            GLError.maybeThrowGLException("Framebuffer creation failed", "glGenFramebuffers");
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId[0]);
            GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_COLOR_ATTACHMENT0,
                    GLES30.GL_TEXTURE_2D,
                    colorTexture.getTextureId(),
                    /*level=*/ 0
            );
            GLError.maybeThrowGLException("Failed to bind color texture to framebuffer", "glFramebufferTexture2D");

            int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw new IllegalStateException("Framebuffer construction not complete: code " + status);
            }
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    @Override
    public void close() {
        if (framebufferId[0] != 0) {
            GLES30.glDeleteFramebuffers(1, framebufferId, 0);
            GLError.maybeLogGLError(Log.WARN, "Framebuffer", "Failed to free framebuffer", "glDeleteFramebuffers");
            framebufferId[0] = 0;
        }
        colorTexture.close();
    }

    /**
     * Resizes the framebuffer to the given dimensions.
     */
    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;

        // Color texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colorTexture.getTextureId());
        GLError.maybeThrowGLException("Failed to bind color texture", "glBindTexture");
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                /*level=*/ 0,
                GLES30.GL_RGBA,
                width,
                height,
                /*border=*/ 0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                /*pixels=*/ null
        );
        GLError.maybeThrowGLException("Failed to specify color texture format", "glTexImage2D");
    }

    /**
     * Returns the color texture associated with this framebuffer.
     */
    public Texture getColorTexture() {
        return colorTexture;
    }

    /**
     * Returns the width of the framebuffer.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the framebuffer.
     */
    public int getHeight() {
        return height;
    }

    /* package-private */
    int getFramebufferId() {
        return framebufferId[0];
    }
}