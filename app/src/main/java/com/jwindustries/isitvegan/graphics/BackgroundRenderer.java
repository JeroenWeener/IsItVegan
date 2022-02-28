package com.jwindustries.isitvegan.graphics;

import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * This class both renders the AR camera background and composes the a scene foreground.
 */
public class BackgroundRenderer {
    // components_per_vertex * number_of_vertices * float_size
    private static final int COORDS_BUFFER_SIZE = 2 * 4 * 4;

    private static final FloatBuffer NDC_QUAD_COORDS_BUFFER =
            ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private static final FloatBuffer VIRTUAL_SCENE_TEX_COORDS_BUFFER =
            ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    static {
        NDC_QUAD_COORDS_BUFFER.put(new float[]{
                /*0:*/ -1f, -1f, /*1:*/ +1f, -1f, /*2:*/ -1f, +1f, /*3:*/ +1f, +1f,
        });
        VIRTUAL_SCENE_TEX_COORDS_BUFFER.put(new float[]{
                /*0:*/ 0f, 0f, /*1:*/ 1f, 0f, /*2:*/ 0f, 1f, /*3:*/ 1f, 1f,
        });
    }

    private final FloatBuffer cameraTexCoords = ByteBuffer.allocateDirect(COORDS_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private final Mesh mesh;
    private final VertexBuffer cameraTexCoordsVertexBuffer;
    private Shader backgroundShader;
    private Shader occlusionShader;
    private final Texture cameraColorTexture;

    /**
     * Allocates and initializes OpenGL resources needed by the background renderer. Must be called
     * during a {@link Render.Renderer} callback, typically in {@link
     * Render.Renderer#onSurfaceCreated(Render)} ()}.
     */
    public BackgroundRenderer(Render render) {
        cameraColorTexture = new Texture(
                Texture.Target.TEXTURE_EXTERNAL_OES,
                Texture.WrapMode.CLAMP_TO_EDGE,
                /*useMipmaps=*/ false
        );

        try {
            backgroundShader = Shader
                    .createFromAssets(
                            render,
                            "shaders/background_camera.vert",
                            "shaders/background_camera.frag"
                    )
                    .setTexture("u_CameraColorTexture", cameraColorTexture);

            occlusionShader = Shader
                    .createFromAssets(render, "shaders/overlay.vert", "shaders/overlay.frag")
                    .setBlend(Shader.BlendFactor.SRC_ALPHA, Shader.BlendFactor.ONE_MINUS_SRC_ALPHA);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a Mesh with three vertex buffers: one for the screen coordinates (normalized device
        // coordinates), one for the camera texture coordinates (to be populated with proper data later
        // before drawing), and one for the virtual scene texture coordinates (unit texture quad)
        VertexBuffer screenCoordsVertexBuffer = new VertexBuffer(/* numberOfEntriesPerVertex=*/ 2, NDC_QUAD_COORDS_BUFFER);
        cameraTexCoordsVertexBuffer = new VertexBuffer(/*numberOfEntriesPerVertex=*/ 2, /*entries=*/ null);
        VertexBuffer virtualSceneTexCoordsVertexBuffer = new VertexBuffer(/* numberOfEntriesPerVertex=*/ 2, VIRTUAL_SCENE_TEX_COORDS_BUFFER);
        VertexBuffer[] vertexBuffers = {screenCoordsVertexBuffer, cameraTexCoordsVertexBuffer, virtualSceneTexCoordsVertexBuffer};
        mesh = new Mesh(Mesh.PrimitiveMode.TRIANGLE_STRIP, vertexBuffers);
    }

    /**
     * Updates the display geometry. This must be called every frame before calling either of
     * BackgroundRenderer's draw methods.
     *
     * @param frame The current {@code Frame} as returned by {@link Session#update()} ()}.
     */
    public void updateDisplayGeometry(Frame frame) {
        if (frame.hasDisplayGeometryChanged()) {
            // If display rotation changed (also includes view size change), we need to re-query the UV
            // coordinates for the screen rect, as they may have changed as well.
            frame.transformCoordinates2d(
                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    NDC_QUAD_COORDS_BUFFER,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    cameraTexCoords
            );
            cameraTexCoordsVertexBuffer.set(cameraTexCoords);
        }
    }

    /**
     * Draws the AR background image. The image will be drawn such that virtual content rendered with
     * the matrices provided by {@link com.google.ar.core.Camera#getViewMatrix(float[], int)} and
     * {@link com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)} will
     * accurately follow static physical objects.
     */
    public void drawBackground(Render render) {
        render.draw(mesh, backgroundShader);
    }

    /**
     * Draws the virtual scene.
     *
     * <p>Virtual content should be rendered using the matrices provided by {@link
     * com.google.ar.core.Camera#getViewMatrix(float[], int)} and {@link
     * com.google.ar.core.Camera#getProjectionMatrix(float[], int, float, float)}.
     */
    public void drawVirtualScene(Render render, Framebuffer virtualSceneFramebuffer) {
        occlusionShader.setTexture("u_VirtualSceneColorTexture", virtualSceneFramebuffer.getColorTexture());
        render.draw(mesh, occlusionShader);
    }

    /**
     * Return the camera color texture generated by this object.
     */
    public Texture getCameraColorTexture() {
        return cameraColorTexture;
    }
}
