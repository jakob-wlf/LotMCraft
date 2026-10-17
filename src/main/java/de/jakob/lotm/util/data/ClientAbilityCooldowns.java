package de.jakob.lotm.util.data;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side mirror of the local player's ability cooldowns, fed by SyncAbilityCooldownPacket.
 * Stores the level game time the cooldown ends at, so it counts down without further packets.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class ClientAbilityCooldowns {

    private record Entry(long endTime, int totalTicks) {}

    private static final Map<String, Entry> cooldowns = new HashMap<>();

    public static void set(String abilityId, int ticks) {
        if (ticks <= 0) {
            cooldowns.remove(abilityId);
            return;
        }
        cooldowns.put(abilityId, new Entry(now() + ticks, ticks));
    }

    public static void clear() {
        cooldowns.clear();
    }

    public static int getRemainingTicks(String abilityId) {
        Entry entry = cooldowns.get(abilityId);
        if (entry == null) return 0;
        long remaining = entry.endTime - now();
        if (remaining <= 0) {
            cooldowns.remove(abilityId);
            return 0;
        }
        return (int) remaining;
    }

    /** Fraction of the cooldown still remaining, 1 = just used, 0 = ready. */
    public static float getRemainingFraction(String abilityId, float partialTick) {
        Entry entry = cooldowns.get(abilityId);
        if (entry == null) return 0f;
        float remaining = entry.endTime - now() - partialTick;
        return Math.clamp(remaining / entry.totalTicks, 0f, 1f);
    }

    private static long now() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0 : mc.level.getGameTime();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
