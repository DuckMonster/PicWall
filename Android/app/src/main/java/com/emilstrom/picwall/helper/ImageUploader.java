package com.emilstrom.picwall.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.canvas.UI.Image.Head;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by DuckMonster on 2014-08-19.
 */
public class ImageUploader implements Runnable {
	public static final String uploadAdress = "http://host.patadata.se/ImageUpload.php";
	public static void uploadImage(String filename, String targetURL, int headReply, Grid g) {
		UploaderJob job = new UploaderJob(filename, targetURL, headReply);
		ImageUploader up = new ImageUploader(job, g);
		new Thread(up).start();
	}

	static class UploaderJob {
		int headReply;
		public String 	targetURL,
						filename;

		public UploaderJob(String filename, String targetURL, int headReply) {
			this.headReply = headReply;
			this.filename = filename;
			this.targetURL = targetURL;
		}
	}

	Grid grid;
	UploaderJob job;

	public ImageUploader(UploaderJob job, Grid g) {
		grid = g;
		this.job = job;
	}

	public void run() {
		compressImage(job.filename);
		uploadToServer();

		if (job.headReply != -1) {
			grid.canvas.sendNodeUploaded(grid.getHeadByServerID(job.headReply), job.targetURL);
		} else {
			grid.canvas.sendHeadUploaded(job.targetURL);
		}
	}

	public void compressImage(String filename) {
		Bitmap bmp = null;

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;
			options.inJustDecodeBounds = true;

			bmp = BitmapFactory.decodeFile(filename, options);

			int sampleSize = (int)Math.ceil((float)Math.max(options.outHeight, options.outWidth) / 1000f);
			Log.v(MainActivity.TAG, options.outWidth + "x" + options.outHeight + " | SS: " + sampleSize);
			options = new BitmapFactory.Options();

			options.inScaled = false;
			options.inSampleSize = sampleSize;

			bmp = BitmapFactory.decodeFile(filename, options);

			File f = new File(filename);
			FileOutputStream str = new FileOutputStream(f);

			bmp.compress(Bitmap.CompressFormat.JPEG, 50, str);
		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt load bitmap from " + filename);
			Log.v(MainActivity.TAG, e.toString());
		}
	}

	public void uploadToServer() {
		final String    lineEnd = "\r\n",
				twoHyphens = "--",
				boundary = "*****",
				targetURL = uploadAdress;

		final int maxBufferSize = 1 * 1024 * 1024;

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;

		File sourceFile = new File(job.filename);
		HttpURLConnection conn = null;
		DataOutputStream dos = null;

		try {
			if (!sourceFile.isFile()) {
				Log.v(MainActivity.TAG, job.filename + " isnt a file!");
				return;
			}

			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(targetURL);

			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", job.targetURL);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; " +
					"name=\"uploaded_file\";" +
					"filename=\"" + job.targetURL + "\"" + lineEnd);

			dos.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while(bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			int response = conn.getResponseCode();
			String responseMsg = conn.getResponseMessage();
			Log.v(MainActivity.TAG, "Server response: " + response + " / " + responseMsg);

			fileInputStream.close();
			dos.flush();
			dos.close();

		} catch(Exception e) {
			Log.v(MainActivity.TAG, "Couldnt upload file");
			Log.v(MainActivity.TAG, e.toString());
		}
	}
}
