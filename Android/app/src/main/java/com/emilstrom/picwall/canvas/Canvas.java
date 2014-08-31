package com.emilstrom.picwall.canvas;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import com.emilstrom.net.client.*;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.UI.Image.Head;
import com.emilstrom.picwall.helper.*;
import com.emilstrom.picwall.protocol.Protocol;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-07-30.
 */
public class Canvas implements GLSurfaceView.Renderer, IClient {
	static final Color colorList[] = {
			new Color(13, 13, 13),
			new Color(38, 38, 38),
			new Color(113, 113, 113),
			new Color(1f, 1f, 1f)
	};
	public static Color getColor(int i) {
		return new Color(colorList[i]);
	}

	public static final String IMAGE_DIRECTORY = "http://host.patadata.se/imgbank/";

	public static Canvas canvas;
	public static float updateTime = -1;

	public static TCPClient client;

	private long lastTime;

	public Grid grid;

	List<String>    cameraBuffer = new ArrayList<String>(),
					serverFilenameBuffer = new ArrayList<String>();
	List<Integer>	imageReplyBuffer = new ArrayList<Integer>();

	public Canvas() {
		canvas = this;
		client = new TCPClient(this);
	}

	public void connectToServer() {
		grid = new Grid(this);
		client.connect("host.patadata.se", 12345);
	}

	public void cameraSnap(String path, int headReply) {
		cameraBuffer.add(path);
		imageReplyBuffer.add(headReply);
		requestFilename();
	}

	public void logic() {
		calculateUpdateTime();
		client.update();

		while(cameraBuffer.size() > 0 && serverFilenameBuffer.size() > 0) {
			Log.v(MainActivity.TAG, "Trying to upload image...");

			String path = cameraBuffer.get(0),
					serverName = serverFilenameBuffer.get(0);
			int headReply = imageReplyBuffer.get(0);

			cameraBuffer.remove(0);
			serverFilenameBuffer.remove(0);
			imageReplyBuffer.remove(0);

			ImageUploader.uploadImage(path, serverName, headReply, grid);
		}

		grid.logic();
	}

	public void draw() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		grid.draw();
	}

	private void calculateUpdateTime() {
		if (updateTime == -1) {
			lastTime = System.currentTimeMillis();
		}

		long newTime = System.currentTimeMillis();
		if ((newTime - lastTime) * 0.001f > 0.5f)  //Heavy lag
			updateTime = 0f;
		else
			updateTime = (newTime - lastTime) * 0.001f;

		lastTime = newTime;
	}

	public void backButtonPressed() {
		grid.backButtonPressed();
	}

	//// Online stuff
	@Override
	public void serverConnected() {
		Log.v(MainActivity.TAG, "Connected!");
		grid.requestImage();
	}

	@Override
	public void serverMessage(MessageBuffer msg) {
		switch(msg.readWord()) {
			case Protocol.RECEIVE_HEAD:
				grid.receiveHead(msg);
				break;

			case Protocol.RECEIVE_NODE:
				grid.receiveNode(msg);
				break;

			case Protocol.RECEIVE_CLEAR:
				grid.receiveClear();
				break;

			case Protocol.RECEIVE_OPEN_THREAD:
				grid.getHeadByServerID(msg.readInt()).expand();
				break;

			case Protocol.RECEIVE_CENTER_THREAD:
				grid.centerThread(grid.getHeadByServerID(msg.readInt()));
				break;

			case Protocol.REQUEST_FILE_NAME:
				serverFilenameBuffer.add(msg.readString());
				break;
		}
	}

	@Override
	public void serverDisconnected() {
		Log.v(MainActivity.TAG, "Disconnected!");
	}

	@Override
	public void engineException(Exception e) {
		Log.v(MainActivity.TAG, e.toString());

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Log.v(MainActivity.TAG, pw.toString()); // stack trace as a string
	}

	///

	public void requestFilename() {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.REQUEST_FILE_NAME);
		client.sendMessage(msg);
	}

	public void sendHeadUploaded(String filename) {
		MessageBuffer msg = new MessageBuffer();

		msg.addWord(Protocol.HEAD_UPLOADED);
		msg.addString(filename);

		client.sendMessage(msg);
	}

	public void sendNodeUploaded(Head h, String filename) {
		MessageBuffer msg = new MessageBuffer();

		msg.addWord(Protocol.NODE_UPLOADED);
		msg.addInt(h.headServerIndex);

		msg.addString(filename);

		client.sendMessage(msg);
	}


	////
	public Camera camera;

	float[] projectionMatrix = new float[16];
	public float canvasWidth = 20, canvasHeight;

	public float[] getVPMatrix() {
		float vpMatrix[] = new float[16];
		Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, camera.getViewMatrix(), 0);

		return vpMatrix;
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		Color bg = colorList[0];
		GLES20.glClearColor(bg.r, bg.b, bg.g, 1f);
//		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glEnable(GLES20.GL_BLEND);

		camera = new Camera(this);

		Shader.generateShaders();

		connectToServer();
	}

	public void onDrawFrame(GL10 unused) {
		logic();
		draw();
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float)width / height;
		canvasHeight = canvasWidth/ratio;

		Matrix.orthoM(projectionMatrix, 0, -canvasWidth/2, canvasWidth/2, -canvasHeight/2, canvasHeight/2, 1f, 50f);
	}

	public static void setStencilDepth(int d) {
		GLES20.glStencilFunc(GLES20.GL_LEQUAL, d, 0xff);
	}
}