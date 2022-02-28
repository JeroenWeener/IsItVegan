package com.jwindustries.isitvegan.graphics;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Render {
    private final AssetManager assetManager;

    private int viewportWidth = 1;
    private int viewportHeight = 1;

    /**
     * Constructs a Render object and instantiates GLSurfaceView parameters.
     *
     * @param glSurfaceView Android GLSurfaceView
     * @param renderer      Renderer implementation to receive callbacks
     * @param assetManager  AssetManager for loading Android resources
     */
    public Render(GLSurfaceView glSurfaceView, Renderer renderer, AssetManager assetManager) {
        this.assetManager = assetManager;
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.setRenderer(
                new GLSurfaceView.Renderer() {
                    @Override
                    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                        GLES30.glEnable(GLES30.GL_BLEND);
                        GLError.maybeThrowGLException("Failed to enable blending", "glEnable");
                        renderer.onSurfaceCreated(Render.this);
                    }

                    @Override
                    public void onSurfaceChanged(GL10 gl, int w, int h) {
                        viewportWidth = w;
                        viewportHeight = h;
                        renderer.onSurfaceChanged(Render.this, w, h);
                    }

                    @Override
                    public void onDrawFrame(GL10 gl) {
                        clear(/*framebuffer=*/ null, 0f, 0f, 0f, 1f);
                        renderer.onDrawFrame(Render.this);
                    }
                });
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setWillNotDraw(false);
    }

    /**
     * Draw a {@link Mesh} with the specified {@link Shader}.
     */
    public void draw(Mesh mesh, Shader shader) {
        draw(mesh, shader, /*framebuffer=*/ null);
    }

    /**
     * Draw a {@link Mesh} with the specified {@link Shader} to the given {@link Framebuffer}.
     *
     * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is used.
     */
    public void draw(Mesh mesh, Shader shader, Framebuffer framebuffer) {
        useFramebuffer(framebuffer);
        shader.lowLevelUse();
        mesh.lowLevelDraw();
    }

    /**
     * Clear the given framebuffer.
     *
     * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is
     * cleared.
     */
    public void clear(Framebuffer framebuffer, float r, float g, float b, float a) {
        useFramebuffer(framebuffer);
        GLES30.glClearColor(r, g, b, a);
        GLError.maybeThrowGLException("Failed to set clear color", "glClearColor");
        GLES30.glDepthMask(true);
        GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear");
    }

    /**
     * Interface to be implemented for rendering callbacks.
     */
    public interface Renderer {
        /**
         * Called by {@link Render} when the GL render surface is created.
         *
         * <p>See {@link GLSurfaceView.Renderer#onSurfaceCreated}.
         */
        void onSurfaceCreated(Render render);

        /**
         * Called by {@link Render} when the GL render surface dimensions are changed.
         *
         * <p>See {@link GLSurfaceView.Renderer#onSurfaceChanged}.
         */
        void onSurfaceChanged(Render render, int width, int height);

        /**
         * Called by {@link Render} when a GL frame is to be rendered.
         *
         * <p>See {@link GLSurfaceView.Renderer#onDrawFrame}.
         */
        void onDrawFrame(Render render);
    }

    /* package-private */
    AssetManager getAssets() {
        return assetManager;
    }

    private void useFramebuffer(Framebuffer framebuffer) {
        int framebufferId;
        int viewportWidth;
        int viewportHeight;
        if (framebuffer == null) {
            framebufferId = 0;
            viewportWidth = this.viewportWidth;
            viewportHeight = this.viewportHeight;
        } else {
            framebufferId = framebuffer.getFramebufferId();
            viewportWidth = framebuffer.getWidth();
            viewportHeight = framebuffer.getHeight();
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");
    }
}
