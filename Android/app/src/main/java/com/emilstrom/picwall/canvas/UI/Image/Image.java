package com.emilstrom.picwall.canvas.UI.Image;

import com.emilstrom.picwall.canvas.Canvas;
import com.emilstrom.picwall.canvas.Grid;
import com.emilstrom.picwall.canvas.UI.UIElement;
import com.emilstrom.picwall.helper.*;

/**
 * Created by Emil on 2014-07-31.
 */
public class Image extends UIElement {
	public static final float IMAGE_OFFSET = 0.5f, imageSpeed = 35f;

	public Mesh mesh;

	String imageURL = "";

	public Vertex2 position;
	public float scale;
	float wscale = 1f, hscale = 1f;

	int readyClickFinger = -1;

	Input oldInput;

	public Image(String url, Grid g) {
		super(g);
		init();

		imageURL = url;
	}
	public Image(Texture t, Grid g) {
		super(g);
		init();

		mesh.tex = t;
	}

	public void init() {
		mesh = new Mesh(grid.canvas);

		position = new Vertex2();
	}

	@Override
	public Vertex2 getPosition() {
		return position;
	}

	@Override
	public Vertex2 getSize() {
		return new Vertex2(scale * wscale, scale * hscale);
	}

	@Override
	public int getDepth() {
		if (isZoomed()) return 5;
		else return 10;
	}

	public void loadTexture() {
		if (mesh.tex.textureType == Texture.TEXTURE_URL && (isLoaded() || imageURL.equals(""))) return;

		mesh.tex = TextureLoader.loadTextureFromURL(imageURL);
	}

	public boolean isLoaded() {
		if (mesh.tex == null) return true;
		else if (mesh.tex.textureType != Texture.TEXTURE_URL) return false;
		else return mesh.tex.loaded;
	}

	public boolean isOnScreen() {
		return Math.abs(position.y) - hscale * scale < grid.canvas.canvasHeight/2;
	}

	public Vertex2 getTargetPosition() {
		return new Vertex2(0, 0);
	}

	public float getTargetScale() {
		if (isZoomed()) {
			return grid.canvas.canvasWidth / mesh.tex.scaleWidth - 1f;
		}
		else return grid.gridScale;
	}

	public Color getColor() {
		return Color.WHITE;
	}

	public void zoom() {
		if (grid.zoomedBufferImage == this) grid.zoomedBufferImage = null;
		else grid.zoomedBufferImage = this;
	}

	public boolean isZoomed() {
		return grid.zoomedBufferImage == this;
	}

	public void onClick() {
		if (!grid.imageIsZoomed() || isZoomed()) {
			zoom();
		}
	}

	public void logic() {
		{
			Vertex2 targetPosition = getTargetPosition(),
					dif = Vertex2.subtract(targetPosition, position);

			position.add(dif.times(Canvas.updateTime * imageSpeed));
		}

		//Scale to target scale

		if (!grid.isScaling()) {
			float dif = getTargetScale() - scale;
			scale += dif * 20f * Canvas.updateTime;
		} else scale = getTargetScale();

		//Scale to texture scale
		if (wscale != mesh.tex.scaleWidth) {
			float dif = mesh.tex.scaleWidth - wscale;
			if (Math.abs(dif) < 0.01) wscale = mesh.tex.scaleWidth;
			else wscale += dif * 20f * Canvas.updateTime;
		}

		if (hscale != mesh.tex.scaleHeight) {
			float dif = mesh.tex.scaleHeight - hscale;
			if (Math.abs(dif) < 0.01) hscale = mesh.tex.scaleHeight;
			else hscale += dif * 20f * Canvas.updateTime;
		}

		Input in = InputHelper.getInput();
		if (oldInput == null) oldInput = in;

		checkInput(in);

		oldInput = in;

		if (grid.isMovingGrid() || grid.isScaling() || grid.isMovingNodes())
			readyClickFinger = -1;
	}

	public void checkInput(Input in) {
		//Check for clicked
		for(int i=0; i<Input.NMBR_OF_FINGERS; i++) {
			if (in.isPressed(i) && !oldInput.isPressed(i)) {
				if (collidesWith(in.getPosition(i))) {
					readyClickFinger = i;
				}
			}

			if (!in.isPressed(i)) {
				if (readyClickFinger == i && oldInput.isPressed(i)) {
					Vertex2 pos = oldInput.getPosition(i);

					if (collidesWith(pos) && isClicked(pos)) {
						onClick();
					}
				}

				if (readyClickFinger == i) readyClickFinger = -1;
			}
		}
	}

	public void draw() {
		Canvas.setStencilDepth(getDepth());

		mesh.setColor(getColor());

		mesh.reset();
		mesh.translate(position);
		mesh.scale(scale);

		mesh.scale(wscale, hscale, 1f);
		mesh.draw();
	}
}