package de.jakob.lotm.abilities.door;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncPlayerListPacket;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@EventBusSubscriber
public class PlayerTeleportationAbility extends SelectableAbility {

    public record PlayerInfo(int id, String name, UUID uuid) implements Comparable<PlayerInfo> {
        @Override
        public int compareTo(PlayerInfo other) {
            return this.name.compareToIgnoreCase(other.name); // Alphabetical ordering
        }
    }

    // Thread-safe for server operations
    private static final Map<UUID, List<PlayerInfo>> playerListCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();
    private static final long UPDATE_INTERVAL = 20; // 1 second (20 ticks)
    private static long serverTick = 0;

    public PlayerTeleportationAbility(String id) {
        super(id, 1);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1900.0f;
    }

    @Override
    protected String[] getAbilityNames() {
        // This method is called on client side for UI display
        // We need to return player names from the current player's cached list
        if (net.minecraft.client.Minecraft.getInstance().player != null) {
            UUID playerUUID = net.minecraft.client.Minecraft.getInstance().player.getUUID();
            List<PlayerInfo> players = playerListCache.getOrDefault(playerUUID, Collections.emptyList());
            return players.stream()
                    .map(PlayerInfo::name)
                    .toArray(String[]::new);
        }
        return new String[0];
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(entity instanceof Player player)) {
            return; // NPCs can't use this ability
        }

        if (!level.isClientSide) {
            List<PlayerInfo> availablePlayers = getAvailablePlayers((ServerLevel) level, player);

            // Validate ability index
            if (abilityIndex < 0 || abilityIndex >= availablePlayers.size()) {
                return; // Invalid index, possibly due to player list changing
            }

            PlayerInfo targetInfo = availablePlayers.get(abilityIndex);
            ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(targetInfo.uuid());

            // Edge case: target player is no longer online
            if (targetPlayer == null) {
                // Refresh the player list immediately
                updatePlayerList((ServerPlayer) player, (ServerLevel) level);
                return;
            }

            // Edge case: can't teleport to yourself
            if (targetPlayer.getUUID().equals(player.getUUID())) {
                return;
            }

            // Edge case: target player is in a different dimension
            if (targetPlayer.level().dimension() != player.level().dimension()) {
                // Teleport to the target dimension first, then to the player
                Vec3 targetPos = targetPlayer.position();
                ((ServerPlayer) player).teleportTo(
                        (ServerLevel) targetPlayer.level(),
                        targetPos.x,
                        targetPos.y,
                        targetPos.z,
                        player.getYRot(),
                        player.getXRot()
                );
            } else {
                // Same dimension teleportation
                Vec3 targetPos = targetPlayer.position();
                player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            }

            // Reset selected ability to avoid index out of bounds if list changes
            selectedAbilities.put(player.getUUID(), 0);
        }
    }

    /**
     * Gets the current list of available players, excluding the user
     */
    private List<PlayerInfo> getAvailablePlayers(ServerLevel level, Player excludePlayer) {
        List<PlayerInfo> players = level.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> !p.getUUID().equals(excludePlayer.getUUID())) // Exclude self
                .map(p -> new PlayerInfo(0, p.getGameProfile().getName(), p.getUUID()))
                .sorted() // Alphabetical order
                .collect(Collectors.toList());

        // Assign sequential IDs after sorting
        List<PlayerInfo> indexedPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            PlayerInfo old = players.get(i);
            indexedPlayers.add(new PlayerInfo(i, old.name(), old.uuid()));
        }

        return indexedPlayers;
    }

    /**
     * Updates the player list and syncs to client
     */
    private void updatePlayerList(ServerPlayer player, ServerLevel level) {
        List<PlayerInfo> players = getAvailablePlayers(level, player);
        playerListCache.put(player.getUUID(), players);
        lastUpdateTime.put(player.getUUID(), serverTick);

        // Send to client
        PacketHandler.sendToPlayer(player, new SyncPlayerListPacket(players));
    }

    @Override
    public void nextAbility(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        List<PlayerInfo> players = playerListCache.getOrDefault(player.getUUID(), Collections.emptyList());
        if (players.isEmpty()) {
            return;
        }

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility++;
        if (selectedAbility >= players.size()) {
            selectedAbility = 0;
        }
        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        List<PlayerInfo> players = playerListCache.getOrDefault(player.getUUID(), Collections.emptyList());
        if (players.isEmpty()) {
            return;
        }

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if (selectedAbility < 0) {
            selectedAbility = players.size() - 1;
        }
        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        List<PlayerInfo> players = playerListCache.getOrDefault(player.getUUID(), Collections.emptyList());

        if (players.isEmpty()) {
            return;
        }

        // Clamp the value to valid range
        if (selectedAbility < 0) {
            selectedAbility = 0;
        } else if (selectedAbility >= players.size()) {
            selectedAbility = players.size() - 1;
        }

        selectedAbilities.put(player.getUUID(), selectedAbility);
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return "";
        }

        List<PlayerInfo> players = playerListCache.getOrDefault(player.getUUID(), Collections.emptyList());
        if (players.isEmpty()) {
            return "";
        }

        if (!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());

        // Safety check for index bounds
        if (selectedAbility < 0 || selectedAbility >= players.size()) {
            selectedAbilities.put(entity.getUUID(), 0);
            selectedAbility = 0;
        }

        return players.get(selectedAbility).name();
    }

    // ===== Event Handlers for Automatic Updates =====

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        serverTick++;

        // Update player lists every second
        if (serverTick % UPDATE_INTERVAL == 0) {
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                Long lastUpdate = lastUpdateTime.get(player.getUUID());
                if (lastUpdate == null || serverTick - lastUpdate >= UPDATE_INTERVAL) {
                    // Find ability instance (you may need to adjust this based on your ability management)
                    // This assumes you have a way to get the ability instance
                    updatePlayerListStatic(player, (ServerLevel) player.level());
                }
            }
        }
    }

    private static void updatePlayerListStatic(ServerPlayer player, ServerLevel level) {
        List<PlayerInfo> players = level.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> !p.getUUID().equals(player.getUUID()))
                .map(p -> new PlayerInfo(0, p.getGameProfile().getName(), p.getUUID()))
                .sorted()
                .collect(Collectors.toList());

        List<PlayerInfo> indexedPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            PlayerInfo old = players.get(i);
            indexedPlayers.add(new PlayerInfo(i, old.name(), old.uuid()));
        }

        playerListCache.put(player.getUUID(), indexedPlayers);
        lastUpdateTime.put(player.getUUID(), serverTick);

        PacketHandler.sendToPlayer(player, new SyncPlayerListPacket(indexedPlayers));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Initialize player list for newly logged in player
            updatePlayerListStatic(serverPlayer, (ServerLevel) serverPlayer.level());

            // Update all other players' lists to include the new player
            for (ServerPlayer otherPlayer : serverPlayer.getServer().getPlayerList().getPlayers()) {
                if (!otherPlayer.getUUID().equals(serverPlayer.getUUID())) {
                    updatePlayerListStatic(otherPlayer, (ServerLevel) otherPlayer.level());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Clean up cached data
            playerListCache.remove(serverPlayer.getUUID());
            lastUpdateTime.remove(serverPlayer.getUUID());

            // Update all remaining players' lists to remove the logged out player
            for (ServerPlayer otherPlayer : serverPlayer.getServer().getPlayerList().getPlayers()) {
                if (!otherPlayer.getUUID().equals(serverPlayer.getUUID())) {
                    updatePlayerListStatic(otherPlayer, (ServerLevel) otherPlayer.level());
                }
            }
        }
    }
}