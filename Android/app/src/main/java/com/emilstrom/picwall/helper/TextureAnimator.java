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

	public TextureAnimator() {
	}

	public Texture getTexture() { return getTexture(getCurrentFrame()); }
	public Texture getTexture(int i) {
		if (i >= getNmbrOfFrames()) return null;
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
		return textureList.size();
	}

	public void addTexture(Texture t, int time) {
		textureList.add(t);
		frameDelay.add(time);
	}

	public void logic() {
		if (getCurrentFrameDelay() == 0)
			currentFrame += 1f;
		else
			currentFrame += Canvas.updateTime * (1000f / getCurrentFrameDelay());
		if (currentFrame > getNmbrOfFrames()) currentFrame -= getNmbrOfFrames();
	}
}
