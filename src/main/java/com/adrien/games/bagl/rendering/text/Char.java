package com.adrien.games.bagl.rendering.text;

import com.adrien.games.bagl.rendering.texture.TextureRegion;

public class Char {

	private final TextureRegion region;
	private final int width;
	private final int height;
	private final int xOffset;
	private final int yOffset;
	private final float xAdvance;
	private final char value;
	
	public Char(TextureRegion region, int width, int height, int xOffset, int yOffset, float xAdvance, char value) {
		this.region = region;
		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.xAdvance = xAdvance;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "char : " + this.value;
	}

	public TextureRegion getRegion() {
		return region;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public char getValue() {
		return value;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public float getXAdvance() {
		return xAdvance;
	}
	
}
