package com.adrien.games.bagl.core.math;

import com.adrien.games.bagl.utils.MathUtils;

/**
 * Represents an axis-aligned bounding box.
 */
public class Box {

    /**
     * Position of the bottom left corner of the box
     */
    private Vector2 position;

    /**
     * Width of the box
     */
    private float width;

    /**
     * Height of the box
     */
    private float height;

    public Box(final Vector2 position, final float width, final float height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    /**
     * Checks if this box contains another box.
     * <p>Border can be aligned. So, if the two boxes are at the same position
     * and of the same size this method will return true.</p>
     *
     * @param other The other box.
     * @return true if the other box is contained in this box.
     */
    public boolean contains(final Box other) {
        final float left = this.position.getX();
        final float right = left + this.width;
        final float bottom = this.position.getY();
        final float top = bottom + this.height;

        final float otherLeft = other.position.getX();
        final float otherRight = otherLeft + other.width;
        final float otherBottom = other.position.getY();
        final float otherTop = otherBottom + other.height;

        return left <= otherLeft && right >= otherRight && bottom <= otherBottom && top >= otherTop;
    }

    /**
     * Checks if this box collides with another.
     *
     * @param other The other box.
     * @return true is the two boxes collide.
     */
    public boolean collides(final Box other) {
        final float left = this.position.getX();
        final float right = left + this.width;
        final float bottom = this.position.getY();
        final float top = bottom + this.height;

        final float otherLeft = other.position.getX();
        final float otherRight = otherLeft + other.width;
        final float otherBottom = other.position.getY();
        final float otherTop = otherBottom + other.height;

        final float xIntersect = MathUtils.min(right - otherLeft, otherRight - left);
        final float yIntersect = MathUtils.min(top - otherBottom, otherTop - bottom);

        return xIntersect > 0 && xIntersect < this.width + other.width
                && yIntersect > 0 && yIntersect < this.height + other.height;
    }

    /**
     * Computes the intersection between two boxes.
     * <p>If there is no intersection the intersection value will be
     * a zero-vector and the method will return false. Otherwise,
     * the method returns true.</p>
     *
     * @param first        The first box.
     * @param second       The second box.
     * @param intersection The result of the intersection.
     * @return false if no intersection, true otherwise.
     */
    public static boolean intersection(final Box first, final Box second, final Vector2 intersection) {
        final float firstLeft = first.getPosition().getX();
        final float firstRight = firstLeft + first.getWidth();
        final float firstBottom = first.getPosition().getY();
        final float firstTop = firstBottom + first.getHeight();
        final float firstWidth = first.getWidth();
        final float firstHeight = first.getHeight();

        final float secondLeft = second.getPosition().getX();
        final float secondRight = secondLeft + second.getWidth();
        final float secondBottom = second.getPosition().getY();
        final float secondTop = secondBottom + second.getHeight();
        final float secondWidth = second.getWidth();
        final float secondHeight = second.getHeight();

        final float xIntersect = MathUtils.min(firstRight - secondLeft, secondRight - firstLeft);
        final float yIntersect = MathUtils.min(firstTop - secondBottom, secondTop - firstBottom);

        final boolean intersects = xIntersect > 0 && xIntersect < firstWidth + secondWidth
                && yIntersect > 0 && yIntersect < firstHeight + secondHeight;

        if (intersects) {
            intersection.setXY(xIntersect, yIntersect);
        } else {
            intersection.setXY(0, 0);
        }
        return intersects;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

}
