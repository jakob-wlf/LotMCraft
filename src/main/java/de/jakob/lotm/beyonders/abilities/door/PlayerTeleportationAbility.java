package de.jakob.lotm.beyonders.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncPlayerTeleportationOnlinePlayersPacket;
import de.jakob.lotm.network.packets.toClient.SyncPlayerTeleportationPlayerNamesPacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PlayerTeleportationAbility extends SelectableAbility {

    // Server and client keep separate copies: in singleplayer both threads share these statics, and editing
    // one shared list from both sides crashed the server tick. The lists are swapped as whole immutable
    // snapshots, never modified in place, so readers always see a complete list.
    private static volatile List<UUID> serverOnlinePlayers = List.of();
    private static volatile List<UUID> clientOnlinePlayers = List.of(); // synced from the server
    public static final Map<UUID, ServerLevel> levelsForPlayer = new ConcurrentHashMap<>(); // Does not need sync to client side
    private static final Map<UUID, String> serverNamesForPlayer = new ConcurrentHashMap<>();
    private static final Map<UUID, String> clientNamesForPlayer = new ConcurrentHashMap<>(); // synced from the server

    private static List<UUID> onlinePlayers() {
        return EffectiveSide.get().isClient() ? clientOnlinePlayers : serverOnlinePlayers;
    }

    private static Map<UUID, String> namesForPlayer() {
        return EffectiveSide.get().isClient() ? clientNamesForPlayer : serverNamesForPlayer;
    }

    public PlayerTeleportationAbility(String id) {
        super(id, 20);

        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        canBeShared = false;

        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(5, 10));

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(36000f, 20000f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 30000;
    }

    @Override
    protected String[] getAbilityNames() {
        Map<UUID, String> names = namesForPlayer();
        return onlinePlayers().stream().map(uuid -> names.getOrDefault(uuid, "Unknown Player")).toArray(String[]::new);
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        List<UUID> players = serverOnlinePlayers;
        if(players.isEmpty()) {
            return;
        }
        if(selectedAbility >= players.size()) {
            selectedAbility = 0;
            setSelectedAbility(player, 0);
        }

        UUID targetPlayerUUID = players.get(selectedAbility);
        ServerLevel targetLevel = levelsForPlayer.getOrDefault(targetPlayerUUID, null);
        if(targetLevel == null) {
            return;
        }

        ServerPlayer targetPlayer = (ServerPlayer) targetLevel.getPlayerByUUID(targetPlayerUUID);
        if(targetPlayer == null) {
            return;
        }

//        if (50 <= DivinationUtil.getConcealmentPower(targetPlayer)) {
//            player.sendSystemMessage(Component.literal("Failed to locate the target"));
//            return;
//        }
        if ((targetPlayer.level().dimension().location().equals(ModDimensions.SEFIRAH_CASTLE_TYPE_KEY.location()))
                //|| (targetPlayer.level().dimension().location().equals(ModDimensions.SPACE_TYPE_KEY.location()))
                || (targetPlayer.level().dimension().equals(ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY))
                //|| (targetPlayer.level().dimension().location().equals(ModDimensions.WORLD_CREATION_LEVEL_KEY.location()))
                || (targetPlayer.level().dimension().equals(ModDimensions.DREAM_MAZE_DIMENSION_KEY))
        ) {
            return;
        }

        player.teleportTo(targetLevel, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), Set.of(), player.getYRot(), player.getXRot());
        targetLevel.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        EffectManager.playEffect(EffectIds.WAYPOINT, targetPlayer.getX(), targetPlayer.getY() + 1, targetPlayer.getZ(), targetLevel);
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return "";

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        List<UUID> players = onlinePlayers();
        if(players.isEmpty()) {
            return "";
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        if(selectedAbility >= players.size() || selectedAbility < 0) {
            selectedAbility = 0;
            selectedAbilities.put(entity.getUUID(), 0);
        }

        String targetPlayerName = namesForPlayer().getOrDefault(players.get(selectedAbility), "Unknown Player");
        return targetPlayerName;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        List<UUID> snapshot = new ArrayList<>();
        for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            snapshot.add(player.getUUID());
            levelsForPlayer.put(player.getUUID(), player.serverLevel());
            serverNamesForPlayer.put(player.getUUID(), player.getName().getString());
            PacketHandler.sendToAllPlayers(new SyncPlayerTeleportationPlayerNamesPacket(player.getUUID().toString(), player.getName().getString()));
        }
        serverOnlinePlayers = List.copyOf(snapshot);

        if(snapshot.isEmpty()) {
            return;
        }

        PacketHandler.sendToAllPlayers(new SyncPlayerTeleportationOnlinePlayersPacket(
                new ArrayList<>(snapshot.stream().map(UUID::toString).toList())
        ));
    }

    /** Client side: replaces the synced list in one step. */
    public static void setOnlinePlayers(List<UUID> players) {
        clientOnlinePlayers = List.copyOf(players);
    }

    /** Client side. */
    public static void setNameForPlayer(UUID uuid, String name) {
        clientNamesForPlayer.put(uuid, name);
    }
}