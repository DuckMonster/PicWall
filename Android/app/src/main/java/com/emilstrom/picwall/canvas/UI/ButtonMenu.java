package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.Vertex2;

/**
 * Created by Emil on 2014-08-12.
 */
public class ButtonMenu extends Button {
	public ButtonMenu(Vertex2 pos, int icon, Grid g) {
		super(pos, icon, g);
	}

	public void onClick() {
		super.onClick();
		if (grid.mainMenu.show) grid.mainMenu.hideMenu();
		else grid.mainMenu.showMenu();
	}
}
