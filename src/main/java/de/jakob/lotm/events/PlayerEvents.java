package de.jakob.lotm.events;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ToggleAbilityItem.cleanupEntity(player.getUUID());
        }
    }

    private static final Random random = new Random();

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(DivinationAbility.dangerPremonitionActive.contains(event.getEntity().getUUID()) && random.nextFloat() < .1) {
            event.setCanceled(true);
            if(event.getEntity() instanceof ServerPlayer player) {
                Component actionBarText = Component.literal("Dodged Attack").withStyle(ChatFormatting.DARK_PURPLE);
                sendActionBar(player, actionBarText);
            }
        }
        if(NightmareAbility.hasActiveNightmare(event.getEntity())) {
            if(event.getAmount() >= event.getEntity().getHealth()) {
                event.setCanceled(true);
                event.getEntity().setHealth(event.getEntity().getMaxHealth());
                NightmareAbility.stopNightmare(event.getEntity().getUUID());
            }
        }
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }
}