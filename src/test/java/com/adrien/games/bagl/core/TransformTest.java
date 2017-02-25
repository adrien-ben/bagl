package com.adrien.games.bagl.core;

import org.hamcrest.MatcherAssert;import org.hamcrest.core.IsEqual;
import org.junit.Test;

public class TransformTest {

	@Test
	public void itShouldTranslate() {
		Transform transform = new Transform();
		Transform translation = new Transform();
		translation.setPosition(new Vector3(2, 3, 4));
		transform.transform(translation);
		MatcherAssert.assertThat(transform.getPosition().getX(), IsEqual.equalTo(2f));
		MatcherAssert.assertThat(transform.getPosition().getY(), IsEqual.equalTo(3f));
		MatcherAssert.assertThat(transform.getPosition().getZ(), IsEqual.equalTo(4f));		
		MatcherAssert.assertThat(transform.getRotation().getA(), IsEqual.equalTo(0f));
		MatcherAssert.assertThat(transform.getRotation().getI(), IsEqual.equalTo(0f));
		MatcherAssert.assertThat(transform.getRotation().getJ(), IsEqual.equalTo(0f));
		MatcherAssert.assertThat(transform.getRotation().getK(), IsEqual.equalTo(0f));		
		MatcherAssert.assertThat(transform.getScale().getX(), IsEqual.equalTo(1f));
		MatcherAssert.assertThat(transform.getScale().getY(), IsEqual.equalTo(1f));
		MatcherAssert.assertThat(transform.getScale().getZ(), IsEqual.equalTo(1f));
	}

}
