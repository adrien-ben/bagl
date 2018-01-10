package com.adrien.games.bagl.core;

public interface Game {
    void init();

    void destroy();

    void update(Time time);

    void render();
}
