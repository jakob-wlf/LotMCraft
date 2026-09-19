package de.jakob.lotm.beyonders.advancementrituals.visionary.seq1;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.beyonders.advancementrituals.RitualRequirementData;
import de.jakob.lotm.beyonders.advancementrituals.ServerPopulationData;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.UUID;

/** Sequence 2 -> 1 Visionary "author's census" advancement ritual: a written (Story Writing /
 *  Psychological Cue) prophecy must successfully trigger its effect on enough unique players,
 *  scaling with 1.5x the highest concurrent player count the server has ever had. */
public final class VisSeq1Ritual {
    private static final String PATHWAY = "visionary";
    private static final int TARGET_SEQUENCE = 1;

    private VisSeq1Ritual() {
    }

    /** Call whenever a caster's written prophecy successfully triggers its effect on {@code target}. */
    public static void onProphecyTriggered(UUID casterId, LivingEntity target) {
        if (!(target instanceof ServerPlayer targetPlayer)) return;

        ServerPlayer caster = targetPlayer.server.getPlayerList().getPlayer(casterId);
        if (caster == null || !isCandidate(caster)) return;

        Set<UUID> triggered = caster.getData(ModAttachments.VISIONARY_RITUAL_DATA).getStoryWritingRitualTargets();
        triggered.add(targetPlayer.getUUID());

        int required = getRequiredCount(caster);

        int totalWeight = 0;
        for (UUID uuid : triggered) {
            ServerPlayer p = caster.server.getPlayerList().getPlayer(uuid);
            totalWeight += p != null ? AdvancementRitualManager.getParticipantWeight(p) : 1;
        }
        if (totalWeight < required) return;

        if (AdvancementRitualManager.startRitual(caster, PATHWAY, TARGET_SEQUENCE, 1)
                && AdvancementRitualManager.completeRitual(caster)) {
            triggered.clear();
        }
    }

    public static int getRequiredCount(ServerPlayer caster) {
        int minimumRequired = RitualRequirementData.get(caster.server)
            .getRequiredAmount(PATHWAY, TARGET_SEQUENCE);
        int highestPlayerCount = ServerPopulationData.get(caster.server).getHighestPlayerCount();
        return Math.max(minimumRequired, (int) Math.ceil(1.5 * highestPlayerCount));
    }

    public static int getCurrentWeightedProgress(ServerPlayer caster) {
        Set<UUID> triggered = caster.getData(ModAttachments.VISIONARY_RITUAL_DATA).getStoryWritingRitualTargets();
        int totalWeight = 0;
        for (UUID uuid : triggered) {
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
