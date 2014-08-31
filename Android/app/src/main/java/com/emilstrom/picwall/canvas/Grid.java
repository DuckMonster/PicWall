package com.emilstrom.picwall.canvas;

import android.opengl.GLES20;
import android.util.Log;
import com.emilstrom.net.client.MessageBuffer;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.R;
import com.emilstrom.picwall.canvas.UI.Image.Head;
import com.emilstrom.picwall.canvas.UI.Image.Image;
import com.emilstrom.picwall.canvas.UI.Image.Node;
import com.emilstrom.picwall.canvas.UI.*;
import com.emilstrom.picwall.helper.*;
import com.emilstrom.picwall.protocol.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-01.
 */
public class Grid {
	public static final float MIN_SCALE = 6f, MAX_SCALE = 13f, ZOOMED_BACKGROUND_ALPHA = 0.8f, MOVE_THRESHOLD = 0.6f, MOVE_THRESHOLD_EXPANDED = 0.8f;

	public Canvas canvas;

	public float gridSpeed, gridPosition;
	public float gridScale = MIN_SCALE;
	int moveFinger = -1;
	Vertex2 moveOrigin;

	//SCALING
	int scaleFingers[] = { -1, -1 };
	float scaleFingerStart = 0f, scaleBufferValue = 0f, scaleOffset = 0f;
	Vertex2 scaleFingerOrigin;

	//

	Input oldInput;

	List<Head> headList = new ArrayList<Head>();
	public int expandedHead = -1;
	Image zoomedImage = null;
	public Image zoomedBufferImage = null;

	Mesh zoomedBackgroundMesh;
	float zoomedBackgroundAlpha = 0f;

	//UI
	public List<UIElement> uiElementList = new ArrayList<UIElement>();

	public Menu mainMenu;
	public TopMenu topMenu;

	public Grid(Canvas c) {
		canvas = c;
		zoomedBackgroundMesh = new Mesh(c);

		mainMenu = new Menu(this);
		topMenu = new TopMenu(this);
	}

	public Head getHead(int headIndex) {
		return headList.get(headIndex);
	}

	public Head getHeadByServerID(int id) {
		for(Head h : headList) if (h.headServerIndex == id) return h;

		return null;
	}

	public int addHead(int serverID, String url) {
		Head h = new Head(serverID, url, headList.size(), this);
		headList.add(h);

		return h.headIndex;
	}

	public int addHead(int serverID, Texture t) {
		Head h = new Head(serverID, t, headList.size(), this);
		headList.add(h);

		return h.headIndex;
	}

	public Node addNodeToHead(int h, String url) { return addNodeToHead(headList.get(h), url); }
	public Node addNodeToHead(Head h, String url) {
		return h.addNode(url);
	}

	public Node addNodeToHead(int h, Texture t) { return addNodeToHead(headList.get(h), t); }
	public Node addNodeToHead(Head h, Texture t) {
		return h.addNode(t);
	}

	public boolean isScaling() {
		return scaleFingers[0] != -1 && scaleFingers[1] != -1;
	}

	public boolean isMovingNodes() {
		if (expandedHead == -1) return false;

		return headList.get(expandedHead).isMovingNodes();
	}

	public boolean isMovingGrid() {
		return (moveFinger != -1 && moveOrigin == null);
	}

	public boolean imageIsZoomed() {
		return (zoomedImage != null || zoomedBufferImage != null);
	}

	public boolean threadIsExpanded() {
		return expandedHead != -1;
	}

	public boolean allowMove() {
		return !isScaling() && !isMovingNodes() && !imageIsZoomed();
	}

	public void centerThread(Head h) {
		gridPosition = (h.headIndex*1.2f) * gridScale;
	}

	public UIElement getClickedElement(Vertex2 pos) {
		UIElement elem = null;
		int elemDepth = -1;

		for(UIElement e : uiElementList)
			if ((e.getDepth() <= elemDepth || elemDepth == -1) && e.collidesWith(pos)) {
				elem = e;
				elemDepth = e.getDepth();
			}

		return elem;
	}

	public void backButtonPressed() {
		if (imageIsZoomed())
			zoomedBufferImage = null;
		else if (threadIsExpanded())
			expandedHead = -1;
	}

