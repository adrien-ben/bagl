package com.adrienben.games.bagl.engine.scene.components;

import com.adrienben.games.bagl.engine.Time;
import com.adrienben.games.bagl.engine.rendering.particles.ParticleEmitter;
import com.adrienben.games.bagl.engine.scene.Component;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;
import com.adrienben.games.bagl.engine.scene.GameObject;

/**
 * Particle component.
 * <p>
 * This component contains a {@link ParticleEmitter} whose position will be updated
 * with the position of the parent {@link GameObject}.
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
     * {@link GameObject}.
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
