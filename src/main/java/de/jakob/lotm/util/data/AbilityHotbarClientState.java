package de.jakob.lotm.util.data;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.RequestAbilityBarPacket;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Client-only toggle that swaps the vanilla item hotbar for the 2-row ability hotbar.
 * While active, the number keys cast ability bar abilities instead of switching item slots.
 */
@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class AbilityHotbarClientState {

    private static boolean enabled = false;

    public static void toggle() {
        enabled = !enabled;
        if (enabled) {
            PacketHandler.sendToServer(new RequestAbilityBarPacket());
        }
    }

    public static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        return enabled && mc.player != null && ClientBeyonderCache.isBeyonder(mc.player.getUUID());
    }

    /** Compact label for a slot keybind, e.g. "1" or "^1" for Ctrl+1, so it fits under a 16px icon. */
    public static String shortKeyLabel(KeyMapping keyMapping) {
        if (keyMapping == null || keyMapping.isUnbound()) return "-";
        String prefix = switch (keyMapping.getKeyModifier()) {
            case CONTROL -> "^";
            case SHIFT -> "S";
            case ALT -> "A";
            default -> "";
        };
        return prefix + keyMapping.getKey().getDisplayName().getString();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        enabled = false;
    }
}
