package de.jakob.lotm.beyonders.advancementrituals;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncRitualProgressPacket;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class AdvancementRitualManager {
    public static final int lowestRitualSequence = 5;
    public static final int highestRitualSequence = 0;
    public static final int completionWindowTicks = 5 * 60 * 20;

    // Only pathways with an actual ritual implementation are gated; others advance normally.
    private static final Set<String> RITUAL_IMPLEMENTED_PATHWAYS = Set.of("visionary");

    private static final Map<UUID, ActiveRitual> activeRituals = new HashMap<>();
    private static final Map<UUID, RitualCompletion> completions = new HashMap<>();
    private static final Map<UUID, Integer> visibleProgressSequences = new HashMap<>();

    private AdvancementRitualManager() {
    }

    public static boolean requiresRitual(ServerPlayer player, String pathway, int targetSequence) {
        return !player.isCreative()
                && RITUAL_IMPLEMENTED_PATHWAYS.contains(pathway)
                && targetSequence >= highestRitualSequence
                && targetSequence <= lowestRitualSequence
                && BeyonderData.isBeyonder(player)
                && BeyonderData.getSequence(player) > targetSequence;
    }

    public static boolean startRitual(ServerPlayer player, String pathway, int targetSequence,
                                      int durationTicks) {
        if (activeRituals.containsKey(player.getUUID())
                || hasMatchingCompletion(player, pathway, targetSequence)
            || !requiresRitual(player, pathway, targetSequence)
                || !isStillEligible(player, targetSequence)
                || pathway == null
                || pathway.isBlank()
                || durationTicks <= 0) {
            return false;
        }

        activeRituals.put(player.getUUID(),
                new ActiveRitual(pathway, targetSequence, durationTicks));
        player.sendSystemMessage(Component.literal("Ritual started.")
            .withStyle(ChatFormatting.GOLD));
        return true;
    }

    public static boolean cancelRitual(ServerPlayer player) {
        boolean removed = activeRituals.remove(player.getUUID()) != null;
        clearProgress(player);
        return removed;
    }

    public static boolean completeRitual(ServerPlayer player) {
        ActiveRitual ritual = activeRituals.remove(player.getUUID());
        if (ritual == null || !isStillEligible(player, ritual.targetSequence)) {
            return false;
        }

        if (hasMatchingCompletion(player, ritual.pathway, ritual.targetSequence)) {
            clearProgress(player);
            return false;
        }

        long expiresAt = player.level().getGameTime() + completionWindowTicks;
        completions.put(player.getUUID(),
                new RitualCompletion(ritual.pathway, ritual.targetSequence, expiresAt));
        clearProgress(player);
        player.sendSystemMessage(Component.literal("Ritual completed.")
                .withStyle(ChatFormatting.GOLD));
        return true;
    }

        public static void syncProgress(ServerPlayer player, int current, int total,
                        String label, boolean timed) {
            if (!isRitualActive(player)) {
                clearProgress(player);
                return;
            }
            visibleProgressSequences.put(player.getUUID(), BeyonderData.getSequence(player) - 1);
        PacketHandler.sendToPlayer(player, new SyncRitualProgressPacket(
            Math.clamp(current, 0, Math.max(1, total)), Math.max(1, total), label, timed, true));
        }

        public static void clearProgress(ServerPlayer player) {
            visibleProgressSequences.remove(player.getUUID());
        PacketHandler.sendToPlayer(player, new SyncRitualProgressPacket(0, 0, "Ritual", false, false));
        }

    public static boolean hasValidCompletion(ServerPlayer player, String pathway, int targetSequence) {
        if (!requiresRitual(player, pathway, targetSequence)) {
            return true;
        }

        RitualCompletion completion = completions.get(player.getUUID());
        if (completion == null) {
            return false;
        }

        if (player.level().getGameTime() >= completion.expiresAt) {
            completions.remove(player.getUUID());
            return false;
        }

        return completion.pathway.equals(pathway)
                && completion.targetSequence == targetSequence;
    }

    private static boolean hasMatchingCompletion(ServerPlayer player, String pathway, int targetSequence) {
        RitualCompletion completion = completions.get(player.getUUID());
        if (completion == null) return false;
        if (player.level().getGameTime() >= completion.expiresAt) {
            completions.remove(player.getUUID());
            return false;
        }
        return completion.pathway.equals(pathway)
                && completion.targetSequence == targetSequence;
    }

    public static boolean consumeCompletion(ServerPlayer player, String pathway, int targetSequence) {
        if (!requiresRitual(player, pathway, targetSequence)) {
            return true;
        }
        if (!hasValidCompletion(player, pathway, targetSequence)) {
            activeRituals.remove(player.getUUID());
            clearProgress(player);
            player.sendSystemMessage(Component.literal(
                    "The advancement ritual is incomplete. This advancement will fail.")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        completions.remove(player.getUUID());
        return true;
    }

    public static boolean isRitualActive(ServerPlayer player) {
        return activeRituals.containsKey(player.getUUID());
    }

    public static float getRitualProgress(ServerPlayer player) {
        ActiveRitual ritual = activeRituals.get(player.getUUID());
        if (ritual == null) {
            return 0.0f;
        }
        return Math.min(1.0f, ritual.elapsedTicks / (float) ritual.durationTicks);
    }

    public static long getRemainingCompletionTicks(ServerPlayer player) {
        RitualCompletion completion = completions.get(player.getUUID());
        if (completion == null) {
            return 0;
        }
        return Math.max(0, completion.expiresAt - player.level().getGameTime());
    }

    /**
     * Weight a beyonder counts as toward ritual headcount requirements (e.g. "gather 10 players").
     * Higher-sequence beyonders are worth more; Great Old Ones alone satisfy almost any threshold.
     */
    public static int getParticipantWeight(LivingEntity entity) {
        if (!BeyonderData.isBeyonder(entity)) {
            return 1;
        }
        return switch (BeyonderData.getSequence(entity)) {
            case 4 -> 2;
            case 3 -> 3;
            case 2 -> 4;
            case 1 -> 6;
            case 0 -> 8;
            case LOTMCraft.GREAT_OLD_ONE_SEQ -> 999;
            default -> 1;
        };
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Integer progressSequence = visibleProgressSequences.get(player.getUUID());
        if (progressSequence != null && !isStillEligible(player, progressSequence)) {
            clearProgress(player);
        }

        ActiveRitual ritual = activeRituals.get(player.getUUID());
        if (ritual == null) {
            return;
        }
        if (!isStillEligible(player, ritual.targetSequence)) {
            activeRituals.remove(player.getUUID());
            clearProgress(player);
            return;
        }

        ritual.elapsedTicks++;
        if (ritual.elapsedTicks >= ritual.durationTicks) {
            completeRitual(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        activeRituals.remove(event.getEntity().getUUID());
        visibleProgressSequences.remove(event.getEntity().getUUID());
    }

    private static boolean isStillEligible(ServerPlayer player, int targetSequence) {
        return BeyonderData.getSequence(player) == targetSequence + 1;
    }

    private static final class ActiveRitual {
        private final String pathway;
        private final int targetSequence;
        private final int durationTicks;
        private int elapsedTicks;

        private ActiveRitual(String pathway, int targetSequence, int durationTicks) {
            this.pathway = pathway;
            this.targetSequence = targetSequence;
            this.durationTicks = durationTicks;
        }
    }

    private record RitualCompletion(String pathway, int targetSequence, long expiresAt) {
    }
}
