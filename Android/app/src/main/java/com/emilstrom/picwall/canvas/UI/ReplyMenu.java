package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.R;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.canvas.UI.Image.Head;
import com.emilstrom.picwall.helper.Color;
import com.emilstrom.picwall.helper.Mesh;
import com.emilstrom.picwall.helper.Vertex2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-19.
 */
public class ReplyMenu extends UIElement {
	public static float menuHeight = 3f, buttonPadding = 0.4f;

	static class MenuButton extends Button {
		ReplyMenu menu;
		int buttonIndex;

		public MenuButton(ButtonAction onClick, int index, int icon, ReplyMenu menu, Grid g) {
			super(onClick, new Vertex2(), icon, menuHeight - buttonPadding*2, g);

			this.menu = menu;
			buttonIndex = index;
		}

		@Override
		public Vertex2 getTargetPosition() {
			Vertex2 pos = new Vertex2(menu.getPosition());
			pos.x -= menu.getWidth()/2;
			pos.x += buttonPadding + size/2 + (buttonPadding + size) * buttonIndex;

			return pos;
		}

		@Override
		public int getDepth() {
			return menu.getDepth()-1;
		}

        @Override
        public void onClick() {
            if (menu.head.showReplyMenu()) super.onClick();
        }
	}

	Mesh menuMesh;

	Head head;
	public Vertex2 position;

	List<MenuButton> buttonList = new ArrayList<MenuButton>();

	public ReplyMenu(Head h, Grid g) {
		super(g);
		menuMesh = new Mesh(g.canvas);
		head = h;

		position = getTargetPosition();

		addButton(
				new Button.ButtonAction() {
					public void execute() {
						MainActivity.context.launchCameraIntent(head.headServerIndex);
					}
				},
				R.drawable.camera
		);

		addButton(
				new Button.ButtonAction() {
					public void execute() {
						MainActivity.context.launchImagePickerIntent(head.headServerIndex);
					}
				},
				R.drawable.gallery
		);
	}

	public void addButton(Button.ButtonAction action, int icon) {
		buttonList.add(new MenuButton(action, buttonList.size(), icon, this, grid));
	}

	public Vertex2 getPosition() {
		return position;
	}
	public Vertex2 getTargetPosition() {
		Vertex2 pos = new Vertex2(head.getTargetPosition());
		pos.x += head.scale / 2 + 0.5f + getWidth()/2;

		return pos;
	}

	public float getWidth() {
		return (buttonPadding * (buttonList.size()+1) + (menuHeight-buttonPadding*2) * buttonList.size());
	}

	@Override
	public int getDepth() {
		return 11 + head.getNmbrOfNodes() + 2;
	}

	public void logic() {
		Vertex2 target = getTargetPosition();
		Vertex2 dif = target.minus(position);

		position.add(dif.times(35f * Canvas.updateTime));
		position = getTargetPosition();

		for (MenuButton b : buttonList) b.logic();
	}

	public void draw() {
		Canvas.setStencilDepth(getDepth());

		menuMesh.reset();

		Color c = Canvas.getColor(1);
		c.a = 0.4f;
		menuMesh.setColor(c);
		menuMesh.translate(getPosition());
		menuMesh.scale(getWidth(), menuHeight, 1f);

		menuMesh.draw();

		for(MenuButton b : buttonList) b.draw();
	}
}
