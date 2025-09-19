package de.jakob.lotm.abilities.demoness.handlers;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.demoness.InvisibilityAbility;
import de.jakob.lotm.abilities.demoness.ShadowConcealmentAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
public class InvisibilityClientHandler {

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (ShadowConcealmentAbility.invisiblePlayers.contains(player.getUUID()) || InvisibilityAbility.invisiblePlayers.contains(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && (ShadowConcealmentAbility.invisiblePlayers.contains(player.getUUID()) || InvisibilityAbility.invisiblePlayers.contains(player.getUUID()))) {
            event.setCanceled(true);
        }
    }
}