	public void logic() {
		Input input = InputHelper.getInput();
		if (oldInput == null) oldInput = input;

		//Move canvas
		gridSpeed -= gridSpeed * 2.6 * Canvas.updateTime;

		if (moveFinger == -1 && allowMove()) {
			for(int i=0; i<Input.NMBR_OF_FINGERS; i++) {
				if (input.isPressed(i) && oldInput.isPressed(i)) {
					moveOrigin = input.getPosition(i);
					moveFinger = i;
					break;
				}
			}
		} else if (moveFinger != -1) {
			if (!allowMove() || !input.isPressed(moveFinger)) {
				moveFinger = -1;
			} else {
				Vertex2 pos = input.getPosition(moveFinger),
						oldPos = oldInput.getPosition(moveFinger);

				if (moveOrigin == null || Math.abs(moveOrigin.y - pos.y) > (threadIsExpanded() ? MOVE_THRESHOLD_EXPANDED : MOVE_THRESHOLD)) {
					if (moveOrigin != null){
						moveOrigin = null;
					}

					gridSpeed = (pos.y - oldPos.y);
				} else gridSpeed = 0;
			}
		}

		if (isMovingNodes()) gridSpeed = 0;
		gridPosition += gridSpeed;

		//

		////Scale canvas
		//Check for fingers
		if (scaleFingers[0] == -1 || scaleFingers[1] == -1) {
			for (int i = 0; i < Input.NMBR_OF_FINGERS; i++) {
				if (input.isPressed(i)) {
					if (scaleFingers[0] == -1 && scaleFingers[1] != i)
						scaleFingers[0] = i;

					if (scaleFingers[1] == -1 && scaleFingers[0] != i)
						scaleFingers[1] = i;
				}
			}

			if (isScaling()) {
				Vertex2 pos1 = input.getPosition(scaleFingers[0]),
						pos2 = input.getPosition(scaleFingers[1]);

				float len = Vertex2.getLength(pos1, pos2);

				scaleFingerStart = len;
				scaleBufferValue = gridScale;
				scaleFingerOrigin = pos1.plus(Vertex2.getDirectionVertex(pos1, pos2).times(len/2));
				scaleFingerOrigin.y *= -1;

				scaleOffset = scaleFingerOrigin.y;

				scaleFingerOrigin.y += gridPosition;
				scaleFingerOrigin.y /= gridScale;

				gridSpeed = 0f;
			}
		}

		if (scaleFingers[0] != -1 && !input.isPressed(scaleFingers[0])) scaleFingers[0] = -1;
		if (scaleFingers[1] != -1 && !input.isPressed(scaleFingers[1])) scaleFingers[1] = -1;
		if (isScaling()) {
			float len = Vertex2.getLength(input.getPosition(scaleFingers[0]), input.getPosition(scaleFingers[1]));
			gridScale = scaleBufferValue + (len - scaleFingerStart);

			if (gridScale < MIN_SCALE) gridScale = MIN_SCALE;
			if (gridScale > MAX_SCALE) gridScale = MAX_SCALE;

			gridPosition = (scaleFingerOrigin.y - scaleOffset / gridScale) * gridScale;
		}

		//

		oldInput = input;

		if (imageIsZoomed())
			zoomedBackgroundAlpha += (ZOOMED_BACKGROUND_ALPHA - zoomedBackgroundAlpha) * 3.5 * Canvas.updateTime;
		else
			zoomedBackgroundAlpha -= zoomedBackgroundAlpha * 3.5 * Canvas.updateTime;

		if (zoomedBackgroundAlpha > 0.8f) zoomedBackgroundAlpha = 0.8f;
		if (zoomedBackgroundAlpha < 0f) zoomedBackgroundAlpha = 0f;

		for(Head i : headList) if (i != null) i.logic();

		mainMenu.logic();
		topMenu.logic();
		zoomedImage = zoomedBufferImage;
	}

	public void draw() {
		GLES20.glEnable(GLES20.GL_STENCIL_TEST);

		GLES20.glStencilMask(0xff);
		GLES20.glClearStencil(100);
		GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);
		GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_REPLACE);

		for(Head i : headList) if (i != null) i.draw();

		if (zoomedBackgroundAlpha > 0f) {
			Canvas.setStencilDepth(9);

			zoomedBackgroundMesh.setColor(new Color(0f, 0f, 0f, zoomedBackgroundAlpha));

			zoomedBackgroundMesh.reset();
			zoomedBackgroundMesh.scale(canvas.canvasWidth, canvas.canvasHeight, 0f);
			zoomedBackgroundMesh.draw();
		}

		if (!Canvas.client.isConnected()) {
			Canvas.setStencilDepth(1);

		}

		mainMenu.draw();
		topMenu.draw();
	}

	//Server communications
	public void requestImage() {
		MessageBuffer msg = new MessageBuffer();
		msg.addWord(Protocol.REQUEST_IMAGE);

		Canvas.client.sendMessage(msg);
	}

	public void receiveHead(MessageBuffer msg) {
		int serverID = msg.readInt();
		int headID = addHead(serverID, Canvas.IMAGE_DIRECTORY + msg.readString());

		//And then the nodes
		while(msg.getSize() > 0) {
			Node n = addNodeToHead(headID, Canvas.IMAGE_DIRECTORY + msg.readString());
		}
	}

	public void receiveNode(MessageBuffer msg) {
		int headID = msg.readInt();
		Head h = getHeadByServerID(headID);

		if (h == null) {
			Log.v(MainActivity.TAG, "Recieved node for a head I dont have....");
			return;
		}

		Node n = addNodeToHead(h, Canvas.IMAGE_DIRECTORY + msg.readString());
		n.loadTexture();
	}

	public void receiveClear() {
		headList.clear();
	}
}