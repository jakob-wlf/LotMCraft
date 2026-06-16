package de.jakob.lotm.acting;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncPlayerActingDataPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ActingHelper {

    private static final String NBT_COOLDOWN_KEY = "lotm_acting_cooldowns";
    public static final String NBT_UNLOCKED_KEY = "lotm_unlocked_acting_triggers";

    public static boolean isOnCooldown(Player player, String taskId) {
        CompoundTag cooldowns = player.getPersistentData().getCompound(NBT_COOLDOWN_KEY);
        if (!cooldowns.contains(taskId)) return false;
        long expiry = cooldowns.getLong(taskId);
        long now = player.level().getGameTime();

        // If expiry is suspiciously far in the future, ignore it. I think this can only happen if someone manually adjusts the NBT data of the world but just to be safe :)
        if (expiry > now + 20 * 60 * 60 * 24) {
            cooldowns.remove(taskId);
            player.getPersistentData().put(NBT_COOLDOWN_KEY, cooldowns);
            return false;
        }
        return now < expiry;
    }

    public static void setCooldown(Player player, String taskId, long cooldownTicks) {
        if (cooldownTicks <= 0) return;
        CompoundTag cooldowns = player.getPersistentData().getCompound(NBT_COOLDOWN_KEY);
        cooldowns.putLong(taskId, player.level().getGameTime() + cooldownTicks);
        player.getPersistentData().put(NBT_COOLDOWN_KEY, cooldowns);
    }

    public static void unlockTrigger(String pathway, int sequence, Player player, String triggerId) {
        if(!(player instanceof ServerPlayer serverPlayer))
            return;

        CompoundTag unlocked = player.getPersistentData().getCompound(NBT_UNLOCKED_KEY);
        String key = pathway + "_" + sequence + "_" + triggerId;
        unlocked.putBoolean(key, true);
        player.getPersistentData().put(NBT_UNLOCKED_KEY, unlocked);

        PacketHandler.sendToPlayer(serverPlayer, new SyncPlayerActingDataPayload(unlocked));
    }

    public static boolean isTriggerUnlocked(String pathway, int sequence, Player player, String triggerId) {
            CompoundTag unlocked = player.getPersistentData().getCompound(NBT_UNLOCKED_KEY);
            String key = pathway + "_" + sequence + "_" + triggerId;
            return unlocked.getBoolean(key);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag unlocked = player.getPersistentData().getCompound(NBT_UNLOCKED_KEY);
            PacketHandler.sendToPlayer(serverPlayer, new SyncPlayerActingDataPayload(unlocked));
        }
    }

    @SubscribeEvent
    public static void onCopyPlayerData(PlayerEvent.Clone event) {
        if(event.getEntity() instanceof ServerPlayer serverPlayer) {
            CompoundTag unlocked = serverPlayer.getPersistentData().getCompound(NBT_UNLOCKED_KEY);
            PacketHandler.sendToPlayer(serverPlayer, new SyncPlayerActingDataPayload(unlocked));
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if(player instanceof ServerPlayer serverPlayer) {
            CompoundTag unlocked = player.getPersistentData().getCompound(NBT_UNLOCKED_KEY);
            PacketHandler.sendToPlayer(serverPlayer, new SyncPlayerActingDataPayload(unlocked));
        }
    }
}