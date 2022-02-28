package com.jwindustries.isitvegan.graphics;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLException;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a GPU shader, the state of its associated uniforms, and some additional draw state.
 */
public class Shader implements Closeable {
    private static final String TAG = Shader.class.getSimpleName();

    /**
     * A factor to be used in a blend function.
     *
     * @see <a href="https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glBlendFunc.xhtml">glBlendFunc</a>
     */
    public enum BlendFactor {
        ZERO(GLES30.GL_ZERO),
        ONE(GLES30.GL_ONE),
        SRC_COLOR(GLES30.GL_SRC_COLOR),
        ONE_MINUS_SRC_COLOR(GLES30.GL_ONE_MINUS_SRC_COLOR),
        DST_COLOR(GLES30.GL_DST_COLOR),
        ONE_MINUS_DST_COLOR(GLES30.GL_ONE_MINUS_DST_COLOR),
        SRC_ALPHA(GLES30.GL_SRC_ALPHA),
        ONE_MINUS_SRC_ALPHA(GLES30.GL_ONE_MINUS_SRC_ALPHA),
        DST_ALPHA(GLES30.GL_DST_ALPHA),
        ONE_MINUS_DST_ALPHA(GLES30.GL_ONE_MINUS_DST_ALPHA),
        CONSTANT_COLOR(GLES30.GL_CONSTANT_COLOR),
        ONE_MINUS_CONSTANT_COLOR(GLES30.GL_ONE_MINUS_CONSTANT_COLOR),
        CONSTANT_ALPHA(GLES30.GL_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_ALPHA(GLES30.GL_ONE_MINUS_CONSTANT_ALPHA);

        /* package-private */
        final int glesEnum;

        BlendFactor(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    private int programId = 0;
    private final Map<Integer, Uniform> uniforms = new HashMap<>();
    private int maxTextureUnit = 0;

    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private final Map<Integer, String> uniformNames = new HashMap<>();

    private BlendFactor sourceRgbBlend = BlendFactor.ONE;
    private BlendFactor destRgbBlend = BlendFactor.ZERO;
    private BlendFactor sourceAlphaBlend = BlendFactor.ONE;
    private BlendFactor destAlphaBlend = BlendFactor.ZERO;

    /**
     * Constructs a {@link Shader} given the shader code.
     */
    public Shader(
            String vertexShaderCode,
            String fragmentShaderCode
    ) {
        int vertexShaderId = 0;
        int fragmentShaderId = 0;
        try {
            vertexShaderId =
                    createShader(
                            GLES30.GL_VERTEX_SHADER, prepareSourceCode(vertexShaderCode));
            fragmentShaderId =
                    createShader(
                            GLES30.GL_FRAGMENT_SHADER, prepareSourceCode(fragmentShaderCode));

            programId = GLES30.glCreateProgram();
            GLError.maybeThrowGLException("Shader program creation failed", "glCreateProgram");
            GLES30.glAttachShader(programId, vertexShaderId);
            GLError.maybeThrowGLException("Failed to attach vertex shader", "glAttachShader");
            GLES30.glAttachShader(programId, fragmentShaderId);
            GLError.maybeThrowGLException("Failed to attach fragment shader", "glAttachShader");
            GLES30.glLinkProgram(programId);
            GLError.maybeThrowGLException("Failed to link shader program", "glLinkProgram");

            final int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == GLES30.GL_FALSE) {
                String infoLog = GLES30.glGetProgramInfoLog(programId);
                GLError.maybeLogGLError(
                        Log.WARN, TAG, "Failed to retrieve shader program info log", "glGetProgramInfoLog");
                throw new GLException(0, "Shader link failed: " + infoLog);
            }
        } catch (Throwable t) {
            close();
            throw t;
        } finally {
            // Shader objects can be flagged for deletion immediately after program creation.
            if (vertexShaderId != 0) {
                GLES30.glDeleteShader(vertexShaderId);
                GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free vertex shader", "glDeleteShader");
            }
            if (fragmentShaderId != 0) {
                GLES30.glDeleteShader(fragmentShaderId);
                GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free fragment shader", "glDeleteShader");
            }
        }
    }

    /**
     * Creates a {@link Shader} from the given asset file names.
     *
     * <p>The file contents are interpreted as UTF-8 text.
     */
    public static Shader createFromAssets(
            Render render,
            String vertexShaderFileName,
            String fragmentShaderFileName
    ) throws IOException {
        AssetManager assets = render.getAssets();
        return new Shader(
                inputStreamToString(assets.open(vertexShaderFileName)),
                inputStreamToString(assets.open(fragmentShaderFileName))
        );
    }

    @Override
    public void close() {
        if (programId != 0) {
            GLES30.glDeleteProgram(programId);
            programId = 0;
        }
    }

    /**
     * Sets blending function.
     *
     * @see <a href="https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glBlendFunc.xhtml">glBlendFunc</a>
     */
    public Shader setBlend(BlendFactor sourceBlend, BlendFactor destBlend) {
        this.sourceRgbBlend = sourceBlend;
        this.destRgbBlend = destBlend;
        this.sourceAlphaBlend = sourceBlend;
        this.destAlphaBlend = destBlend;
        return this;
    }

    /**
     * Sets a texture uniform.
     */
    public Shader setTexture(String name, Texture texture) {
        // Special handling for Textures. If replacing an existing texture uniform, reuse the texture
        // unit.
        int location = getUniformLocation(name);
        Uniform uniform = uniforms.get(location);
        int textureUnit;
        if (!(uniform instanceof UniformTexture)) {
            textureUnit = maxTextureUnit++;
        } else {
            UniformTexture uniformTexture = (UniformTexture) uniform;
            textureUnit = uniformTexture.getTextureUnit();
        }
        uniforms.put(location, new UniformTexture(textureUnit, texture));
        return this;
    }

    /**
     * Sets a {@code float} uniform.
     */
    public Shader setFloat(String name, float v0) {
        float[] values = {v0};
        uniforms.put(getUniformLocation(name), new Uniform1f(values));
        return this;
    }

    /**
     * Sets a {@code vec4} uniform.
     */
    public Shader setVec4(String name, float[] values) {
        if (values.length != 4) {
            throw new IllegalArgumentException("Value array length must be 4");
        }
        uniforms.put(getUniformLocation(name), new Uniform4f(values.clone()));
        return this;
    }

    /**
     * Sets a {@code mat4} uniform.
     */
    public Shader setMat4(String name, float[] values) {
        if (values.length != 16) {
            throw new IllegalArgumentException("Value array length must be 16 (4x4)");
        }
        uniforms.put(getUniformLocation(name), new UniformMatrix4f(values.clone()));
        return this;
    }

    /**
     * Activates the shader. Don't call this directly unless you are doing low level OpenGL code;
     * instead, prefer {@link Render#draw}.
     */
    public void lowLevelUse() {
        // Make active shader/set uniforms
        if (programId == 0) {
            throw new IllegalStateException("Attempted to use freed shader");
        }
        GLES30.glUseProgram(programId);
        GLError.maybeThrowGLException("Failed to use shader program", "glUseProgram");
        GLES30.glBlendFuncSeparate(
                sourceRgbBlend.glesEnum,
                destRgbBlend.glesEnum,
                sourceAlphaBlend.glesEnum,
                destAlphaBlend.glesEnum);
        GLError.maybeThrowGLException("Failed to set blend mode", "glBlendFuncSeparate");
        GLES30.glDepthMask(false);
        GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLError.maybeThrowGLException("Failed to disable depth test", "glDisable");
        try {
            // Remove all non-texture uniforms from the map after setting them, since they're stored as
            // part of the program.
            ArrayList<Integer> obsoleteEntries = new ArrayList<>(uniforms.size());
            for (Map.Entry<Integer, Uniform> entry : uniforms.entrySet()) {
                try {
                    entry.getValue().use(entry.getKey());
                    if (!(entry.getValue() instanceof UniformTexture)) {
                        obsoleteEntries.add(entry.getKey());
                    }
                } catch (GLException e) {
                    String name = uniformNames.get(entry.getKey());
                    throw new IllegalArgumentException("Error setting uniform `" + name + "'", e);
                }
            }
            uniforms.keySet().removeAll(obsoleteEntries);
        } finally {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to set active texture", "glActiveTexture");
        }
    }

    private interface Uniform {
        void use(int location);
    }

    private static class UniformTexture implements Uniform {
        private final int textureUnit;
        private final Texture texture;

        public UniformTexture(int textureUnit, Texture texture) {
            this.textureUnit = textureUnit;
            this.texture = texture;
        }

        public int getTextureUnit() {
            return textureUnit;
        }

        @Override
        public void use(int location) {
            if (texture.getTextureId() == 0) {
                throw new IllegalStateException("Tried to draw with freed texture");
            }
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + textureUnit);
            GLError.maybeThrowGLException("Failed to set active texture", "glActiveTexture");
            GLES30.glBindTexture(texture.getTarget().glesEnum, texture.getTextureId());
            GLError.maybeThrowGLException("Failed to bind texture", "glBindTexture");
            GLES30.glUniform1i(location, textureUnit);
            GLError.maybeThrowGLException("Failed to set shader texture uniform", "glUniform1i");
        }
    }

