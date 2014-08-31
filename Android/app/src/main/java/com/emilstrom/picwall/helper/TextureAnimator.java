package com.emilstrom.picwall.helper;

import com.emilstrom.picwall.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 2014-08-27.
 */
public class TextureAnimator {
	List<Texture> textureList = new ArrayList<Texture>();
	List<Integer> frameDelay = new ArrayList<Integer>();
	float currentFrame;
	int frameLength = 0;

	public TextureAnimator() {
	}

	public Texture getTexture() { return getTexture(getCurrentFrame()); }
	public Texture getTexture(int i) {
		if (i >= getNmbrOfLoadedFrames()) return null;
		return textureList.get(i);
	}

	public int getCurrentFrame() {
		return (int)Math.floor(currentFrame);
	}
	public int getCurrentFrameDelay() {
		if (getCurrentFrame() >= frameDelay.size()) return 1000;
		return frameDelay.get(getCurrentFrame());
	}
	public int getNmbrOfFrames() {
		return frameLength;
	}
	public int getNmbrOfLoadedFrames() {
		return textureList.size();
	}
	public void setNmbrOfFrames(int n) { frameLength = n; }

	public void addTexture(Texture t, int time) {
		textureList.add(t);
		frameDelay.add(time);
	}

	public void logic() {
		float nextFrame = currentFrame;

		if (getCurrentFrameDelay() == 0)
			nextFrame += 1f;
		else
			nextFrame += Canvas.updateTime * (1000f / getCurrentFrameDelay());

		if (nextFrame >= getNmbrOfFrames()) nextFrame -= getNmbrOfFrames();

		if (nextFrame < getNmbrOfLoadedFrames()) currentFrame = nextFrame;
	}
}
