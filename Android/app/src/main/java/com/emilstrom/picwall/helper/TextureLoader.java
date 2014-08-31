package com.emilstrom.picwall.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.emilstrom.picwall.MainActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-08.
 */
public class TextureLoader implements Runnable {
	static final int NMBR_OF_LOADERS = 5;

	static TextureLoader loaderList[];
	static List<LoaderJob> 		imageJobList = new ArrayList<LoaderJob>(),
								animJobList = new ArrayList<LoaderJob>();

	static void initURLLoaderList(int nmbr) {
		loaderList = new TextureLoader[nmbr];
		for(int i=0; i<loaderList.length; i++)
			loaderList[i] = new TextureLoader(i != loaderList.length-1 ? TYPE_GIF : TYPE_JPG);
	}

	static void addJobToList(LoaderJob job) {
		if (job.type == TYPE_JPG) imageJobList.add(job);
		else animJobList.add(job);
	}

	public static Texture loadTextureFromURL(String url) {
		if (loaderList == null) initURLLoaderList(NMBR_OF_LOADERS);

		Texture t = new Texture(Texture.TEXTURE_URL);
		addJobToList(new LoaderJob(t, url));

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
		if (loaderList == null) initURLLoaderList(NMBR_OF_LOADERS);

		TextureAnimator t = new TextureAnimator();
		addJobToList(new LoaderJob(t, url));

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
		Texture targetTexture;
		TextureAnimator targetTextureAnimator;
		String targetURL = "";
		int type;

		public LoaderJob(Texture target, String url) {
			type = TYPE_JPG;

			this.targetTexture = target;
			this.targetURL = url;
		}
		public LoaderJob(TextureAnimator target, String url) {
			type = TYPE_GIF;

			this.targetTextureAnimator = target;
			this.targetURL = url;
		}
	}

	static class JobFetcher {
		public synchronized LoaderJob fetchJob(int type) {
			List<LoaderJob> jobList = (type == TYPE_JPG ? imageJobList : animJobList);

			if (jobList.size() > 0) {
				LoaderJob job = jobList.get(0);
				jobList.remove(0);

				return job;
			} else
				return null;
		}
	}

	static class BitmapScaler {
		public synchronized Bitmap scaleBitmap(Bitmap b, int maxSize, Texture targetTexture) {
			Bitmap sb = null;

			try {
				targetTexture.srcWidth = b.getWidth();
				targetTexture.srcHeight = b.getHeight();

				float exp = (float) (Math.log(Math.max(targetTexture.srcWidth, targetTexture.srcHeight)) / Math.log(2));
				exp = (float) Math.ceil(exp);
				exp = Math.min(maxSize, exp);          //MAX SIZE

				sb = Bitmap.createScaledBitmap(b, (int) Math.pow(2, exp), (int) Math.pow(2, exp), false);

				targetTexture.texWidth = sb.getWidth();
				targetTexture.texHeight = sb.getHeight();

				targetTexture.generateScaleValues();

				if (sb != b) {
					b.recycle();
					b = null;
				}

				Thread.sleep(5);
			} catch(Exception e) {
				Log.v(MainActivity.TAG, "Couldnt scale picture");
				Log.v(MainActivity.TAG, e.toString());
			}

			return sb;
		}
	}

	public static final int TYPE_JPG =0, TYPE_GIF =1;
	static final JobFetcher jobFetcher = new JobFetcher();
	static final BitmapScaler bitmapScaler = new BitmapScaler();

	List<LoaderJob> jobBuffer = new ArrayList<LoaderJob>();
	int loaderType = 0;

	public TextureLoader(int type) {
		loaderType = type;

		Thread t = new Thread(this);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	public void run() {
		while(true) {
			LoaderJob j = jobFetcher.fetchJob(loaderType);

			if (j != null) {
				if (j.type == TYPE_JPG)
					loadJob(j);
				else if (j.type == TYPE_GIF)
					loadGIFJob(j);
			}

			try {Thread.sleep(50);} catch (Exception e) {}
		}
	}

	public void loadJob(LoaderJob job) {
		Log.v(MainActivity.TAG, "Beginning to load " + job.targetURL);

		Bitmap bmp = null;

		try {
			URL url = new URL(job.targetURL);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;

			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);

			conn.disconnect();

			bmp = bitmapScaler.scaleBitmap(bmp, Texture.STANDARD_MAX_SIZE, job.targetTexture);
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
		Log.v(MainActivity.TAG, "Beginning to load " + job.targetURL);

		try {
			MainActivity.printMemoryUsage();

			URL url = new URL(job.targetURL);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			GifDecoder decoder = new GifDecoder();
			decoder.read(conn.getInputStream());

			conn.disconnect();

			int nmbrOfFrames = decoder.getFrameCount();
			job.targetTextureAnimator.setNmbrOfFrames(nmbrOfFrames);

			int loadedFrames = 0;

			for(int i=0; i<nmbrOfFrames; i++) {
				MainActivity.printMemoryUsage();

				Texture t = new Texture(Texture.TEXTURE_URL);

				Bitmap b = decoder.getFrame(i);
				b = bitmapScaler.scaleBitmap(b, Texture.REDUCED_MAX_SIZE, t);

				if (b != null) {
					t.loadedBitmapBuffer = b;
					t.loaded = true;

					int delay = decoder.getDelay(i);

					job.targetTextureAnimator.addTexture(t, delay);
					loadedFrames++;
				}

				Runtime.getRuntime().gc();
				try{ Thread.sleep(400); } catch (Exception e) {}
			}

			job.targetTextureAnimator.setNmbrOfFrames(loadedFrames);

			Log.v(MainActivity.TAG, "Finished loading " + job.targetURL);
			MainActivity.printMemoryUsage();
		} catch(Exception e) {
			Log.v(MainActivity.TAG, e.toString());
			Log.v(MainActivity.TAG, "Couldnt load " + job.targetURL);
		}
	}
}