    private static class Uniform1f implements Uniform {
        private final float[] values;

        public Uniform1f(float[] values) {
            this.values = values;
        }

        @Override
        public void use(int location) {
            GLES30.glUniform1fv(location, values.length, values, 0);
            GLError.maybeThrowGLException("Failed to set shader uniform 1f", "glUniform1fv");
        }
    }

    private static class Uniform4f implements Uniform {
        private final float[] values;

        public Uniform4f(float[] values) {
            this.values = values;
        }

        @Override
        public void use(int location) {
            GLES30.glUniform4fv(location, values.length / 4, values, 0);
            GLError.maybeThrowGLException("Failed to set shader uniform 4f", "glUniform4fv");
        }
    }

    private static class UniformMatrix4f implements Uniform {
        private final float[] values;

        public UniformMatrix4f(float[] values) {
            this.values = values;
        }

        @Override
        public void use(int location) {
            GLES30.glUniformMatrix4fv(location, values.length / 16, /*transpose=*/ false, values, 0);
            GLError.maybeThrowGLException("Failed to set shader uniform matrix 4f", "glUniformMatrix4fv");
        }
    }

    private int getUniformLocation(String name) {
        Integer locationObject = uniformLocations.get(name);
        if (locationObject != null) {
            return locationObject;
        }
        int location = GLES30.glGetUniformLocation(programId, name);
        GLError.maybeThrowGLException("Failed to find uniform", "glGetUniformLocation");
        if (location == -1) {
            throw new IllegalArgumentException("Shader uniform does not exist: " + name);
        }
        uniformLocations.put(name, location);
        uniformNames.put(location, name);
        return location;
    }

