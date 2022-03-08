package com.jwindustries.isitvegan.graphics;

import android.opengl.GLES30;
import android.util.Log;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * A collection of vertices, faces, and other attributes that define how to render a 3D object.
 *
 * <p>To render the mesh, use {@link Render#draw(Mesh, Shader)}.
 */
public class Mesh implements Closeable {
    /**
     * The kind of primitive to render.
     *
     * <p>This determines how the data in {@link VertexBuffer}s are interpreted.
     * See <a href="https://www.khronos.org/opengl/wiki/Primitive">here</a> for more on how
     * primitives behave.
     */
    public enum PrimitiveMode {
        POINTS(GLES30.GL_POINTS),
        LINE_STRIP(GLES30.GL_LINE_STRIP),
        LINE_LOOP(GLES30.GL_LINE_LOOP),
        LINES(GLES30.GL_LINES),
        TRIANGLE_STRIP(GLES30.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(GLES30.GL_TRIANGLE_FAN),
        TRIANGLES(GLES30.GL_TRIANGLES);

        /* package-private */
        final int glesEnum;

        PrimitiveMode(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    private final int[] vertexArrayId = {0};
    private final PrimitiveMode primitiveMode;
    private final VertexBuffer[] vertexBuffers;

    /**
     * Construct a {@link Mesh}.
     *
     * <p>The ordering of the {@code vertexBuffers} is significant. Their array indices will
     * correspond to their attribute locations, which must be taken into account in shader code. The
     * <a href="https://www.khronos.org/opengl/wiki/Layout_Qualifier_(GLSL)">layout qualifier</a> must
     * be used in the vertex shader code to explicitly associate attributes with these indices.
     */
    public Mesh(
            PrimitiveMode primitiveMode,
            VertexBuffer[] vertexBuffers
    ) {
        if (vertexBuffers == null || vertexBuffers.length == 0) {
            throw new IllegalArgumentException("Must pass at least one vertex buffer");
        }

        this.primitiveMode = primitiveMode;
        this.vertexBuffers = vertexBuffers;

        try {
            // Create vertex array
            GLES30.glGenVertexArrays(1, vertexArrayId, 0);
            GLError.maybeThrowGLException("Failed to generate a vertex array", "glGenVertexArrays");

            // Bind vertex array
            GLES30.glBindVertexArray(vertexArrayId[0]);
            GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");

            for (int i = 0; i < vertexBuffers.length; ++i) {
                // Bind each vertex buffer to vertex array
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffers[i].getBufferId());
                GLError.maybeThrowGLException("Failed to bind vertex buffer", "glBindBuffer");
                GLES30.glVertexAttribPointer(i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_FLOAT, false, 0, 0);
                GLError.maybeThrowGLException("Failed to associate vertex buffer with vertex array", "glVertexAttribPointer");
                GLES30.glEnableVertexAttribArray(i);
                GLError.maybeThrowGLException("Failed to enable vertex buffer", "glEnableVertexAttribArray");
            }
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    /**
     * Constructs a rectangle {@link Mesh} from the given points.
     */
    public static Mesh createRectangleFromPoints(float[] pointA, float[] pointB, float[] pointC, float[] pointD) {
        // number of vertices per point, points per triangle, triangles, byte size of floats
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(3 * 3 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(new float[]{
                pointA[0], pointA[1], pointA[2],
                pointB[0], pointB[1], pointB[2],
                pointC[0], pointC[1], pointC[2],

                pointA[0], pointA[1], pointA[2],
                pointD[0], pointD[1], pointD[2],
                pointC[0], pointC[1], pointC[2],
        });

        VertexBuffer[] vertexBuffers = {new VertexBuffer(3, floatBuffer)};
        return new Mesh(PrimitiveMode.TRIANGLES, vertexBuffers);
    }

    /**
     * Constructs a circle {@link Mesh} within the box specified by the parameters
     */
    public static Mesh createCircle(float top, float right, float bottom, float left, float depth) {
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;
        int numberOfPoints = 64;
        float[][] circlePoints = getCirclePoints(numberOfPoints, top, right, bottom, left, depth);

        float[] vertices = new float[numberOfPoints * 3 * 3];

        // Circle slice
        for (int i = 0; i < numberOfPoints; i++) {
            vertices[i * 3 * 3 + 0] = circlePoints[i][0];
            vertices[i * 3 * 3 + 1] = circlePoints[i][1];
            vertices[i * 3 * 3 + 2] = circlePoints[i][2];

            vertices[i * 3 * 3 + 3] = circlePoints[(i + 1) % numberOfPoints][0];
            vertices[i * 3 * 3 + 4] = circlePoints[(i + 1) % numberOfPoints][1];
            vertices[i * 3 * 3 + 5] = circlePoints[(i + 1) % numberOfPoints][2];

            vertices[(i * 3 * 3 + 6) % (numberOfPoints * 3 * 3)] = centerX;
            vertices[(i * 3 * 3 + 7) % (numberOfPoints * 3 * 3)] = depth;
            vertices[(i * 3 * 3 + 8) % (numberOfPoints * 3 * 3)] = centerY;
        }
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(numberOfPoints * 3 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(vertices);

        VertexBuffer[] vertexBuffers = {new VertexBuffer(3, floatBuffer)};
        return new Mesh(PrimitiveMode.TRIANGLES, vertexBuffers);
    }

    private static float[][] getCirclePoints(int numberOfPoints, float top, float right, float bottom, float left, float depth) {
        double angleDelta = Math.toRadians(360. / numberOfPoints);
        float width = right - left;
        float height = bottom - top;
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;

        float[][] results = new float[numberOfPoints][3];

        for (int i = 0; i < numberOfPoints; i++) {
            double angle = angleDelta * i;

            float x = (float) Math.cos(angle) * width + centerX;
            float y = (float) Math.sin(angle) * height + centerY;
            float[] result = new float[]{x, depth, y};
            results[i] = result;
        }
        return results;
    }

    @Override
    public void close() {
        if (vertexArrayId[0] != 0) {
            GLES30.glDeleteVertexArrays(1, vertexArrayId, 0);
            GLError.maybeLogGLError(Log.WARN, "Mesh", "Failed to free vertex array object", "glDeleteVertexArrays");
        }
    }

    /**
     * Draws the mesh. Don't call this directly unless you are doing low level OpenGL code; instead,
     * prefer {@link Render#draw}.
     */
    public void lowLevelDraw() {
        if (vertexArrayId[0] == 0) {
            throw new IllegalStateException("Tried to draw a freed Mesh");
        }

        GLES30.glBindVertexArray(vertexArrayId[0]);
        GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");

        // Sanity check for debugging
        int numberOfVertices = vertexBuffers[0].getNumberOfVertices();
        for (int i = 1; i < vertexBuffers.length; ++i) {
            if (vertexBuffers[i].getNumberOfVertices() != numberOfVertices) {
                throw new IllegalStateException("Vertex buffers have mismatching numbers of vertices");
            }
        }
        GLES30.glDrawArrays(primitiveMode.glesEnum, 0, numberOfVertices);
        GLError.maybeThrowGLException("Failed to draw vertex array object", "glDrawArrays");

    }
}
