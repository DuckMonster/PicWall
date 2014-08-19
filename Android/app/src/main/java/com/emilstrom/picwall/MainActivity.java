package com.emilstrom.picwall;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class MainActivity extends Activity {
	public static final String TAG = "PicWallTag";
	public static MainActivity context;

	static final int RESULT_CAMERA = 100, RESULT_IMAGE_PICKER = 101;

	GLSurface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    context = this;

	    surface = new GLSurface(this);

        setContentView(surface);
    }

	@Override
	public void onBackPressed() {
		surface.backButtonPressed();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_CAMERA) {
			if (resultCode == RESULT_OK) {
				File f = getTempFile();
				try {
					surface.canvas.cameraSnap(f.getPath());
					Log.v(TAG, "Filed saved at " + f.getPath());
				} catch(Exception e) {
					Log.v(TAG, "Something went wrong :(");
				}
			} else if (resultCode == RESULT_CANCELED)
				Log.v(TAG, "Camera was quit");
			else
				Log.v(TAG, "Something failed with the camera...");
		}

		if (requestCode == RESULT_IMAGE_PICKER) {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();

				String[] filePathColumn = {MediaStore.Images.Media.DATA};

				Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);

				cursor.close();

				context.surface.canvas.cameraSnap(filePath);
			}
		}
	}

	public void launchCameraIntent() {
		Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		in.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
		startActivityForResult(in, RESULT_CAMERA);
	}

	public void launchImagePickerIntent() {
		Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(in, RESULT_IMAGE_PICKER);
	}

	public File getTempFile() {
		final File f = new File(Environment.getExternalStorageDirectory(), this.getPackageName());
		if (!f.exists()) f.mkdir();

		return new File(f, "image.tmp");
	}

	public void uploadFileToServer(String fileName, String targetFilename) {
		final String    lineEnd = "\r\n",
						twoHyphens = "--",
						boundary = "*****",
						targetURL = "http://host.patadata.se/ImageUpload.php";

		final int maxBufferSize = 1 * 1024 * 1024;

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;

		File sourceFile = new File(fileName);
		HttpURLConnection conn = null;
		DataOutputStream dos = null;

		try {
			if (!sourceFile.isFile()) {
				Log.v(TAG, fileName + " isnt a file!");
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
			conn.setRequestProperty("uploaded_file", targetFilename);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; " +
					"name=\"uploaded_file\";" +
					"filename=\"" + targetFilename + "\"" + lineEnd);

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
			Log.v(TAG, "Server response: " + response + " / " + responseMsg);

			fileInputStream.close();
			dos.flush();
			dos.close();

		} catch(Exception e) {
			Log.v(TAG, "Couldnt upload file");
			Log.v(TAG, e.toString());
		}
	}
}
