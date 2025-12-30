package de.jakob.lotm.abilities.common;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PendingAllyRequestPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AllyAbility extends AbilityItem {

    // Store pending ally requests: requester UUID -> target UUID -> expiration time
    private static final Map<UUID, Map<UUID, Long>> pendingRequests = new ConcurrentHashMap<>();
    private static final long REQUEST_TIMEOUT = 30000; // 30 seconds

    public AllyAbility(Properties properties) {
        super(properties, 1);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap<>();
        for(String pathway : BeyonderData.pathways) {
            reqs.put(pathway, 9);
        }
        return reqs;
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof Player player)) return;

        // Get the target entity
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 1.5f, true);

        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("lotm.ally.no_target").withColor(0xF44336));
            return;
        }

        // Check if already allies
        if (AllyUtil.areAllies(entity, target)) {
            // Remove ally relationship
            AllyUtil.removeAllies(entity, target);
            AbilityUtil.sendActionBar(entity, Component.translatable("lotm.ally.removed", target.getName()).withColor(0xFF9800));
            return;
        }

        // Check if target can be allied without permission
        if (AllyUtil.canBeAllied(target)) {
            AllyUtil.makeAllies(entity, target);
            AbilityUtil.sendActionBar(entity, Component.translatable("lotm.ally.added", target.getName()).withColor(0x4CAF50));
            return;
        }

        // Target is a beyonder or player - need to send request
        if (target instanceof Player targetPlayer) {
            sendAllyRequest(player, targetPlayer);
        } else {
            AbilityUtil.sendActionBar(entity, Component.translatable("lotm.ally.cannot_ally").withColor(0xF44336));
        }
    }

    /**
     * Send an ally request to a player
     */
    private void sendAllyRequest(Player requester, Player target) {
        UUID requesterUUID = requester.getUUID();
        UUID targetUUID = target.getUUID();

        // Check if there's already a pending request from target to requester
        if (hasPendingRequest(targetUUID, requesterUUID)) {
            // Auto-accept if both players want to be allies
            AllyUtil.makeAllies(requester, target);
            removePendingRequest(targetUUID, requesterUUID);
            AbilityUtil.sendActionBar(requester, Component.translatable("lotm.ally.request_accepted").withColor(0x4CAF50));
            AbilityUtil.sendActionBar(target, Component.translatable("lotm.ally.request_accepted").withColor(0x4CAF50));
            return;
        }

        // Send new request
        addPendingRequest(requesterUUID, targetUUID);

        // Notify requester
        AbilityUtil.sendActionBar(requester, Component.translatable("lotm.ally.request_sent", target.getName()).withColor(0x2196F3));

        // Send packet to target client to display clickable message
        if (target instanceof ServerPlayer serverTarget) {
            PendingAllyRequestPacket packet = new PendingAllyRequestPacket(requesterUUID, requester.getName().getString());
            PacketHandler.sendToPlayer(serverTarget, packet);
        }

        AbilityUtil.sendActionBar(target,
                Component.translatable("lotm.ally.request_received_short", requester.getName())
                        .withColor(0x2196F3));
    }

    /**
     * Accept an ally request
     */
    public static void acceptAllyRequest(Player accepter, UUID requesterUUID) {
        if (!hasPendingRequest(requesterUUID, accepter.getUUID())) {
            AbilityUtil.sendActionBar(accepter, Component.translatable("lotm.ally.no_request").withColor(0xF44336));
            return;
        }

        Player requester = accepter.getServer().getPlayerList().getPlayer(requesterUUID);
        if (requester == null) {
            AbilityUtil.sendActionBar(accepter, Component.translatable("lotm.ally.requester_offline").withColor(0xF44336));
            removePendingRequest(requesterUUID, accepter.getUUID());
            return;
        }

        AllyUtil.makeAllies(requester, accepter);
        removePendingRequest(requesterUUID, accepter.getUUID());

        AbilityUtil.sendActionBar(accepter, Component.translatable("lotm.ally.request_accepted").withColor(0x4CAF50));
        AbilityUtil.sendActionBar(requester, Component.translatable("lotm.ally.request_accepted").withColor(0x4CAF50));
    }

    /**
     * Deny an ally request
     */
    public static void denyAllyRequest(Player denier, UUID requesterUUID) {
        if (!hasPendingRequest(requesterUUID, denier.getUUID())) {
            return;
        }

        removePendingRequest(requesterUUID, denier.getUUID());

        Player requester = denier.getServer().getPlayerList().getPlayer(requesterUUID);
        if (requester != null) {
            AbilityUtil.sendActionBar(requester, Component.translatable("lotm.ally.request_denied", denier.getName()).withColor(0xF44336));
        }

        AbilityUtil.sendActionBar(denier, Component.translatable("lotm.ally.request_denied_self").withColor(0xFF9800));
    }

    // Request management methods
    private static void addPendingRequest(UUID requester, UUID target) {
        pendingRequests.computeIfAbsent(requester, k -> new ConcurrentHashMap<>())
                .put(target, System.currentTimeMillis() + REQUEST_TIMEOUT);
    }

    private static void removePendingRequest(UUID requester, UUID target) {
        Map<UUID, Long> requests = pendingRequests.get(requester);
        if (requests != null) {
            requests.remove(target);
            if (requests.isEmpty()) {
                pendingRequests.remove(requester);
            }
        }
    }

    private static boolean hasPendingRequest(UUID requester, UUID target) {
        cleanupExpiredRequests();
        Map<UUID, Long> requests = pendingRequests.get(requester);
        if (requests == null) return false;

        Long expiration = requests.get(target);
        if (expiration == null) return false;

        if (System.currentTimeMillis() > expiration) {
            requests.remove(target);
            return false;
        }

        return true;
    }

    private static void cleanupExpiredRequests() {
        long now = System.currentTimeMillis();
        pendingRequests.forEach((requester, targets) -> {
            targets.entrySet().removeIf(entry -> entry.getValue() < now);
        });
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}