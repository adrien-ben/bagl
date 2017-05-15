package com.adrien.games.bagl.sample;

import com.adrien.games.bagl.core.Color;
import com.adrien.games.bagl.core.Engine;
import com.adrien.games.bagl.core.Game;
import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.core.math.Vector2;
import com.adrien.games.bagl.rendering.text.Font;
import com.adrien.games.bagl.rendering.text.TextRenderer;
import com.adrien.games.bagl.utils.FileUtils;

/**
 * Text sample. Implementation of the signed distance field algorithm from Valve.
 */
public class TextSample implements Game {

    private static final String TITLE = "Text Sample";

    private static final String TEST_STRING = "Hello World.. ;)";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SCALE = 1f;

    private Font arial;
    private Font segoe;
    private TextRenderer renderer;

    @Override
    public void init() {
        Engine.setClearColor(Color.CORNFLOWER_BLUE);
        this.arial = new Font(FileUtils.getResourceAbsolutePath("/fonts/arial/arial.fnt"));
        this.segoe = new Font(FileUtils.getResourceAbsolutePath("/fonts/segoe/segoe.fnt"));
        this.renderer = new TextRenderer();
    }

    @Override
    public void destroy() {
        this.arial.destroy();
        this.renderer.destroy();
    }

    @Override
    public void update(Time time) {
    }

    @Override
    public void render() {
        this.renderer.render(TEST_STRING, this.segoe, new Vector2(0, 0), TEXT_SCALE, TEXT_COLOR);
    }

    public static void main(String[] args) {
        new Engine(new TextSample(), TITLE).start();
    }

}
