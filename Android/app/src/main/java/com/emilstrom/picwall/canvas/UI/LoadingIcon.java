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
	static Texture loadingTexture = TextureLoader.loadTextureFromResource(R.drawable.hourglass);
	Image image;

	Mesh mesh;

	float rotation, targetRotation;
	Timer rotateTimer = new Timer(0.75f, false);

	public Vertex2 rectangle = new Vertex2(0f, 0f), targetRectangle;
	Timer rectangleTimer = new Timer(0.3f, true);

	Timer fadeTimer = new Timer(0.4f, false);

	public LoadingIcon(Image i) {
		image = i;
		mesh = new Mesh(i.grid.canvas);
	}

	public void logic() {
		if (fadeTimer.isDone()) return;

		if (image.isLoaded()) {
			fadeTimer.logic();
			rectangle.x = image.wscale;
			rectangle.y = image.hscale;
		}

		rotateTimer.logic();
		rectangleTimer.logic();
		if (rotateTimer.isDone()) {
			targetRotation += 180f;
			rotateTimer.reset();
		}
		if (rectangleTimer.isDone()) {
			targetRectangle = new Vertex2(
					(float)ExtraMath.getRndDouble(0.2f, 0.8f),
					(float)ExtraMath.getRndDouble(0.2f, 0.8f)
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
		if (fadeTimer.isDone()) return;

		Canvas.setStencilDepth(image.getDepth());

		Color c = Canvas.getColor(1);
		c.a = 0.6f - 0.6f * fadeTimer.percentageDone();
		mesh.setColor(c);

		mesh.reset();

		mesh.translate(image.getPosition());
		mesh.scale(image.scale);
		mesh.scale(rectangle.x, rectangle.y, 1f);

		mesh.draw();


		c = Canvas.getColor(3);
		c.a = 1f - fadeTimer.percentageDone();
		mesh.setColor(c);

		mesh.reset();

		mesh.translate(image.getPosition());
		mesh.scale(image.scale);
		mesh.scale(loadingTexture.scaleWidth, loadingTexture.scaleHeight, 0f);
		mesh.rotate(rotation, 0, 0, 1f);

		mesh.draw(loadingTexture);
	}
}
