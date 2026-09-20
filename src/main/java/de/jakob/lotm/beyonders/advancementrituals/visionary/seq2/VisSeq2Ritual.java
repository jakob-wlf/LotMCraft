package de.jakob.lotm.beyonders.advancementrituals.visionary.seq2;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.beyonders.advancementrituals.RitualRequirementData;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.UUID;

/** Sequence 3 -> 2 Visionary advancement ritual: successfully manipulate
 *  10 distinct players, each carrying an emotion effect while controlled, then release them. */
public final class VisSeq2Ritual {
    private static final String PATHWAY = "visionary";
    private static final int TARGET_SEQUENCE = 2;

    private VisSeq2Ritual() {
    }

    /** Call when a manipulation control session on {@code target} ends (marionette released). */
    public static void onManipulationReleased(ServerPlayer caster, LivingEntity target) {
        if (!(target instanceof ServerPlayer targetPlayer)) return;
        if (!isCandidate(caster)) return;
        if (!targetPlayer.hasEffect(ModEffects.JOY) && !targetPlayer.hasEffect(ModEffects.ANGER)) return;

        Set<UUID> manipulated = caster.getData(ModAttachments.VISIONARY_RITUAL_DATA).getManipulatedRitualTargets();
        manipulated.add(targetPlayer.getUUID());

        int totalWeight = 0;
        for (UUID uuid : manipulated) {
            ServerPlayer p = caster.server.getPlayerList().getPlayer(uuid);
            totalWeight += p != null ? AdvancementRitualManager.getParticipantWeight(p) : 1;
        }
        int required = getRequiredCount(caster);
        if (totalWeight < required) return;

        if (AdvancementRitualManager.startRitual(caster, PATHWAY, TARGET_SEQUENCE, 1)
                && AdvancementRitualManager.completeRitual(caster)) {
            manipulated.clear();
        }
    }

    public static int getRequiredCount(ServerPlayer caster) {
        return RitualRequirementData.get(caster.server).getRequiredAmount(PATHWAY, TARGET_SEQUENCE);
    }

    public static int getCurrentWeightedProgress(ServerPlayer caster) {
        Set<UUID> manipulated = caster.getData(ModAttachments.VISIONARY_RITUAL_DATA).getManipulatedRitualTargets();
        int totalWeight = 0;
        for (UUID uuid : manipulated) {
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
