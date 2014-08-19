package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.picwall.R;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.Color;
import com.emilstrom.picwall.helper.Mesh;
import com.emilstrom.picwall.helper.TextureLoader;

/**
 * Created by Emil on 2014-08-12.
 */
public class Menu {
	public static float menuWidth = 8f;

	Grid grid;
	Mesh menuMesh;

	float menuPosition = 0f;
	boolean show = false;

	public Menu(Grid g) {
		grid = g;
		menuMesh = new Mesh(g.canvas);
	}

	public void showMenu() {
		show = true;
	}

	public void hideMenu() {
		show = false;
	}

	public float getTargetMenuPosition() {
		return show ? 1f : 0f;
	}

	public void logic() {
		float dif = getTargetMenuPosition() - menuPosition;
		menuPosition += dif * 10f * Canvas.updateTime;
	}

	public void draw() {
		Canvas.setStencilDepth(0);

		menuMesh.setColor(Canvas.getColor(1));

		menuMesh.reset();

		menuMesh.translate(grid.canvas.canvasWidth/2 + menuWidth/2, 0f, 0f);
		menuMesh.translate(-menuWidth * menuPosition, 0f, 0f);
		menuMesh.scale(menuWidth, grid.canvas.canvasHeight, 1f);

		menuMesh.draw();
	}
}
