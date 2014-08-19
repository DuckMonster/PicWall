package com.emilstrom.picwall.canvas.UI;

import android.opengl.GLES20;
import com.emilstrom.picwall.MainActivity;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.*;

/**
 * Created by Emil on 2014-08-09.
 */
public class Button extends UIElement {
	Vertex2 position, targetPosition;
	Mesh mesh;
	float scale = 3f;

	Input oldInput;

	Timer clickedTimer = new Timer(0.5f, true);

	public Button(Vertex2 pos, int icon, Grid g) {
		super(g);
		position = new Vertex2();
		targetPosition = pos;
		mesh = new Mesh(g.canvas);
	}

	@Override
	public Vertex2 getPosition() {
		return position;
	}

	@Override
	public Vertex2 getSize() {
		return new Vertex2(scale, scale);
	}

	@Override
	public int getDepth() {
		return 2;
	}

	public void onClick() {
		clickedTimer.reset();
	}

	public Vertex2 getTargetPosition() {
		//return new Vertex2(grid.canvas.canvasWidth/2 - 1.2f, -grid.canvas.canvasHeight/2 + 1.2f);
		return targetPosition;
	}

	public void logic() {
		clickedTimer.logic();

		position = getTargetPosition();

		Input in = InputHelper.getInput();
		if (oldInput == null) oldInput = in;

		for(int i=0; i<Input.NMBR_OF_FINGERS; i++)
			if (!in.isPressed(i) && oldInput.isPressed(i)) {
				if (collidesWith(oldInput.getPosition(i)) && isClicked(oldInput.getPosition(i))) onClick();
			}

		oldInput = in;
	}

	public void draw() {
		Canvas.setStencilDepth(getDepth());

		if (clickedTimer.isDone())
			mesh.setColor(Canvas.colorList[1]);
		else {
			float f = (float)Math.pow(6, -clickedTimer.percentageDone() * 5f);
			mesh.setColor(Color.blend(Canvas.colorList[1], Canvas.colorList[2], f));
		}

		mesh.reset();

		mesh.translate(position);
		mesh.scale(scale);

		mesh.draw();
	}
}