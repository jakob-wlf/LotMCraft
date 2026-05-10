package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class OriginBlessingEventHandler {
    private static final String CHANT = "Praise the Origin";
    private static final int REQUIRED_INVOCATIONS = 4;
    private static final int MAX_BLESSING_SEQUENCE = 9;
    private static final long CHANT_WINDOW_MS = 60_000L;

    private static final Map<UUID, Integer> chantProgress = new HashMap<>();
    private static final Map<UUID, Long> chantTimeouts = new HashMap<>();

    private static int nextBlessingSequence = 0;

    private OriginBlessingEventHandler() {
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        UUID playerId = player.getUUID();
        long now = System.currentTimeMillis();

        Long timeout = chantTimeouts.get(playerId);
        if (timeout != null && timeout <= now) {
            chantTimeouts.remove(playerId);
            chantProgress.remove(playerId);
        }

        if (!CHANT.equalsIgnoreCase(event.getRawText().trim())) {
            return;
        }

        int progress = chantProgress.getOrDefault(playerId, 0) + 1;
        chantProgress.put(playerId, progress);
        chantTimeouts.put(playerId, now + CHANT_WINDOW_MS);

        if (progress < REQUIRED_INVOCATIONS) {
            return;
        }

        chantProgress.remove(playerId);
        chantTimeouts.remove(playerId);
        grantBlessing(player);
    }

    private static void grantBlessing(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!BeyonderData.isBeyonder(player)) {
            player.sendSystemMessage(Component.translatable("lotmcraft.origin_blessing.no_path")
                    .withStyle(ChatFormatting.DARK_RED));
            return;
        }

        String pathway = BeyonderData.getPathway(player);
        if (pathway == null || pathway.isBlank() || "none".equalsIgnoreCase(pathway)) {
            player.sendSystemMessage(Component.translatable("lotmcraft.origin_blessing.no_path")
                    .withStyle(ChatFormatting.DARK_RED));
            return;
        }

        int blessingSequence = Math.max(0, Math.min(nextBlessingSequence, MAX_BLESSING_SEQUENCE));
        BeyonderData.setBeyonder(player, pathway, blessingSequence, true, false, true, true, true);

        Component sequenceName = Component.translatable("lotm.sequence." + BeyonderData.getSequenceName(pathway, blessingSequence));
        Component pathwayName = Component.translatable("lotm.sequence." + pathway);

        player.sendSystemMessage(Component.translatable(
                "lotmcraft.origin_blessing.applied",
                blessingSequence,
                sequenceName,
                pathwayName
        ).withStyle(ChatFormatting.GOLD));

        serverLevel.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                "lotmcraft.origin_blessing.broadcast",
                player.getDisplayName(),
                blessingSequence,
                sequenceName
        ).withStyle(ChatFormatting.LIGHT_PURPLE), false);

        nextBlessingSequence = Math.min(MAX_BLESSING_SEQUENCE, blessingSequence + 1);
        player.sendSystemMessage(Component.translatable("lotmcraft.origin_blessing.next", nextBlessingSequence)
                .withStyle(ChatFormatting.GRAY));
    }
}
