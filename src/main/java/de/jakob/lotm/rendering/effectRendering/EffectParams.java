package de.jakob.lotm.rendering.effectRendering;

import java.util.Arrays;

public record EffectParams(Integer duration, Boolean infinite, float[] params) {

    public static final int PARAM_COUNT = 9;
    public static final float UNUSED = -1f;

    public static final int START_X = 0, START_Y = 1, START_Z = 2;
    public static final int END_X = 3, END_Y = 4, END_Z = 5;

    public EffectParams {
        if (params != null && params.length != PARAM_COUNT) {
            throw new IllegalArgumentException("params must have length " + PARAM_COUNT);
        }
    }

    public static EffectParams defaults() {
        return new EffectParams(null, null, null);
    }

    public static EffectParams ofDuration(int duration) {
        return new EffectParams(duration, false, null);
    }

    public static EffectParams ofParams(float... params) {
        return new EffectParams(null, null, pad(params));
    }

    public static EffectParams infiniteWithLoop(int loopDuration) {
        return new EffectParams(loopDuration, true, null);
    }

    public static EffectParams of(int duration, float... params) {
        return new EffectParams(duration, null, pad(params));
    }

    public static EffectParams asInfinite() {
        return new EffectParams(null, true, null);
    }

    public static EffectParams direction(Integer duration,
                                         double startX, double startY, double startZ,
                                         double endX, double endY, double endZ) {
        float[] arr = defaultParamsArray();
        arr[START_X] = (float) startX; arr[START_Y] = (float) startY; arr[START_Z] = (float) startZ;
        arr[END_X] = (float) endX;     arr[END_Y] = (float) endY;     arr[END_Z] = (float) endZ;
        return new EffectParams(duration, null, arr);
    }

    public static EffectParams directionWithParams(Integer duration,
                                         double startX, double startY, double startZ,
                                         double endX, double endY, double endZ, float... params) {
        float[] arr = defaultParamsArray();
        arr[START_X] = (float) startX; arr[START_Y] = (float) startY; arr[START_Z] = (float) startZ;
        arr[END_X] = (float) endX;     arr[END_Y] = (float) endY;     arr[END_Z] = (float) endZ;
        if (params != null) {
            for (int i = 0; i < Math.min(params.length, PARAM_COUNT); i++) {
                if(arr.length <= i + 6 || i + 6 >= PARAM_COUNT) break;
                arr[i + 6] = params[i];
            }
        }
        return new EffectParams(duration, null, arr);
    }

    public static float[] defaultParamsArray() {
        float[] arr = new float[PARAM_COUNT];
        Arrays.fill(arr, UNUSED);
        return arr;
    }

    private static float[] pad(float[] input) {
        float[] arr = defaultParamsArray();
        System.arraycopy(input, 0, arr, 0, Math.min(input.length, PARAM_COUNT));
        return arr;
    }
}