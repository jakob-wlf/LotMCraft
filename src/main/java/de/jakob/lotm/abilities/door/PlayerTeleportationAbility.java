package de.jakob.lotm.abilities.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncPlayerTeleportationOnlinePlayersPacket;
import de.jakob.lotm.network.packets.toClient.SyncPlayerTeleportationPlayerNamesPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PlayerTeleportationAbility extends SelectableAbility {

    public static final ArrayList<UUID> onlinePlayers = new ArrayList<>(); // Needs sync to client side
    public static final HashMap<UUID, ServerLevel> levelsForPlayer = new HashMap<>(); // Does not need sync to client side
    public static final HashMap<UUID, String> namesForPlayer = new HashMap<>(); // Needs sync to client side

    public PlayerTeleportationAbility(String id) {
        super(id, 2);

        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return onlinePlayers.stream().map(uuid -> namesForPlayer.getOrDefault(uuid, "Unknown Player")).toArray(String[]::new).clone();
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        if(onlinePlayers.isEmpty()) {
            return;
        }
        if(selectedAbility >= onlinePlayers.size()) {
            selectedAbility = 0;
            setSelectedAbility(player, 0);
        }

        UUID targetPlayerUUID = onlinePlayers.get(selectedAbility);
        ServerLevel targetLevel = levelsForPlayer.getOrDefault(targetPlayerUUID, null);
        if(targetLevel == null) {
            return;
        }

        ServerPlayer targetPlayer = (ServerPlayer) targetLevel.getPlayerByUUID(targetPlayerUUID);
        if(targetPlayer == null) {
            return;
        }

        player.teleportTo(targetLevel, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), Set.of(), player.getYRot(), player.getXRot());
        targetLevel.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
        EffectManager.playEffect(EffectManager.Effect.WAYPOINT, targetPlayer.getX(), targetPlayer.getY() + 1, targetPlayer.getZ(), targetLevel);
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        if(getAbilityNames().length == 0)
            return "";

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        if(selectedAbility >= onlinePlayers.size() || selectedAbility < 0) {
            selectedAbility = 0;
            selectedAbilities.put(entity.getUUID(), 0);
        }

        String targetPlayerName = namesForPlayer.getOrDefault(onlinePlayers.get(selectedAbility), "Unknown Player");
        return targetPlayerName;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        onlinePlayers.clear();
        for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            onlinePlayers.add(player.getUUID());
            levelsForPlayer.put(player.getUUID(), player.serverLevel());
            namesForPlayer.put(player.getUUID(), player.getName().getString());
            PacketHandler.sendToAllPlayers(new SyncPlayerTeleportationPlayerNamesPacket(player.getUUID().toString(), player.getName().getString()));
        }

        PacketHandler.sendToAllPlayers(new SyncPlayerTeleportationOnlinePlayersPacket(new ArrayList<>(onlinePlayers.stream().map(UUID::toString).toList())));
    }

    public static void setOnlinePlayers(List<UUID> players) {
        onlinePlayers.clear();
        onlinePlayers.addAll(players);
    }

    public static void setNameForPlayer(UUID uuid, String name) {
        namesForPlayer.put(uuid, name);
    }
}