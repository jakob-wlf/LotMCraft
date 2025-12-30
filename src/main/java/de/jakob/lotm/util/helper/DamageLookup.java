package de.jakob.lotm.util.helper;

import java.util.TreeMap;


/**
 * Utility for interpolating damage values across numbered ability sequences
 * and performing DPS calculations based on Minecraft tick intervals.
 *
 * Sequences are ordered by numeric value:
 * Higher number = weaker, lower number = stronger.
 *
 * Scale interpretation:
 *   -1 → previous sequence min
 *    0 → current sequence min
 *    1 → current sequence max
 *    2 → next sequence max
 *
 * Values between these are linearly interpolated.
 */
public class DamageLookup {

    /** Represents the damage range for a sequence. */
    private static class Range {
        final double min;
        final double max;

        Range(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    /** Stores all defined sequences, ordered automatically. */
    private static final TreeMap<Integer, Range> SEQUENCES = new TreeMap<>();

    static {
        SEQUENCES.put(-1, new Range(3, 8));
        SEQUENCES.put(9, new Range(3, 8));
        SEQUENCES.put(8, new Range(4, 9));
        SEQUENCES.put(7, new Range(6.35, 13.2));
        SEQUENCES.put(6, new Range(8, 15.2));
        SEQUENCES.put(5, new Range(8.3, 17.1));
        SEQUENCES.put(4, new Range(12.4, 24.4));
        SEQUENCES.put(3, new Range(14, 27));
        SEQUENCES.put(2, new Range(27, 55));
        SEQUENCES.put(1, new Range(33, 69.5));
        SEQUENCES.put(0, new Range(33, 69.5));
    }

    /**
     * Computes the interpolated damage for a given sequence and scale.
     *
     * @param sequence the ability sequence index
     * @param scale    value in range [-1, 2] determining where to interpolate
     * @return a single interpolated damage value
     *
     * Scale anchor meanings:
     *   -1 = previous sequence min
     *    0 = this sequence min
     *    1 = this sequence max
     *    2 = next sequence max
     *
     * If previous/next sequence does not exist, current sequence anchors are reused.
     */
    public static double lookupDamage(int sequence, double scale) {

        if (!SEQUENCES.containsKey(sequence)) {
            sequence = 9;
        }

        Range current = SEQUENCES.get(sequence);
        Range previous = SEQUENCES.get(sequence + 1);
        Range next = SEQUENCES.get(sequence - 1);

        double anchorMinus1 = previous != null ? previous.min : current.min;
        double anchor0      = current.min;
        double anchor1      = current.max;
        double anchor2      = next != null ? next.max : current.max;

        if (scale <= -1) return anchorMinus1;
        if (scale >=  2) return anchor2;

        if (scale < 0) {
            return lerp(anchorMinus1, anchor0, (scale + 1));
        }
        if (scale < 1) {
            return lerp(anchor0, anchor1, scale);
        }
        return lerp(anchor1, anchor2, (scale - 1));
    }

    /**
     * Computes DPS based on an interpolated damage value, a tick interval,
     * and a total time duration in ticks.
     *
     * Formula:
     *   hits = totalTicks / intervalTicks
     *   result = damagePerHit / hits
     *
     * @param sequence      ability sequence index
     * @param scale         interpolation scale [-1, 2]
     * @param intervalTicks number of ticks between activations (>0)
     * @param totalTicks    total duration in ticks
     * @return damage divided by number of hits within the duration
     */
    public static double lookupDps(int sequence, double scale,
                                   int intervalTicks, int totalTicks) {

        if (intervalTicks <= 0) {
            throw new IllegalArgumentException("intervalTicks must be > 0");
        }

        int hits = totalTicks / intervalTicks;
        if (hits <= 0) {
            return 0;
        }

        double dmg = lookupDamage(sequence, scale);
        return dmg / hits;
    }

    /** Linear interpolation between two values. */
    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
