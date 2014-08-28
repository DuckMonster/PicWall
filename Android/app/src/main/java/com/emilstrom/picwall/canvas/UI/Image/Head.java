package com.emilstrom.picwall.canvas.UI.Image;

import android.util.Log;

import com.emilstrom.math.ExtraMath;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.canvas.UI.ReplyMenu;
import com.emilstrom.picwall.helper.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-01.
 */
public class Head extends Image {
	static float COLLAPSE_DISTANCE = 2.5f, EXPAND_THRESHOLD = 1.4f, MOVE_THRESHOLD = 0.3f;

	public int headServerIndex;

	public int headIndex;
	public List<Node> nodeList = new ArrayList<Node>();

	int moveFinger = -1;
	public float nodeSpeed = 0f, nodePosition = 0f;
	Vertex2 moveOrigin;

	float expandOriginPosition, expandHeadOffset;
	float expandMinOffset, expandMaxOffset;
	float zoomOffset = 0f;

	ReplyMenu replyMenu;

	public Head(int serverID, String url, int index, Grid g) {
		super(url, g);
		headServerIndex = serverID;
		headIndex = index;
		replyMenu = new ReplyMenu(this, g);
	}
	public Head(int serverID, Texture t, int index, Grid g) {
		super(t, g);
		headServerIndex = serverID;
		headIndex = index;
		replyMenu = new ReplyMenu(this, g);
	}

	public Node addNode(String url) {
		Node n = new Node(url, this, nodeList.size(), grid);
		nodeList.add(n);
		return n;
	}
	public Node addNode(Texture t) {
		Node n = new Node(t, this, nodeList.size(), grid);
		nodeList.add(n);
		return n;
	}

	public int getNmbrOfNodes() {
		return nodeList.size();
	}

	public Node getNode(int n) {
		if (n >= nodeList.size()) return null;
		else return nodeList.get(n);
	}

	public boolean isMovingNodes() {
		return (moveFinger != -1 && moveOrigin == null);
	}

	public boolean canMove() {
		return !grid.isScaling() && !grid.isMovingGrid() && !grid.imageIsZoomed();
	}

	public boolean showReplyMenu() {
		if (isZoomed())
			return false;
		else if (isExpanded())
			return true;
		else if (getNmbrOfNodes() <= 0 && !grid.threadIsExpanded())
			return true;

		return false;
	}

	public Vertex2 getTargetPosition() {
		if (isZoomed()) {
			return new Vertex2(grid.canvas.canvasWidth * zoomOffset, 0f);
		} else {
			Vertex2 pos = new Vertex2(-grid.canvas.canvasWidth / 2 + IMAGE_OFFSET + 0.5f * grid.gridScale,
					0f - 1.2f * grid.gridScale * headIndex);

			pos.y += grid.gridPosition;
			//if (isExpanded()) pos.x = -grid.canvas.canvasWidth / 2 + expandHeadOffset * scale;
			if (isExpanded()) pos.y += .5f * scale;
			else if (grid.threadIsExpanded()) {
				if (grid.expandedHead > headIndex)
					pos.y += .5f * scale;
				else
					pos.y -= .5f * scale;

				pos.x = -grid.canvas.canvasWidth/2 + 0f * scale;
			}

			return pos;
		}
	}

	public Color getColor() {
		if (isZoomed()) return Color.WHITE;
		else if (grid.threadIsExpanded() && !isExpanded()) return new Color(1f, 1f, 1f, 0.4f);
		else return super.getColor();
	}

	public int getDepth() {
		if (isZoomed()) return 5;
		else if (isExpanded()) return 9;
		else return 10;
	}

	public boolean isExpanded() {
		return grid.expandedHead == headIndex;
	}

	public boolean canExpand() {
		return nodeList.size() > 0;
	}

	public void expand() {
		if (isExpanded() || !canExpand()) return;

		grid.expandedHead = headIndex;
		grid.centerThread(this);
		expandOriginPosition = grid.gridPosition;
		expandHeadOffset = expandMaxOffset;

		for(Node n : nodeList) if (!n.isLoaded()) n.loadTexture();
	}

	public void collapse() {
		grid.expandedHead = -1;
	}

	@Override
	public void onClick() {
		if (isExpanded() || !canExpand()) super.onClick();
		else expand();
	}

	@Override
	public void onSwipe(int dir) {
		if (isZoomed() && dir == 1) {
			getNode(0).zoom();
			zoomOffset = 1f;
		}
	}

	public void centerNode(Node n) {
		nodePosition = -1f * (n.nodeIndex+1);
	}

	public void logic() {
		super.logic();
		for(Node n : nodeList) n.logic();

		if (!isZoomed() && isExpanded())
			if (Math.abs(grid.gridPosition - expandOriginPosition) >= COLLAPSE_DISTANCE)
				collapse();

		zoomOffset += ExtraMath.minabs(-zoomOffset * 10f * Canvas.updateTime, -zoomOffset);

		replyMenu.logic();
	}

	public void checkInput(Input in) {
		super.checkInput(in);

		if (nodeList.size() <= 0) return;

		if (moveFinger == -1 && canMove() && !grid.isMovingNodes()) {
			for (int i = 0; i < Input.NMBR_OF_FINGERS; i++) {
				if (in.isPressed(i) && oldInput.isPressed(i)) {
					Vertex2 pos = in.getPosition(i),
							mypos = position;

					if (Math.abs(mypos.y - pos.y) <= 0.5f * scale || (getNmbrOfNodes() > 0 && Math.abs(nodeList.get(0).position.y - pos.y) <= 0.5f * scale)) {
						moveFinger = i;
						moveOrigin = pos;
						break;
					}
				}
			}
		}

		//MOVE NODES
		nodeSpeed -= nodeSpeed * 2.6f * Canvas.updateTime;

		if (moveFinger != -1) {
			if (!canMove() || !in.isPressed(moveFinger)) {
				moveFinger = -1;
			} else if (oldInput.isPressed(moveFinger)) {
				Vertex2 pos = in.getPosition(moveFinger),
						oldpos = oldInput.getPosition(moveFinger);

				if (moveOrigin == null || Math.abs(moveOrigin.x - pos.x) > (isExpanded() ? MOVE_THRESHOLD : EXPAND_THRESHOLD)) {
					if (moveOrigin != null) {
						moveOrigin = null;
						expand();
					}

					nodeSpeed = (pos.x - oldpos.x);
				} else nodeSpeed = 0;
			}
		}

		if (isExpanded()) {
			nodePosition += nodeSpeed / grid.gridScale;

			if (nodePosition > 0f) nodePosition = 0f;
			if (nodePosition < -1.2f * (nodeList.size() - 1)) nodePosition = -1.2f * (nodeList.size() - 1);

			expandHeadOffset += nodeSpeed / grid.gridScale;

			if (expandHeadOffset < expandMinOffset)
				expandHeadOffset = expandMinOffset;
			if (expandHeadOffset > expandMaxOffset)
				expandHeadOffset = expandMaxOffset;
		}
	}

	public void draw() {
		if (!isOnScreen()) return;

		for(Node n : nodeList) n.draw();
		super.draw();

		if (showReplyMenu()) replyMenu.draw();
	}
}
