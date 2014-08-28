package com.emilstrom.picwall.helper;

import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.Canvas;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-08.
 */
public class TextureLoader implements Runnable {
	static TextureLoader loaderList[];
	static int loaderIndex = 0;

	static void initURLLoaderList(int nmbr) {
		loaderList = new TextureLoader[nmbr];
		for(int i=0; i<loaderList.length; i++)
			loaderList[i] = new TextureLoader();
	}

	static void addJobToLoader(LoaderJob job) {
		loaderList[loaderIndex].addJob(job);
		loaderIndex = (loaderIndex + 1) % loaderList.length;
	}

	public static Texture loadTextureFromURL(String url) {
		if (loaderList == null) initURLLoaderList(3);

		Texture t = new Texture(Texture.TEXTURE_URL);
		addJobToLoader(new LoaderJob(t, url));

		return t;
	}

	public static Texture loadTextureFromFile(String filePath) {
		Bitmap b = loadBitmapFromFile(filePath);
		return loadTexture(b);
	}

	public static Texture loadTextureFromResource(int resourceID) {
		Bitmap b = loadBitmapFromResource(resourceID);
		return loadTexture(b);
	}

	public static Texture loadTexture(Bitmap b) {
		return new Texture(Texture.TEXTURE_RESOURCE, b);
	}

	public static TextureAnimator loadTextureAnimationFromURL(String url) {
		if (loaderList == null) initURLLoaderList(3);

		TextureAnimator t = new TextureAnimator();
		addJobToLoader(new LoaderJob(t, url));

		return t;
	}

	public static Bitmap loadBitmapFromFile(String filePath) {
		Bitmap bmp = null;

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			options.inSampleSize = 3;

			bmp = BitmapFactory.decodeFile(filePath, options);
		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt load bitmap from " + filePath);
			Log.v(MainActivity.TAG, e.toString());
		}

		if (bmp != null) Log.v(MainActivity.TAG, "Bitmap was loaded from file " + filePath + "!");
		return bmp;
	}

	public static Bitmap loadBitmapFromResource(int resourceID) {
		Bitmap bmp = null;

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;

			bmp = BitmapFactory.decodeResource(MainActivity.context.getResources(), resourceID,options);
		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt load bitmap from resource");
			Log.v(MainActivity.TAG, e.toString());
		}


		if (bmp != null) Log.v(MainActivity.TAG, "Bitmap was loaded from resources!");
		return bmp;
	}


	///////////////////

	static class LoaderJob {
		static int TYPE_IMAGE, TYPE_ANIM;

		Texture targetTexture;
		TextureAnimator targetTextureAnimator;
		String targetURL = "";
		int type;

		public LoaderJob(Texture target, String url) {
			type = TYPE_IMAGE;

			this.targetTexture = target;
			this.targetURL = url;
		}
		public LoaderJob(TextureAnimator target, String url) {
			type = TYPE_ANIM;

			this.targetTextureAnimator = target;
			this.targetURL = url;
		}
	}

	List<LoaderJob> jobBuffer = new ArrayList<LoaderJob>();

	public TextureLoader() {
		Thread t = new Thread(this);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	public void run() {
		while(true) {
			if (jobBuffer.size() > 0) {
				if (jobBuffer.get(0).targetTexture != null)
					loadJob(jobBuffer.get(0));
				else if (jobBuffer.get(0).targetTextureAnimator != null)
					loadGIFJob(jobBuffer.get(0));

				jobBuffer.remove(0);
			}

			try {Thread.sleep(50);} catch (Exception e) {}
		}
	}

	public void addJob(LoaderJob job) {
		jobBuffer.add(job);
	}

	public void loadJob(LoaderJob job) {
		Bitmap bmp = null;

		try {
			URL url = new URL(job.targetURL);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;

			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);

			conn.disconnect();

			bmp = job.targetTexture.setScaledBitmap(bmp);
		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt load " + job.targetURL);
			job.targetTexture.loadingFailed = true;
		}

		if (bmp != null) {
			job.targetTexture.loadedBitmapBuffer = bmp;
			job.targetTexture.loaded = true;

			Log.v(MainActivity.TAG, "Finished loading " + job.targetURL);
		} else job.targetTexture.loadingFailed = true;
	}

	public void loadGIFJob(LoaderJob job) {
		try {
			URL url = new URL(job.targetURL);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			GifDecoder decoder = new GifDecoder();
			decoder.read(conn.getInputStream());

			for(int i=0; i<decoder.getFrameCount(); i++) {
				Texture t = new Texture(Texture.TEXTURE_URL);

				Bitmap b = decoder.getFrame(i);
				b = t.setScaledBitmap(b);

				if (b != null) {
					t.loadedBitmapBuffer = b;
					t.loaded = true;

					job.targetTextureAnimator.addTexture(t, decoder.getDelay(i));
				}
			}

			conn.disconnect();
		} catch(Exception e) {
			Log.v(MainActivity.TAG, e.toString());
			Log.v(MainActivity.TAG, "Couldnt load " + job.targetURL);
		}
	}
}
