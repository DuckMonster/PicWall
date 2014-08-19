package com.emilstrom.picwall.helper;

import android.opengl.Matrix;
import android.util.Log;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.Canvas;

/**
 * Created by Emil on 2014-02-19.
 */
public class InputHelper {
	static boolean pressed[] = new boolean[Input.NMBR_OF_FINGERS];
	static float x[] = new float[Input.NMBR_OF_FINGERS], y[] = new float[Input.NMBR_OF_FINGERS];

	public static void setPressed(int id, boolean p) {
		if (id >= Input.NMBR_OF_FINGERS) return;

		pressed[id] = p;
	}
	public static void setPosition(int id, float xx, float yy) {
		if (id >= Input.NMBR_OF_FINGERS) return;

		x[id] = xx; y[id] = yy;
	}

	public static Input getInput() {
		float[] retx = new float[Input.NMBR_OF_FINGERS],
				rety = new float[Input.NMBR_OF_FINGERS];

		for(int i=0; i<Input.NMBR_OF_FINGERS; i++) {
			if (pressed[i]) {
				float gamePos[] = getGameCoords(x[i], y[i]);
				retx[i] = gamePos[0];
				rety[i] = gamePos[1];
			} else {
				retx[i] = 0;
				rety[i] = 0;
			}
		}

		return new Input(retx, rety, pressed);
	}

	public static float[] getGameCoords(float x, float y) {
		if (Canvas.canvas.camera == null) {
			return new float[]{0, 0};
		}

		float coords[] = {x, y, 0f, 1f},
				invVP[] = new float[16];

		Matrix.invertM(invVP, 0, Canvas.canvas.getVPMatrix(), 0);

		Matrix.multiplyMV(coords, 0, invVP, 0, coords, 0);
//		Log.v(MainActivity.TAG, "x: " + Float.toString(x) + " | y: " + Float.toString(y));
//		Log.v(MainActivity.TAG, "x: " + Float.toString(coords[0]) + " | y: " + Float.toString(coords[1]));
//		Log.v(MainActivity.TAG, "-----");

		return new float[]{coords[0], coords[1]};
	}
}