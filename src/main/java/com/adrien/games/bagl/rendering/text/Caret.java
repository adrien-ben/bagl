package com.adrien.games.bagl.rendering.text;

/**
 * Caret class. This is used to keep track of cursor position
 * when rendering text.
 */
public class Caret {

    private final float lineGap;
    private final float startX;
    private float x;
    private float y;

    public Caret(float lineGap, float x, float y) {
        this.lineGap = lineGap;
        this.startX = x;
        this.x = x;
        this.y = y;
    }

    /**
     * Advances the x position of the caret.
     *
     * @param amount The amount to advance.
     */
    public void advance(float amount) {
        this.x += amount;
    }

    /**
     * Decrease y position of the lineGap and resets the x
     * position.
     */
    public void nextLine() {
        this.x = startX;
        this.y -= lineGap;
    }

    public boolean isNewLine() {
        return this.startX == x;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

}
