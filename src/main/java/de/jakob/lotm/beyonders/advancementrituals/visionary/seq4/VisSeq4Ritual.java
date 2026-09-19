package de.jakob.lotm.beyonders.advancementrituals.visionary.seq4;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.advancementrituals.AdvancementRitualManager;
import de.jakob.lotm.beyonders.advancementrituals.RitualRequirementData;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sequence 5 -> 4 Visionary "shared conviction" advancement ritual. */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class VisSeq4Ritual {
    private static final String PATHWAY = "visionary";
    private static final int TARGET_SEQUENCE = 4;
    private static final double GATHER_RADIUS = 100.0;

    // Players currently holding JOY or ANGER; lets the tick handler skip everyone else cheaply.
    private static final Set<UUID> playersWithEmotion = ConcurrentHashMap.newKeySet();

    private VisSeq4Ritual() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer caster)) return;
        if (!isCandidate(caster)) return;
        if (!playersWithEmotion.contains(caster.getUUID())) {
            if (caster.tickCount % 20 == 0) AdvancementRitualManager.clearProgress(caster);
            return;
        }

        var emotion = getEmotion(caster);
        if (emotion == null) {
            AdvancementRitualManager.clearProgress(caster);
            return;
        }

        var onlinePlayers = caster.server.getPlayerList().getPlayers();
        int required = getRequiredCount(caster);

        int matchingNearby = 0;
        for (ServerPlayer other : onlinePlayers) {
            if (other.level() == caster.level()
                    && other.distanceTo(caster) <= GATHER_RADIUS
                    && other.hasEffect(emotion)) {
                matchingNearby += AdvancementRitualManager.getParticipantWeight(other);
            }
        }

        if (matchingNearby < required) return;

        if (AdvancementRitualManager.startRitual(caster, PATHWAY, TARGET_SEQUENCE, 1)
                && AdvancementRitualManager.completeRitual(caster)) {
            caster.removeEffect(emotion);
        }
    }

    private static boolean isCandidate(ServerPlayer player) {
        return PATHWAY.equals(BeyonderData.getPathway(player))
                && BeyonderData.getSequence(player) == TARGET_SEQUENCE + 1;
    }

    public static int getRequiredCount(ServerPlayer caster) {
        int minimumRequired = RitualRequirementData.get(caster.server)
            .getRequiredAmount(PATHWAY, TARGET_SEQUENCE);
        int halfOnlinePlayers = (int) Math.ceil(caster.server.getPlayerList().getPlayerCount() / 2.0);
        return Math.max(minimumRequired, halfOnlinePlayers);
    }

    /** Current weighted count of nearby players sharing the caster's emotion (0 if caster has no emotion). */
    public static int getCurrentWeightedProgress(ServerPlayer caster) {
        var emotion = getEmotion(caster);
        if (emotion == null) return 0;

        int matchingNearby = 0;
        for (ServerPlayer other : caster.server.getPlayerList().getPlayers()) {
            if (other.level() == caster.level()
                    && other.distanceTo(caster) <= GATHER_RADIUS
                    && other.hasEffect(emotion)) {
                matchingNearby += AdvancementRitualManager.getParticipantWeight(other);
            }
        }
        return matchingNearby;
    }

    // Returns the caster's own emotion, which every matched player must share.
    private static Holder<MobEffect> getEmotion(ServerPlayer caster) {
        if (caster.hasEffect(ModEffects.JOY)) return ModEffects.JOY;
        if (caster.hasEffect(ModEffects.ANGER)) return ModEffects.ANGER;
        return null;
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Holder<MobEffect> effect = event.getEffectInstance().getEffect();
        if (effect == ModEffects.JOY || effect == ModEffects.ANGER) {
            playersWithEmotion.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        untrackIfNoLongerEmotional(event.getEntity());
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        untrackIfNoLongerEmotional(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        playersWithEmotion.remove(event.getEntity().getUUID());
    }

    private static void untrackIfNoLongerEmotional(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (!player.hasEffect(ModEffects.JOY) && !player.hasEffect(ModEffects.ANGER)) {
            playersWithEmotion.remove(player.getUUID());
        }
    }
}
