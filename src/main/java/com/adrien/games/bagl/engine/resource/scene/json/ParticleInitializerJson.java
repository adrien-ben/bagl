package com.adrien.games.bagl.engine.resource.scene.json;

import com.adrien.games.bagl.core.math.Range;
import org.joml.Vector3f;

public class ParticleInitializerJson {

    private Range<Vector3f> position;
    private Range<Vector3f> direction;
    private Range<Float> size;
    private Range<Float> speed;
    private Range<Float> ttl;

    public Range<Vector3f> getPosition() {
        return position;
    }

    public Range<Vector3f> getDirection() {
        return direction;
    }

    public Range<Float> getSize() {
        return size;
    }

    public Range<Float> getSpeed() {
        return speed;
    }

    public Range<Float> getTtl() {
        return ttl;
    }
}
