package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.math.ExtraMath;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.R;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.Color;
import com.emilstrom.picwall.helper.Mesh;
import com.emilstrom.picwall.helper.Texture;
import com.emilstrom.picwall.helper.Vertex2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-19.
 */
public class TopMenu extends UIElement {
	public static float menuHeight = 4f, buttonPadding = 0.4f;

	private static class MenuButton extends Button {
		static final int ALIGN_LEFT = 0, ALIGN_RIGHT = 1;

		public int buttonAlign;
		int buttonIndex;
		TopMenu menu;

		public MenuButton(ButtonAction onClick, int align, int index, int icon, TopMenu menu, Grid g) {
			super(onClick, new Vertex2(), icon, menuHeight - buttonPadding*2, g);
			this.menu = menu;
			buttonIndex = index;
			buttonAlign = align;
		}

		@Override
		public Vertex2 getTargetPosition() {
			switch (buttonAlign) {
				case ALIGN_LEFT:
					return new Vertex2(
							-grid.canvas.canvasWidth / 2 + buttonPadding + size / 2 + (buttonPadding + size) * buttonIndex,
							menu.getPosition().y
					);

				case ALIGN_RIGHT:
					return new Vertex2(
							+grid.canvas.canvasWidth / 2 - buttonPadding - size / 2 - (buttonPadding + size) * buttonIndex,
							menu.getPosition().y
					);

				default: return new Vertex2();
			}
		}
	}

	Mesh menuMesh;
	List<MenuButton> buttonList = new ArrayList<MenuButton>();
	float menuPosition = 1f;

	public TopMenu(Grid g) {
		super(g);
		menuMesh = new Mesh(g.canvas);
		addButton(
				new Button.ButtonAction() {
					public void execute() {
						MainActivity.context.launchCameraIntent(-1);
					}
				},
				MenuButton.ALIGN_LEFT,
				R.drawable.camera
		);

		addButton(
				new Button.ButtonAction() {
					public void execute() {
						MainActivity.context.launchImagePickerIntent(-1);
					}
				},
				MenuButton.ALIGN_LEFT,
				R.drawable.gallery
		);

		addButton(
				new Button.ButtonAction () {
					public void execute() {
						grid.canvas.connectToServer();
					}
				},
				MenuButton.ALIGN_RIGHT,
				R.drawable.refresh
		);
	}

	public void addButton(Button.ButtonAction action, int align, int icon) {
		buttonList.add(new MenuButton(action, align, getButtonsAligned(align), icon, this, grid));
	}

	public int getButtonsAligned(int align) {
		int n = 0;
		for(MenuButton b : buttonList) if (b.buttonAlign == align) n++;

		return n;
	}

	public Vertex2 getPosition() {
		return new Vertex2(0f, grid.canvas.canvasHeight/2 + menuHeight/2 - menuHeight * menuPosition);
	}
	public boolean menuIsVisible() {
		return !grid.threadIsExpanded();
	}

	public void logic() {
		float dif;
		if (menuIsVisible()) {
			dif = 1f - menuPosition;
		} else {
			dif = 0f - menuPosition;
		}

		menuPosition += ExtraMath.minabs(dif, dif * 10f * Canvas.updateTime);

		for(MenuButton b : buttonList) b.logic();
	}

	public void draw() {
		Canvas.setStencilDepth(3);

		menuMesh.reset();

		Color c = Canvas.getColor(1);
		c.a = 0.4f;

		menuMesh.setColor(c);
		menuMesh.translate(getPosition());
		menuMesh.scale(grid.canvas.canvasWidth, menuHeight, 1f);

		menuMesh.draw();

		for(MenuButton b : buttonList) b.draw();
	}
}
