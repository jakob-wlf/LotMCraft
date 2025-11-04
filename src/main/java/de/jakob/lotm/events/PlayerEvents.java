package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.ToggleAbilityItem;
import de.jakob.lotm.abilities.common.DivinationAbility;
import de.jakob.lotm.abilities.darkness.NightmareAbility;
import de.jakob.lotm.abilities.red_priest.CullAbility;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;

import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ToggleAbilityItem.cleanupEntity(player.level(), player);
        }
    }

    private static final Random random = new Random();

    private static final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(.05f, 0, 0),
            1.5f
    );

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
        Entity damager = event.getSource().getEntity();
        if(damager instanceof LivingEntity source && ((CullAbility) AbilityItemHandler.CULL.get()).isActive(source)) {
            Level level = event.getEntity().level();
            if(!level.isClientSide) {
                ParticleUtil.spawnParticles((ServerLevel) level, dust, event.getEntity().getEyePosition().subtract(0, .4, 0), 40, .4, .8, .4, 0);
            }
        }
    }

    private static void sendActionBar(ServerPlayer player, Component message) {
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(message);
        player.connection.send(packet);
    }
}