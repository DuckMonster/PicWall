package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.Vertex2;

/**
 * Created by Emil on 2014-08-12.
 */
public class ButtonCamera extends Button {
	public ButtonCamera(Vertex2 pos, int icon, Grid g) {
		super(pos, icon, g);
	}

	@Override
	public void onClick() {
		super.onClick();

		MainActivity.context.launchCameraIntent();
	}
}
