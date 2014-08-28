package com.emilstrom.picwall.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.R;

/**
 * Created by Emil on 2014-07-31.
 */
public class Texture {
	public static final int TEXTURE_URL=0, TEXTURE_RESOURCE=1;

	public static Texture	loadingTexture = TextureLoader.loadTextureFromResource(R.drawable.loading),
							failedTexture = TextureLoader.loadTextureFromResource(R.drawable.loadingfailed),
							blankTexture = TextureLoader.loadTextureFromResource(R.drawable.blank);

	//HANDLES

	int handle_texture;

	//

	public int textureType;

	public boolean loaded = false, loadingFailed = false;
	Bitmap loadedBitmapBuffer = null;

	public int srcWidth, srcHeight, texWidth, texHeight;
	public float scaleWidth=1f, scaleHeight=1f;

	public Texture(int textureType) {
		this.textureType = textureType;
	}
	public Texture(int textureType, Bitmap b) {
		this.textureType = textureType;
		b = setScaledBitmap(b);
		setLoadedBitmap(b);
	}

	public Bitmap setScaledBitmap(Bitmap b) {
		Bitmap sb = null;

		try {
			srcWidth = b.getWidth();
			srcHeight = b.getHeight();

			float exp = (float) (Math.log(Math.max(srcWidth, srcHeight)) / Math.log(2));
			exp = (float) Math.floor(exp);
			exp = Math.min(9f, exp);          //MAX SIZE

			sb = Bitmap.createScaledBitmap(b, (int) Math.pow(2, exp), (int) Math.pow(2, exp), false);

			Log.v(MainActivity.TAG, "Scaling image..." + b.getWidth() + "x" + b.getHeight() + " -> " + sb.getWidth() + "x" + sb.getWidth());

			texWidth = sb.getWidth();
			texHeight = sb.getHeight();

			generateScaleValues();

			if (sb != b) b.recycle();
		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt scale picture");
			Log.v(MainActivity.TAG, e.toString());
		}

		return sb;
	}

	public void textureLoaded(Bitmap bmp) {
		final int[] tex = new int[1];

		GLES20.glGenTextures(1, tex, 0);

		try {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);

			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

			bmp.recycle();
		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt upload bitmap data to GL");
			Log.v(MainActivity.TAG, e.toString());
		}

		handle_texture = tex[0];
		generateScaleValues();
	}

	public void setLoadedBitmap(Bitmap b) {
		loadedBitmapBuffer = b;
		loaded = true;
	}

	public void generateScaleValues() {
		float w, h;

		if (srcWidth > srcHeight) {
			w = 1f;
			h = (float) srcHeight / srcWidth;
		} else {
			h = 1f;
			w = (float) srcWidth / srcHeight;
		}

		scaleWidth = w;
		scaleHeight = h;
	}

	public void upload() {
		if (loadingFailed) {
			failedTexture.upload();
			return;
		}

		if (!loaded) {
			loadingTexture.upload();
			return;
		}

		if (loadedBitmapBuffer != null) {
			textureLoaded(loadedBitmapBuffer);
			loadedBitmapBuffer = null;
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle_texture);
	}
}
