package de.jakob.lotm.beyonders.advancementrituals.visionary.seq0;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.beyonders.advancementrituals.RitualRequirementData;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.UUID;

/** Sequence 1 -> 0 Visionary "aligned prophecy" advancement ritual: write a single story
 *  predicting both the pathway and sequence a target will become, on 10 distinct players. */
public final class VisSeq0Ritual {
    private static final String PATHWAY = "visionary";
    private static final int TARGET_SEQUENCE = 0;

    private VisSeq0Ritual() {
    }

    /** Call whenever a caster's "become" prediction successfully fires on {@code target}. */
    public static void onPredictionTriggered(UUID casterId, LivingEntity target) {
        if (!(target instanceof ServerPlayer targetPlayer)) return;

        ServerPlayer caster = targetPlayer.server.getPlayerList().getPlayer(casterId);
        if (caster == null || !isCandidate(caster)) return;

        Set<UUID> fulfilled = caster.getData(ModAttachments.VISIONARY_RITUAL_DATA).getPredictedSequenceTargets();
        fulfilled.add(targetPlayer.getUUID());

        int totalWeight = 0;
        for (UUID uuid : fulfilled) {
            ServerPlayer p = caster.server.getPlayerList().getPlayer(uuid);
            totalWeight += p != null ? AdvancementRitualManager.getParticipantWeight(p) : 1;
        }
        int required = getRequiredCount(caster);
        if (totalWeight < required) return;

        if (AdvancementRitualManager.startRitual(caster, PATHWAY, TARGET_SEQUENCE, 1)
                && AdvancementRitualManager.completeRitual(caster)) {
            fulfilled.clear();
        }
    }

    public static int getRequiredCount(ServerPlayer caster) {
        return RitualRequirementData.get(caster.server).getRequiredAmount(PATHWAY, TARGET_SEQUENCE);
    }

    public static int getCurrentWeightedProgress(ServerPlayer caster) {
        Set<UUID> fulfilled = caster.getData(ModAttachments.VISIONARY_RITUAL_DATA).getPredictedSequenceTargets();
        int totalWeight = 0;
        for (UUID uuid : fulfilled) {
            ServerPlayer p = caster.server.getPlayerList().getPlayer(uuid);
            totalWeight += p != null ? AdvancementRitualManager.getParticipantWeight(p) : 1;
        }
        return totalWeight;
    }

    private static boolean isCandidate(ServerPlayer player) {
        return PATHWAY.equals(BeyonderData.getPathway(player))
                && BeyonderData.getSequence(player) == TARGET_SEQUENCE + 1;
    }
}
