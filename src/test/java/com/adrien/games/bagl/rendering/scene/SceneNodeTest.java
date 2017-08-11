package com.adrien.games.bagl.rendering.scene;

import com.adrien.games.bagl.core.math.Vector3;
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class form {@link SceneNode}.
 *
 */
public class SceneNodeTest {

    @Test
    public void itShouldAddChild() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);
        Assert.assertThat(parent.getChildren(), CoreMatchers.hasItem(child));
    }

    @Test
    public void itShouldSetParent() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);

        Assert.assertThat(parent, CoreMatchers.sameInstance(child.getParent()));
    }

    @Test
    public void itShouldRemoveNodeFromPreviousParent() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> newParent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);
        newParent.addChild(child);

        Assert.assertThat(parent.getChildren(), IsEmptyCollection.empty());
    }

    @Test
    public void itShouldRemoveChild() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);
        parent.removeChild(child);

        Assert.assertThat(parent.getChildren(), IsEmptyCollection.empty());
    }

    @Test
    public void itShouldResetParentOnRemovingChild() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);
        parent.removeChild(child);

        Assert.assertThat(child.getParent(), CoreMatchers.nullValue());
    }

    @Test
    public void itShouldBeRoot() {
        final SceneNode<Integer> root = new SceneNode<>(0);

        Assert.assertThat(root.isRoot(), CoreMatchers.is(true));
    }

    @Test
    public void itShouldNotBeRootWhenAdded() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);

        Assert.assertThat(child.isRoot(), CoreMatchers.is(false));
    }

    @Test
    public void itShouldApplyConsumer() {
        final SceneNode<Integer> parent = new SceneNode<>(0);

        parent.apply(node -> node.getLocalTransform().setScale(new Vector3(2, 2, 2)));

        Assert.assertThat(parent.getLocalTransform().getScale(), CoreMatchers.equalTo(new Vector3(2f, 2f, 2f)));
    }

    @Test
    public void itShouldApplyToChildren() {
        final SceneNode<Integer> parent = new SceneNode<>(0);
        final SceneNode<Integer> child = new SceneNode<>(0);

        parent.addChild(child);
        parent.apply(node -> node.getLocalTransform().setScale(new Vector3(3, 3, 3)));

        Assert.assertThat(child.getLocalTransform().getScale(), CoreMatchers.equalTo(new Vector3(3f, 3f, 3f)));
    }

    @Test
    public void itShouldBeEmpty() {
        final SceneNode<Integer> parent = new SceneNode<>();

        Assert.assertThat(parent.isEmpty(), CoreMatchers.is(true));
    }
}
