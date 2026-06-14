package de.jakob.lotm.sefirah;

import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SefirotData;
import de.jakob.lotm.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Handles the mental imprint mechanic for Sefirot ownership.
 *
 * <p>The first player to claim a Sefirot leaves a mental imprint that grows over time.
 * Any subsequent owner faces massive corruption from that imprint, and dying or succumbing
 * while holding it returns the Sefirot to the original owner.
 *
 * <p>Imprint growth (original owner): +1% per real-time hour whether online OR offline.
 * <p>Imprint reduction (non-original owner): −1% per real-time hour ONLY while online.
 * <p>Floor: the imprint can never drop below 10% once it has been established.
 *
 * <p>Corruption values:
 * <ul>
 *   <li>Initial burst on taking an imprinted Sefirot: {@code imprintPercent * 0.004f} (e.g. 40% at 100% imprint)</li>
 *   <li>Passive per game-second while holding: {@code imprintPercent * 0.00002f}</li>
 * </ul>
 */
@EventBusSubscriber
public class SefirotImprintEventHandler {

    /** How many game-seconds constitute one "imprint hour". 3600 seconds = 1 hour at 20 TPS. */
    private static final long SECONDS_PER_IMPRINT_HOUR = 3600L;

    // ── Per-entity tick (every 20 game-ticks = 1 game-second) ────────────────

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide || player.tickCount % 20 != 0) return;
        tickPlayerImprint(player);
    }

    private static void tickPlayerImprint(ServerPlayer player) {
        SefirotData data = SefirotData.get(player.server);
        String sefirot = data.getClaimedSefirot(player.getUUID());
        if (sefirot == null || sefirot.isEmpty()) return;

        UUID firstOwner = data.getFirstOwner(sefirot);
        if (firstOwner == null) {
            // Retroactively register as first owner for sefirot claims made before the imprint system
            data.setFirstOwnerIfAbsent(sefirot, player.getUUID());
            firstOwner = player.getUUID();
        }

        boolean isOriginalOwner = player.getUUID().equals(firstOwner);
        int imprint = data.getMentalImprint(sefirot);

        if (isOriginalOwner) {
            // Accumulate imprint while the original owner is online; also keep epoch fresh
            // so offline time is measured correctly.
            data.updateOriginalOwnerEpoch(sefirot);
            boolean grew = data.tickOriginalOwner(sefirot);
            if (grew) {
                int newImprint = data.getMentalImprint(sefirot);
                player.sendSystemMessage(Component.literal(
                        "Your mental imprint in this Sefirot deepens... (" + newImprint + "%)")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }

            // Passive corruption drain for the original sefirot owner.
            // Inside own dimension: strong drain (100% imprint = 5%/min = ~0.000833/sec)
            // Outside own dimension: weak drain  (100% imprint = 1%/min = ~0.0001667/sec)
            if (imprint > 0) {
                ResourceKey<net.minecraft.world.level.Level> dim =
                        SefirahHandler.getSefirotDimensionKey(sefirot);
                boolean insideDimension = dim != null && player.level().dimension().equals(dim);
                float drainPerSecond = imprint / 100f * (insideDimension ? 0.000833f : 0.0001667f);
                CorruptionComponent corruptComp = player.getData(ModAttachments.CORRUPTION_COMPONENT);
                corruptComp.decreaseCorruptionAndSync(drainPerSecond, player);
            }
        } else if (imprint > 0) {
            // Non-original owner: apply passive corruption and tick their reduction counter
            // At 100% imprint: 0.001f/sec = 1% corruption every 10 seconds, scaling with imprint.
            float passiveCorruption = imprint * 0.00001f;
            CorruptionComponent corruptComp = player.getData(ModAttachments.CORRUPTION_COMPONENT);
            corruptComp.increaseCorruptionAndSync(passiveCorruption, player);

            boolean reduced = data.tickCurrentOwnerReduction(sefirot);
            if (reduced) {
                int newImprint = data.getMentalImprint(sefirot);
                player.sendSystemMessage(Component.literal(
                        "Your will slowly erodes the mental imprint... (" + newImprint + "% remaining)")
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    // ── Player death → reclaim ────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        SefirotData data = SefirotData.get(player.server);
        String sefirot = data.getClaimedSefirot(player.getUUID());
        if (sefirot == null || sefirot.isEmpty()) return;

        UUID firstOwner = data.getFirstOwner(sefirot);
        if (firstOwner == null) return;
        if (player.getUUID().equals(firstOwner)) return; // original owner dying doesn't trigger reclaim
        if (data.getMentalImprint(sefirot) <= 0) return;

        // Non-original owner died while holding an imprinted sefirot → attempt reclaim
        tryReclaimForOriginalOwner(sefirot, firstOwner, player, player.server);
    }

    // ── Player login → process pending reclaims ───────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.server;
        if (server == null) return;

        SefirotData data = SefirotData.get(server);

        // Apply offline imprint growth for any sefirot this player originally claimed.
        for (String sefirot : data.getSefirotOwnedByFirst(player.getUUID())) {
            int gained = data.applyOfflineOriginalOwnerTime(sefirot);
            if (gained > 0) {
                int newImprint = data.getMentalImprint(sefirot);
                player.sendSystemMessage(Component.literal(
                        "While you were away, your mental imprint grew by " + gained + "% ... (" + newImprint + "%)")
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }

        // Process any sefirot that was pending reclaim for this player
        for (Map.Entry<String, UUID> entry : new java.util.HashMap<>(data.getAllPendingReclaims()).entrySet()) {
            if (!entry.getValue().equals(player.getUUID())) continue;
            String sefirot = entry.getKey();
            data.clearPendingReclaim(sefirot);

            // Only reclaim if the sefirot is currently unclaimed
            if (!data.isSefirotClaimed(sefirot)) {
                boolean success = SefirahHandler.claimSefirot(player, sefirot);
                if (success) {
                    player.sendSystemMessage(Component.literal(
                            "Your mental imprint has drawn the Sefirot back to you!")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
                }
            }
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Unclaims the sefirot from {@code currentOwner} and attempts to return it to
     * {@code originalOwnerUUID}. If that player is offline, stores a pending reclaim.
     */
    public static void tryReclaimForOriginalOwner(String sefirot, UUID originalOwnerUUID,
                                                   ServerPlayer currentOwner, MinecraftServer server) {
        // Strip the sefirot from the current owner
        SefirahHandler.unclaimSefirot(currentOwner);
        SefirotData.get(server).resetCurrentOwnerSeconds(sefirot);

        currentOwner.sendSystemMessage(Component.literal(
                "The original owner's mental imprint has torn this Sefirot from your grasp!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));

        // Return to original owner
        ServerPlayer originalOwner = server.getPlayerList().getPlayer(originalOwnerUUID);
        if (originalOwner != null && originalOwner.isAlive()) {
            SefirahHandler.claimSefirot(originalOwner, sefirot);
            originalOwner.sendSystemMessage(Component.literal(
                    "Your mental imprint has reclaimed the Sefirot!")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        } else {
            // Queue for when original owner logs in next
            SefirotData.get(server).setPendingReclaim(sefirot, originalOwnerUUID);
        }
    }

    /**
     * Applies the initial corruption burst when a non-original owner claims an imprinted sefirot.
     * Should be called from {@link SefirahHandler#claimSefirot} after confirming ownership.
     */
    public static void applyInitialImprintCorruption(ServerPlayer player, int imprintPercent) {
        if (imprintPercent <= 0) return;
        float burst = imprintPercent * 0.0085f; // 85% corruption at 100% imprint, scales linearly
        CorruptionComponent corruptComp = player.getData(ModAttachments.CORRUPTION_COMPONENT);
        corruptComp.increaseCorruptionAndSync(burst, player);

        player.sendSystemMessage(Component.literal(
                "The Sefirot's original owner still lingers in this realm — you feel their will crushing yours!")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
