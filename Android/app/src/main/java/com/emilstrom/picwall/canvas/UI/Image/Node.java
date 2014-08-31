package com.emilstrom.picwall.canvas.UI.Image;

import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.*;

/**
 * Created by Emil on 2014-08-01.
 */
public class Node extends Image {
	public static float NODE_PADDING = 1.2f, NODE_PADDING_SHOWCASE = 0.3f;

	Head head;
	public int nodeIndex;

	public Node(String url, Head head, int index, Grid g) {
		super(url, g);

		this.head = head;
		nodeIndex = index;
	}
	public Node(Texture t, Head head, int index, Grid g) {
		super(t, g);

		this.head = head;
		nodeIndex = index;
	}

	public boolean isExpanded() {
		return head.isExpanded();
	}

	@Override
	public void onClick() {
		if (isExpanded() || !head.canExpand()) super.onClick();
		else head.expand();
	}

	@Override
	public void onSwipe(int dir) {
		if (isZoomed()) {
			int nextIndex = nodeIndex + dir;

			if (nextIndex >= 0) {
				Node n = head.getNode(nextIndex);
				if (n != null) {
					n.zoom();
					head.zoomOffset = dir;
				}
			} else {
				head.zoom();
				head.zoomOffset = dir;
			}
		}
	}

	@Override
	public void zoom() {
		head.centerNode(this);
		super.zoom();
	}

	@Override
	public Vertex2 getTargetPosition() {
		if (isZoomed()) {
			return new Vertex2(grid.canvas.canvasWidth * head.zoomOffset, 0f);
		} else {
			float s = grid.gridScale;
			Vertex2 pos;

			if (!isExpanded()) {
				pos = head.getTargetPosition();
				pos.x += 0.3f * s;
				pos.y -= 0.4f * s;

				pos.x += NODE_PADDING_SHOWCASE * nodeIndex * s;
			} else {
				pos = new Vertex2(
						-grid.canvas.canvasWidth / 2 + 0.8f * s + head.nodePosition * s,
						-1.2f * grid.gridScale * head.headIndex + grid.gridPosition - .5f * s
				);

				pos.x += NODE_PADDING * nodeIndex * s;
			}

			if (grid.threadIsExpanded() && !isExpanded()) {
				pos.x -= 2f * s;
			}

			return pos;
		}
	}

	@Override
	public float getTargetScale() {
		float s = super.getTargetScale();
		if (!isExpanded()) s *= 0.5f - 0.2f * nodeIndex;

		return s;
	}

	@Override
	public Color getColor() {
		if (isZoomed()) return Color.WHITE;
		else if (grid.threadIsExpanded() && !isExpanded()) return new Color(1f, 1f, 1f, 1f);
		else if (!isExpanded()) return new Color(1f, 1f, 1f, 1f);
		else return super.getColor();
	}

	@Override
	public int getDepth() {
		if (isZoomed()) return 5;
		else if (isExpanded()) return 9;
		else return 10 + nodeIndex;
	}

	public void logic() {
		super.logic();

        if (!isExpanded()) clickFingerBuffer = -1;
	}
}
