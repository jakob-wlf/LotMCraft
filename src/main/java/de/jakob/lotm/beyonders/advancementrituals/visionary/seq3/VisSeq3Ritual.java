package de.jakob.lotm.beyonders.advancementrituals.visionary.seq3;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.beyonders.advancementrituals.RitualRequirementData;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Sequence 4 -> 3 Visionary "census of sleepers" advancement ritual: put 10 distinct
 *  players to sleep (via the writeable sleep action or other means) within a single night. */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class VisSeq3Ritual {
    private static final String PATHWAY = "visionary";
    private static final int TARGET_SEQUENCE = 3;
    private static final int CHECK_INTERVAL_TICKS = 20;

    private static final Map<UUID, NightProgress> nightProgress = new HashMap<>();

    private VisSeq3Ritual() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer caster)) return;
        if (caster.tickCount % CHECK_INTERVAL_TICKS != 0) return;
        if (!isCandidate(caster)) return;

        if (!caster.level().isNight()) {
            nightProgress.remove(caster.getUUID());
            AdvancementRitualManager.clearProgress(caster);
            return;
        }

        long nightId = caster.level().getDayTime() / 24000L;
        NightProgress progress = nightProgress.computeIfAbsent(caster.getUUID(),
                uuid -> new NightProgress(nightId));
        if (progress.nightId != nightId) {
            progress = new NightProgress(nightId);
            nightProgress.put(caster.getUUID(), progress);
        }

        for (ServerPlayer other : caster.server.getPlayerList().getPlayers()) {
            if (other != caster && other.hasEffect(ModEffects.ASLEEP)) {
                progress.sleepers.add(other.getUUID());
            }
        }

        int totalWeight = 0;
        for (UUID sleeperId : progress.sleepers) {
            ServerPlayer sleeper = caster.server.getPlayerList().getPlayer(sleeperId);
            totalWeight += sleeper != null ? AdvancementRitualManager.getParticipantWeight(sleeper) : 1;
        }
        int required = getRequiredCount(caster);
        if (totalWeight < required) return;

        if (AdvancementRitualManager.startRitual(caster, PATHWAY, TARGET_SEQUENCE, 1)
                && AdvancementRitualManager.completeRitual(caster)) {
            nightProgress.remove(caster.getUUID());
        }
    }

    public static int getRequiredCount(ServerPlayer caster) {
        return RitualRequirementData.get(caster.server).getRequiredAmount(PATHWAY, TARGET_SEQUENCE);
    }

    /** Current tracked weight for tonight's sleepers, or 0 if it's not night or nothing is tracked yet. */
    public static int getCurrentWeightedProgress(ServerPlayer caster) {
        NightProgress progress = nightProgress.get(caster.getUUID());
        if (progress == null) return 0;

        int totalWeight = 0;
        for (UUID sleeperId : progress.sleepers) {
            ServerPlayer sleeper = caster.server.getPlayerList().getPlayer(sleeperId);
            totalWeight += sleeper != null ? AdvancementRitualManager.getParticipantWeight(sleeper) : 1;
        }
        return totalWeight;
    }

    private static boolean isCandidate(ServerPlayer player) {
        return PATHWAY.equals(BeyonderData.getPathway(player))
                && BeyonderData.getSequence(player) == TARGET_SEQUENCE + 1;
    }

    private static final class NightProgress {
        private final long nightId;
        private final Set<UUID> sleepers = new HashSet<>();

        private NightProgress(long nightId) {
            this.nightId = nightId;
        }
    }
}
