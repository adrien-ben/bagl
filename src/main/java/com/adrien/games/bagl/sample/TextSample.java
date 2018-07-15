package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.Text;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.utils.FileUtils;

/**
 * Text sample. Implementation of the signed distance field algorithm from Valve.
 */
public class TextSample implements Game {

    private static final String TITLE = "Text Sample";

    private Font arial;
    private Font segoe;
    private TextRenderer renderer;
    private Text text;

    @Override
    public void init() {
        Engine.setClearColor(Color.CORNFLOWER_BLUE);
        arial = new Font(FileUtils.getResourceAbsolutePath("/fonts/arial/arial.fnt"));
        segoe = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));
        renderer = new TextRenderer();
        text = Text.create("|Hello Potatoe World", segoe, Color.BLACK);
    }

    @Override
    public void destroy() {
        arial.destroy();
        segoe.destroy();
        renderer.destroy();
    }

    @Override
    public void update(Time time) {
    }

    @Override
    public void render() {
        renderer.render(text.setY(1 - 0.25f).setScale(0.25f));
        renderer.render(text.setY(1 - 0.45f).setScale(0.20f));
        renderer.render(text.setY(1 - 0.63f).setScale(0.18f));
        renderer.render(text.setY(1 - 0.78f).setScale(0.15f));
        renderer.render(text.setY(1 - 0.9f).setScale(0.12f));
        renderer.render(text.setY(1 - 0.96f).setScale(0.06f));
        renderer.render(text.setY(0f).setScale(0.04f));
    }

    public static void main(String[] args) {
        new Engine(new TextSample(), TITLE).start();
    }

}
