package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;

public class FloatArray extends Array {

    private float[] underlying;

    public static FloatArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 32);
        return new FloatArray(c);
    }

    public FloatArray(int c) {
        this.underlying = new float[c];
    }

    public FloatArray(float[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public float get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, float val) {
        this.underlying[idx] = val;
    }
}
