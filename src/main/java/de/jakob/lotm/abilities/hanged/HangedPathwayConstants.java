package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;

public final class HangedPathwayConstants {
    public static final String PATHWAY_ID = "hanged_man";
    public static final int SEQUENCE_SECRETS_SUPPLIANT = 9;
    public static final int SEQUENCE_LISTENER = 8;
    public static final int SEQUENCE_SHADOW_ASCETIC = 7;
    public static final int SEQUENCE_ROSE_BISHOP = 6;
    public static final int SEQUENCE_SHEPHERD = 5;
    public static final float TOTAL_SPIRITUALITY_SEQUENCE_9 = 630.0f;
    public static final float TOTAL_SPIRITUALITY_SEQUENCE_8 = 700.0f;
    public static final float TOTAL_SPIRITUALITY_SEQUENCE_7 = 2730.0f;
    public static final float TOTAL_SPIRITUALITY_SEQUENCE_6 = 4200.0f;
    public static final float TOTAL_SPIRITUALITY_SEQUENCE_5 = 6650.0f;
    public static final long TRUE_CREATOR_RAVING_CYCLE_TICKS = 5L * 24000L;
    public static final int ROSE_BISHOP_REGEN_INTERVAL_TICKS = 20;
    public static final int ROSE_BISHOP_REPLENISH_INTERVAL_TICKS = 40;

    private HangedPathwayConstants() {
    }

    public static int pathwayColor() {
        return BeyonderData.pathwayInfos.get(PATHWAY_ID).color();
    }

    public static float scaleForCurrentSequence(LivingEntity entity, int unlockSequence, float sequenceOneMaxScale) {
        if (entity == null || unlockSequence <= 1) {
            return 1.0f;
        }

        int currentSequence = Math.max(1, BeyonderData.getSequence(entity));
        int clampedUnlockSequence = Math.max(1, unlockSequence);
        if (currentSequence >= clampedUnlockSequence) {
            return 1.0f;
        }

        float progress = (clampedUnlockSequence - currentSequence) / (float) (clampedUnlockSequence - 1);
        return 1.0f + ((sequenceOneMaxScale - 1.0f) * progress);
    }

    public static int scaleIntForCurrentSequence(LivingEntity entity, int unlockSequence, int baseValue, float sequenceOneMaxScale) {
        return Math.max(baseValue, Math.round(baseValue * scaleForCurrentSequence(entity, unlockSequence, sequenceOneMaxScale)));
    }

    public static double scaleDoubleForCurrentSequence(LivingEntity entity, int unlockSequence, double baseValue, float sequenceOneMaxScale) {
        return baseValue * scaleForCurrentSequence(entity, unlockSequence, sequenceOneMaxScale);
    }
}
