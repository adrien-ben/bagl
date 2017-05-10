package com.adrien.games.bagl.rendering.light;

public class Attenuation {

    public static final Attenuation CLOSE = new Attenuation(1f, 0.7f, 1.8f);
    public static final Attenuation VERY_FAR = new Attenuation(1f, 0.0014f, 0.000007f);

    private float constant;
    private float linear;
    private float quadratic;

    public Attenuation(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getQuadratic() {
        return quadratic;
    }

    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }

}