    private static int createShader(int type, String code) {
        int shaderId = GLES30.glCreateShader(type);
        GLError.maybeThrowGLException("Shader creation failed", "glCreateShader");
        GLES30.glShaderSource(shaderId, code);
        GLError.maybeThrowGLException("Shader source failed", "glShaderSource");
        GLES30.glCompileShader(shaderId);
        GLError.maybeThrowGLException("Shader compilation failed", "glCompileShader");

        final int[] compileStatus = new int[1];
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == GLES30.GL_FALSE) {
            String infoLog = GLES30.glGetShaderInfoLog(shaderId);
            GLError.maybeLogGLError(
                    Log.WARN, TAG, "Failed to retrieve shader info log", "glGetShaderInfoLog");
            GLES30.glDeleteShader(shaderId);
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free shader", "glDeleteShader");
            throw new GLException(0, "Shader compilation failed: " + infoLog);
        }

        return shaderId;
    }

    private static String prepareSourceCode(String sourceCode) {
        return sourceCode.replaceAll("(?m)^(\\s*#\\s*version\\s+.*)$", "$1\n");
    }

    private static String inputStreamToString(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, UTF_8.name());
        char[] buffer = new char[1024 * 4];
        StringBuilder builder = new StringBuilder();
        int amount;
        while ((amount = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, amount);
        }
        reader.close();
        return builder.toString();
    }
}
