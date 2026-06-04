package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;

public final class BlackEmperorProgression {
    private BlackEmperorProgression() {}

    // Lower sequence number = stronger.
    public static boolean isSeqAtMost(LivingEntity entity, int seq) {
        return BeyonderData.isBeyonder(entity) && BeyonderData.getSequence(entity) <= seq;
    }

    public static boolean isSeq4Plus(LivingEntity entity) {
        return isSeqAtMost(entity, 4);
    }

    public static boolean isSeq5Plus(LivingEntity entity) {
        return isSeqAtMost(entity, 5);
    }

    public static float scaleFloat(LivingEntity entity, float seq9Value, float perStepBonus, float cap) {
        int step = Math.max(0, 9 - BeyonderData.getSequence(entity));
        return Math.min(seq9Value + (step * perStepBonus), cap);
    }

    public static int scaleTicks(LivingEntity entity, int seq9Ticks, int perStepBonus, int capTicks) {
        int step = Math.max(0, 9 - BeyonderData.getSequence(entity));
        return Math.min(seq9Ticks + (step * perStepBonus), capTicks);
    }
}