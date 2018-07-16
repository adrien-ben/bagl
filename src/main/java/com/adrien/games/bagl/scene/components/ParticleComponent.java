package com.adrien.games.bagl.scene.components;

import com.adrien.games.bagl.core.Time;
import com.adrien.games.bagl.rendering.particles.ParticleEmitter;
import com.adrien.games.bagl.scene.Component;
import com.adrien.games.bagl.scene.ComponentVisitor;

/**
 * Particle component.
 * <p>
 * This component contains a {@link ParticleEmitter} whose position will be updated
 * with the position of the parent {@link com.adrien.games.bagl.scene.GameObject}.
 *
 * @author adrien
 */
public class ParticleComponent extends Component {

    private ParticleEmitter emitter;

    public ParticleComponent(final ParticleEmitter emitter) {
        this.emitter = emitter;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set the position of the emitter to the position of the parent
     * {@link com.adrien.games.bagl.scene.GameObject}.
     */
    @Override
    public void update(final Time time) {
        emitter.setPosition(parentObject.getTransform().getTranslation());
        emitter.update(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(final ComponentVisitor visitor) {
        visitor.visit(this);
    }

    public ParticleEmitter getEmitter() {
        return emitter;
    }
}
