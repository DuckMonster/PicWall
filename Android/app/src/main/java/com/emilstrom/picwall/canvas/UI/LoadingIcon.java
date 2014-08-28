package com.emilstrom.picwall.canvas.UI;

import com.emilstrom.math.ExtraMath;
import com.emilstrom.picwall.R;
import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.UI.Image.Image;
import com.emilstrom.picwall.helper.*;

/**
 * Created by Emil on 2014-08-28.
 */
public class LoadingIcon {
	Image image;

	Mesh mesh;
	Texture loadingTexture;

	float rotation, targetRotation;
	Timer rotateTimer = new Timer(0.75f, false);

	Vertex2 rectangle = new Vertex2(0f, 0f), targetRectangle;
	Timer rectangleTimer = new Timer(0.3f, true);

	public LoadingIcon(Image i) {
		image = i;
		loadingTexture = TextureLoader.loadTextureFromResource(R.drawable.hourglass);
		mesh = new Mesh(i.grid.canvas);
	}

	public void logic() {
		rotateTimer.logic();
		rectangleTimer.logic();
		if (rotateTimer.isDone()) {
			targetRotation += 180f;
			rotateTimer.reset();
		}
		if (rectangleTimer.isDone()) {
			targetRectangle = new Vertex2(
					(float)ExtraMath.getRndDouble(0.2f, 1f),
					(float)ExtraMath.getRndDouble(0.2f, 1f)
			);
			rectangleTimer.reset();
		}

		{
			float dif = targetRotation - rotation;
			rotation += dif * 5f * Canvas.updateTime;
		}

		{
			Vertex2 dif = targetRectangle.minus(rectangle);
			rectangle.add(dif.times(5f * Canvas.updateTime));
		}
	}

	public void draw() {
		Canvas.setStencilDepth(image.getDepth());

		mesh.setColor(new Color(1f, 1f, 1f, 0.3f));

		mesh.reset();

		mesh.translate(image.getPosition());
		mesh.scale(image.scale);
		mesh.scale(rectangle.x, rectangle.y, 1f);
		mesh.scale(0.8f);

		mesh.draw();


		mesh.setColor(new Color(1f, 1f, 1f, 0.7f));

		mesh.reset();

		mesh.translate(image.getPosition());
		mesh.scale(image.scale);
		mesh.scale(loadingTexture.scaleWidth, loadingTexture.scaleHeight, 0f);
		mesh.rotate(rotation, 0, 0, 1f);

		mesh.draw(loadingTexture);
	}
}
