package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.helper.*;

/**
 * Created by Emil on 2014-08-09.
 */
public class Button extends UIElement {
	public static final float iconPadding = 0.3f;
	public static interface ButtonAction {
		public void execute();
	}

	ButtonAction onClickAction;

	Vertex2 position, targetPosition;
	Mesh buttonMesh;
	float size = 3f;

	Input oldInput;

	Timer clickedTimer = new Timer(0.5f, true);

	Texture iconTexture;

	public Button(Vertex2 pos, int icon, float size, Grid g) {
		super(g);
		position = new Vertex2();
		targetPosition = pos;
		buttonMesh = new Mesh(g.canvas);
		iconTexture = TextureLoader.loadTextureFromResource(icon);
		this.size = size;
	}
	public Button(ButtonAction onClick, Vertex2 pos, int icon, float size, Grid g) {
		super(g);
		onClickAction = onClick;
		position = new Vertex2();
		targetPosition = pos;
		buttonMesh = new Mesh(g.canvas);
		iconTexture = TextureLoader.loadTextureFromResource(icon);
		this.size = size;
	}

	@Override
	public Vertex2 getPosition() {
		return position;
	}

	@Override
	public Vertex2 getSize() {
		return new Vertex2(size, size);
	}

	@Override
	public int getDepth() {
		return 2;
	}

	public void onClick() {
		if (onClickAction != null) onClickAction.execute();
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

		for(int i=0; i<Input.NMBR_OF_FINGERS; i++) {
			if (in.isPressed(i) && collidesWith(in.getPosition(i)) && isClicked(in.getPosition(i)))
				clickedTimer.reset();

            if (!in.isPressed(i) && oldInput.isPressed(i)) {
                if (collidesWith(oldInput.getPosition(i)) && isClicked(oldInput.getPosition(i)))
                    onClick();
            }
        }

		oldInput = in;
	}

	public void draw() {
		Canvas.setStencilDepth(getDepth());

		if (clickedTimer.isDone())
			buttonMesh.setColor(Canvas.getColor(1));
		else {
			float f = (float)Math.pow(6, -clickedTimer.percentageDone() * 5f);
			buttonMesh.setColor(Color.blend(Canvas.getColor(1), Canvas.getColor(2), f));
		}

		buttonMesh.reset();

		buttonMesh.translate(position);
		buttonMesh.scale(size);

		buttonMesh.translate(0f, 0f, -getDepth() - 0.1f);

		buttonMesh.draw();

		if (iconTexture != null) {
			buttonMesh.setColor(Color.WHITE);

			buttonMesh.reset();

			buttonMesh.translate(position);
			buttonMesh.scale(size * (1f - iconPadding));
			buttonMesh.scale(iconTexture.scaleWidth, iconTexture.scaleHeight, 1f);

			buttonMesh.draw(iconTexture);
		}
	}
}