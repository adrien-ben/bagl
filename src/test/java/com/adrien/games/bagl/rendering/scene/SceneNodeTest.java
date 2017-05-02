package com.adrien.games.bagl.rendering.scene;

import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.junit.Test;

/**
 * Test class form {@link SceneNode}.
 *
 */
public class SceneNodeTest {

	@Test
	public void itShouldAddChild() {
		final SceneNode<Integer> parent = new SceneNode<Integer>(0);
		final SceneNode<Integer> child = new SceneNode<Integer>(0);
		
		parent.addChild(child);
		
		MatcherAssert.assertThat(parent.getChildren(), IsCollectionContaining.hasItem(child));
	}
	
	@Test
	public void itShouldSetParent() {
		final SceneNode<Integer> parent = new SceneNode<Integer>(0);
		final SceneNode<Integer> child = new SceneNode<Integer>(0);
		
		parent.addChild(child);

		MatcherAssert.assertThat(parent, IsSame.sameInstance(child.getParent()));
	}
	
	@Test
	public void itShouldRemoveNodeFromPreviousParent() {
		final SceneNode<Integer> parent = new SceneNode<Integer>(0);
		final SceneNode<Integer> newParent = new SceneNode<Integer>(0);
		final SceneNode<Integer> child = new SceneNode<Integer>(0);
		
		parent.addChild(child);
		newParent.addChild(child);
		
		MatcherAssert.assertThat(parent.getChildren(), IsEmptyCollection.empty());
	}
	
	@Test
	public void itShouldRemoveChild() {
		final SceneNode<Integer> parent = new SceneNode<Integer>(0);
		final SceneNode<Integer> child = new SceneNode<Integer>(0);
		
		parent.addChild(child);
		parent.removeChild(child);
		
		MatcherAssert.assertThat(parent.getChildren(), IsEmptyCollection.empty());
	}

	@Test
	public void itShouldResetParentOnRemovingChild() {
		final SceneNode<Integer> parent = new SceneNode<Integer>(0);
		final SceneNode<Integer> child = new SceneNode<Integer>(0);
		
		parent.addChild(child);
		parent.removeChild(child);
		
		MatcherAssert.assertThat(child.getParent(), IsNull.nullValue());
	}
	
	@Test
	public void itShouldBeRoot() {
		final SceneNode<Integer> root = new SceneNode<Integer>(0);
		
		MatcherAssert.assertThat(root.isRoot(), Is.is(true));
	}
	
	@Test
	public void itShouldNotBeRootWhenAdded() {
		final SceneNode<Integer> parent = new SceneNode<Integer>(0);
		final SceneNode<Integer> child = new SceneNode<Integer>(0);
		
		parent.addChild(child);
		
		MatcherAssert.assertThat(child.isRoot(), Is.is(false));
	}
}
