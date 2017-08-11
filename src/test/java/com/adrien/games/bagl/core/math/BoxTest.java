package com.adrien.games.bagl.core.math;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link Box}.
 */
public class BoxTest {

	@Test
	public void itShouldNotCollide1() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(11, 0), 10, 10);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(false));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(false));
	}

	@Test
	public void itShouldNotCollide2() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(2, 11), 8, 8);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(false));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(false));
	}

	@Test
	public void itShouldNotCollide3() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(10, 11), 8, 8);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(false));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(false));
	}

	@Test
	public void itShouldCollide1() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(1, 1), 8, 8);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(true));
	}

	@Test
	public void itShouldCollide2() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(9, 0), 10, 10);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(true));
	}

	@Test
	public void itShouldCollide3() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(0, 9), 10, 10);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(true));
	}

	@Test
	public void itShouldCollide4() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(9, 9), 10, 10);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(true));
	}

	@Test
	public void itShouldCollide5() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(5, 5), 8, 8);
		Assert.assertThat(box1.collides(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.collides(box1), CoreMatchers.is(true));
	}

	@Test
	public void itShouldContain() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(1, 1), 8, 8);
		Assert.assertThat(box1.contains(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.contains(box1), CoreMatchers.is(false));
	}

	@Test
	public void itShouldContainWhenEquals() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(0, 0), 10, 10);
		Assert.assertThat(box1.contains(box2), CoreMatchers.is(true));
		Assert.assertThat(box2.contains(box1), CoreMatchers.is(true));
	}

	@Test
	public void itShouldNotContain() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(5, 5), 10, 10);
		Assert.assertThat(box1.contains(box2), CoreMatchers.is(false));
		Assert.assertThat(box2.contains(box1), CoreMatchers.is(false));
	}

	@Test
	public void itShouldNotHaveIntersection1() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(11, 0), 10, 10);
		this.assertIntersection(box1, box2, Vector2.ZERO);
	}

	@Test
	public void itShouldNotHaveIntersection2() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(2, 11), 8, 8);
		this.assertIntersection(box1, box2, Vector2.ZERO);
	}

	@Test
	public void itShouldNotHaveIntersection3() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(10, 11), 8, 8);
		this.assertIntersection(box1, box2, Vector2.ZERO);
	}

	@Test
	public void itShouldHaveIntersection2() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(9, 0), 10, 10);
		this.assertIntersection(box1, box2, new Vector2(1, 10));
	}

	@Test
	public void itShouldHaveIntersection3() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(0, 9), 10, 10);
		this.assertIntersection(box1, box2, new Vector2(10, 1));
	}

	@Test
	public void itShouldHaveIntersection4() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(9, 9), 10, 10);
		this.assertIntersection(box1, box2, new Vector2(1, 1));
	}

	@Test
	public void itShouldHaveIntersection5() {
		final Box box1 = new Box(new Vector2(0, 0), 10, 10);
		final Box box2 = new Box(new Vector2(5, 5), 8, 8);
		this.assertIntersection(box1, box2, new Vector2(5, 5));
	}

	private void assertIntersection(final Box box1, final Box box2, final Vector2 expected) {
		final Vector2 intersection = new Vector2();
		Box.intersection(box1, box2,intersection);
		Assert.assertThat(intersection, CoreMatchers.equalTo(expected));
		Box.intersection(box2, box1,intersection);
		Assert.assertThat(intersection, CoreMatchers.equalTo(expected));
	}

}