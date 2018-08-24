package com.adrienben.games.bagl.engine.rendering.postprocess.fxaa;

/**
 * Quality presets for Fxaa.
 *
 * @author adrien
 */
public enum FxaaPresets {

    DISABLED(-1f, -1f, -1f),
    LOW(0.25f, 0.25f, 0.0883f),
    MEDIUM(0.5f, 0.166f, 0.0833f),
    HIGH(0.75f, 0.125f, 0.0625f);

    private float fxaaQualitySubpix;
    private float fxaaQualityEdgeThreshold;
    private float fxaaQualityEdgeThresholdMin;

    FxaaPresets(final float fxaaQualitySubpix, final float fxaaQualityEdgeThreshold, final float fxaaQualityEdgeThresholdMin) {
        this.fxaaQualitySubpix = fxaaQualitySubpix;
        this.fxaaQualityEdgeThreshold = fxaaQualityEdgeThreshold;
        this.fxaaQualityEdgeThresholdMin = fxaaQualityEdgeThresholdMin;
    }

    public float getFxaaQualitySubpix() {
        return fxaaQualitySubpix;
    }

    public float getFxaaQualityEdgeThreshold() {
        return fxaaQualityEdgeThreshold;
    }

    public float getFxaaQualityEdgeThresholdMin() {
        return fxaaQualityEdgeThresholdMin;
    }
}
