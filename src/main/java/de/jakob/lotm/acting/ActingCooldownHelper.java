package de.jakob.lotm.acting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class ActingCooldownHelper {

    private static final String NBT_KEY = "lotm_acting_cooldowns";

    public static boolean isOnCooldown(Player player, String taskId) {
        CompoundTag cooldowns = player.getPersistentData().getCompound(NBT_KEY);
        if (!cooldowns.contains(taskId)) return false;
        long expiry = cooldowns.getLong(taskId);
        long now = player.level().getGameTime();

        // If expiry is suspiciously far in the future, ignore it. I think this can only happen if someone manually adjusts the NBT data of the world but just to be safe :)
        if (expiry > now + 20 * 60 * 60 * 24) {
            cooldowns.remove(taskId);
            player.getPersistentData().put(NBT_KEY, cooldowns);
            return false;
        }
        return now < expiry;
    }

    public static void setCooldown(Player player, String taskId, long cooldownTicks) {
        if (cooldownTicks <= 0) return;
        CompoundTag cooldowns = player.getPersistentData().getCompound(NBT_KEY);
        cooldowns.putLong(taskId, player.level().getGameTime() + cooldownTicks);
        player.getPersistentData().put(NBT_KEY, cooldowns);
    }
}