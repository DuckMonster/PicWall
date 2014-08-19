package com.emilstrom.picwall.canvas;

import android.opengl.Matrix;
import com.emilstrom.picwall.helper.Vertex3;

/**
 * Created by Emil on 2014-07-31.
 */
public class Camera {
	Canvas canvas;
	public Vertex3 position;

	public Camera(Canvas c) {
		canvas = c;
		position = new Vertex3(0f, 0f, 9f);
	}

	public float[] getViewMatrix() {
		float matrix[] = new float[16];
		Matrix.setLookAtM(matrix, 0, position.x, position.y, position.z, position.x, position.y, 0f, 0f, 1f, 0f);

		return matrix;
	}

	public void logic() {
	}

	public void draw() {
	}
}
