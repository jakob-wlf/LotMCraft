package de.jakob.lotm.beyonders.boss;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public final class BossEvents {
    private BossEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob && BossRegistry.getDefinition(mob).isPresent()) {
            BossRuntime.refresh(mob);
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide()) {
            BossRuntime.unapplyGoals(mob);
            BossRuntime.removeBossBar(mob);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        BossRegistry.getDefinition(mob).ifPresentOrElse(
            definition -> {
                BossRuntime.applyGoals(mob);
                BossRuntime.enforceTargetPolicy(mob, definition);
                BossRuntime.tick(mob, definition);
                BossRuntime.tickRetaliation(mob, definition);
            },
            () -> {
                BossRuntime.unapplyGoals(mob);
                BossRuntime.removeBossBar(mob);
            }
        );
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        BossDefinition definition = BossRegistry.getDefinition(mob).orElse(null);
        if (definition == null) {
            return;
        }
        LivingEntity candidate = event.getNewAboutToBeSetTarget();
        if (candidate == null) {
            return;
        }
        if (!(candidate instanceof Player player)) {
            event.setNewAboutToBeSetTarget(null);
            return;
        }
        if (definition.hostility() == BossDefinition.Hostility.RETALIATE && !BossRuntime.isRetaliationTarget(mob, player)) {
            event.setNewAboutToBeSetTarget(null);
            return;
        }
        if (mob.getTarget() != player) {
            BossRuntime.alertPlayer(mob, player, definition);
        }
    }

    @SubscribeEvent
    public static void onLivingAttacked(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        BossDefinition definition = BossRegistry.getDefinition(mob).orElse(null);
        if (definition == null || definition.hostility() != BossDefinition.Hostility.RETALIATE) {
            return;
        }
        if (event.getSource().getEntity() instanceof Player player) {
            BossRuntime.rememberRetaliation(mob, player);
            mob.setTarget(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            BossRuntime.removeBossBar(mob);
        }
    }
}
