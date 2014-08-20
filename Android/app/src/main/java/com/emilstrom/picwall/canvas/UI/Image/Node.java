package com.emilstrom.picwall.canvas.UI.Image;

import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.*;

/**
 * Created by Emil on 2014-08-01.
 */
public class Node extends Image {
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

	public Vertex2 getTargetPosition() {
		if (isZoomed()) {
			return new Vertex2(grid.canvas.canvasWidth * head.zoomOffset, 0f);
		} else {
			float s = grid.gridScale;

			Vertex2 pos = new Vertex2(
					-grid.canvas.canvasWidth / 2 + IMAGE_OFFSET + .5f * s +
					(1.2f + 1.2f * nodeIndex) * s * (isExpanded() ? 1f : 0.1f),

					0f - 1.2f * s * head.headIndex
			);

			if (isExpanded()) {
				pos.x -= 1f * s;
				pos.x += head.nodePosition * s;
				pos.y -= .5f * s;
			} else if (grid.threadIsExpanded()) {
				if (grid.expandedHead > head.headIndex)
					pos.y += .5f * scale;
				else
					pos.y -= .5f * scale;

				pos.x = -grid.canvas.canvasWidth/2 - 0.5f * scale;
			}

			pos.y += grid.gridPosition;

			return pos;
		}
	}

	@Override
	public Color getColor() {
		if (isZoomed()) return Color.WHITE;
		else if (grid.threadIsExpanded() && !isExpanded()) return new Color(1f, 1f, 1f, 0.4f - 0.2f*nodeIndex);
		else if (!isExpanded()) return new Color(1f, 1f, 1f, 0.5f - 0.25f*nodeIndex);
		else return super.getColor();
	}

	@Override
	public int getDepth() {
		if (isZoomed()) return 5;
		else if (isExpanded()) return 9;
		else return 11 + nodeIndex;
	}

	public void logic() {
		super.logic();

        if (!isExpanded()) clickFingerBuffer = -1;
	}

	public void draw() {
		Canvas.setStencilDepth(getDepth());

		mesh.setColor(getColor());

		mesh.reset();
		mesh.translate(position);
		mesh.scale(scale);

		mesh.scale(wscale, hscale, 1f);
		mesh.draw();
	}
}
