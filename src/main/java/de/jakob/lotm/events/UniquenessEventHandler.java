package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.entity.custom.uniqueness.UniquenessEntity;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class UniquenessEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        if (entity instanceof Player player) {
            UniquenessComponent comp = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
            if (comp.hasUniqueness()) {
                serverLevel.getServer().execute(() -> {
                    comp.setHasUniqueness(false);
                    comp.setUniquenessPathway("");
                    BeyonderData.playerMap.setUniqueness(player, "none");
                    if (player instanceof ServerPlayer sp) {
                        PacketHandler.syncUniquenessToPlayer(sp);
                    }
                });
            }
        }

        Entity killer = event.getSource().getEntity();
        if (killer instanceof ServerPlayer killerPlayer) {
            UniquenessComponent killerComp = killerPlayer.getData(ModAttachments.UNIQUENESS_COMPONENT);
            if (killerComp.hasUniqueness()) {
                killerComp.incrementKillCount();
                PacketHandler.syncUniquenessToPlayer(killerPlayer);
            }
        }
    }
}
