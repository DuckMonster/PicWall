package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.Vertex2;

/**
 * Created by Emil on 2014-08-15.
 */
public class UIElement {
	public Grid grid;

	public UIElement(Grid g) {
		grid = g;
		grid.uiElementList.add(this);
	}

	public int getDepth() {
		return 10;
	}

	public Vertex2 getPosition() {
		return new Vertex2();
	}

	public Vertex2 getSize() {
		return new Vertex2();
	}

	public boolean collidesWith(Vertex2 pos) {
		Vertex2 dif = pos.minus(getPosition()),
				size = getSize();

		return (Math.abs(dif.x) < size.x/2 && Math.abs(dif.y) < size.y/2);
	}

	public boolean isClicked(Vertex2 pos) {
		return grid.getClickedElement(pos) == this;
	}
}
