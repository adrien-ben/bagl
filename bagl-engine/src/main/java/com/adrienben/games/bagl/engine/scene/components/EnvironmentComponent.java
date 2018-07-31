package com.adrienben.games.bagl.engine.scene.components;

import com.adrienben.games.bagl.engine.Time;
import com.adrienben.games.bagl.engine.scene.Component;
import com.adrienben.games.bagl.engine.scene.ComponentVisitor;
import com.adrienben.games.bagl.opengl.texture.Cubemap;

import java.util.Optional;

/**
 * Component containing environment lighting maps
 *
 * @author adrien
 */
public class EnvironmentComponent extends Component {

    private Cubemap environmentMap;
    private Cubemap irradianceMap;
    private Cubemap preFilteredMap;

    /**
     * Construct a new environment component
     *
     * @param environment The environment map
     * @param irradiance  The irradiance map
     * @param preFiltered The pre-filtered map
     */
    public EnvironmentComponent(final Cubemap environment, final Cubemap irradiance, final Cubemap preFiltered) {
        this.environmentMap = environment;
        this.irradianceMap = irradiance;
        this.preFilteredMap = preFiltered;
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#update(Time)
     */
    @Override
    public void update(final Time time) {
        // does nothing
    }


    /**
     * {@inheritDoc}
     *
     * @see Component#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        environmentMap.destroy();
        irradianceMap.destroy();
        preFilteredMap.destroy();
    }

    /**
     * {@inheritDoc}
     *
     * @see Component#accept(ComponentVisitor)
     */
    @Override
    public void accept(final ComponentVisitor visitor) {
        visitor.visit(this);
    }

    public Optional<Cubemap> getEnvironmentMap() {
        return Optional.ofNullable(this.environmentMap);
    }

    public Optional<Cubemap> getIrradianceMap() {
        return Optional.ofNullable(this.irradianceMap);
    }

    public Optional<Cubemap> getPreFilteredMap() {
        return Optional.ofNullable(this.preFilteredMap);
    }
}
