package com.emilstrom.picwall.helper;

import android.opengl.GLES20;
import android.util.Log;
import com.emilstrom.picwall.MainActivity;

/**
 * Created by Emil on 2014-07-30.
 */
public class Shader {
	private final static String vertexSource =
			"uniform mat4 u_mvp;" +
			"attribute vec3 a_position;" +
			"attribute vec2 a_texPosition;" +
			"varying vec2 v_texPosition;" +
			"void main() {" +
					"v_texPosition = a_texPosition;" +
					"gl_Position = u_mvp * vec4(a_position, 1.0);" +
			"}";

	private final static String fragmentSource =
			"precision mediump float;" +
			"uniform sampler2D u_texture;" +
			"uniform vec4 u_color;" +
			"varying vec2 v_texPosition;" +
			"void main() {" +
					"vec4 color = texture2D(u_texture, v_texPosition) * u_color;" +
					"if (color.a > 0.0) {" +
						"gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0) * texture2D(u_texture, v_texPosition) * u_color;" +
					"} else {" +
						"discard;" +
					"}" +
			"}";

	public static int vertexShader, fragmentShader;
	public static int program;

	public static void generateShaders() {
		vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		program = createProgram(vertexShader, fragmentShader);
	}

	public static int loadShader(int shaderType, String source) {
		int s = GLES20.glCreateShader(shaderType);

		GLES20.glShaderSource(s, source);
		GLES20.glCompileShader(s);

		Log.v(MainActivity.TAG, "Shader debug: " + GLES20.glGetShaderInfoLog(s));

		return s;
	}

	public static int createProgram(int vShader, int fShader) {
		int p = GLES20.glCreateProgram();

		GLES20.glAttachShader(p, vShader);
		GLES20.glAttachShader(p, fShader);

		GLES20.glLinkProgram(p);

		return p;
	}
}
