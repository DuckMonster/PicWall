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

import com.emilstrom.picwall.canvas.Canvas;

import org.apache.http.protocol.HTTP;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class MainActivity extends Activity {
	public static final String TAG = "PicWallTag";
	public static MainActivity context;

	public static final int RESULT_CAMERA = 100, RESULT_IMAGE_PICKER = 101;

	int imageReplyBuffer = -1;

	GLSurface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    context = this;

	    surface = new GLSurface(this);

        setContentView(surface);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		Canvas.client.disconnect();
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
					surface.canvas.cameraSnap(
							f.getPath(),
							imageReplyBuffer
					);
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

				context.surface.canvas.cameraSnap(
						filePath,
						imageReplyBuffer
				);
			}
		}
	}

	public void launchCameraIntent(int replyHead) {
		Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		in.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
		in.putExtra("image_reply", replyHead);
		imageReplyBuffer = replyHead;

		startActivityForResult(in, RESULT_CAMERA);
	}

	public void launchImagePickerIntent(int replyHead) {
		Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		in.putExtra("image_reply", replyHead);
		imageReplyBuffer = replyHead;

		startActivityForResult(in, RESULT_IMAGE_PICKER);
	}

	public File getTempFile() {
		final File f = new File(Environment.getExternalStorageDirectory(), this.getPackageName());
		if (!f.exists()) f.mkdir();

		return new File(f, "image.tmp");
	}
}
