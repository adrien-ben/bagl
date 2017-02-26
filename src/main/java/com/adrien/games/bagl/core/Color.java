package com.adrien.games.bagl.core;

public class Color {

	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color WHITE = new Color(1, 1, 1);
	public static final Color RED = new Color(1, 0, 0);
	public static final Color GREEN = new Color(0, 1, 0);
	public static final Color BLUE = new Color(0, 0, 1);
	public static final Color LIGHT_YELLOW = new Color(1, 1, 224/255f);
	public static final Color YELLOW = new Color(1, 1, 0);
	public static final Color PURPLE = new Color(1, 0, 1);
	public static final Color CYAN = new Color(0, 1, 1);
	public static final Color ORANGE = new Color(1, 165f/255, 0);
	public static final Color TURQUOISE = new Color(64f/255, 224f/255, 208f/255);
	public static final Color BRIGHT_PINK = new Color(1, 0, 127f/255);
	
	private float red;
	private float green;
	private float blue;
	private float alpha;
	
	public Color(float red, float green, float blue) {
		this(red, green, blue, 1);
	}
	
	public Color(float red, float green, float blue, float alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public float getRed() {
		return red;
	}

	public void setRed(float red) {
		this.red = red;
	}

	public float getGreen() {
		return green;
	}

	public void setGreen(float green) {
		this.green = green;
	}

	public float getBlue() {
		return blue;
	}

	public void setBlue(float blue) {
		this.blue = blue;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
}
