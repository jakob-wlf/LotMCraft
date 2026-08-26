package de.jakob.lotm.beyonders.abilities.fool.marionettes;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MarionetteComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.goals.*;
import de.jakob.lotm.entity.goals.EntityLoadChunksGoal;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MarionetteUtils {

    public static boolean isMarionette(LivingEntity entity) {
        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT);
        return component.isMarionette();
    }

    public static boolean turnEntityIntoMarionette(LivingEntity entity, Player controller) {
        if (entity instanceof Player) {
            return false;
        }

        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (component.isMarionette()) {
            return false;
        }

        component.setMarionette(true);
        component.setControllerUUID(controller.getStringUUID());
        component.setCurrentMode(MarionetteComponent.MarionetteMode.FOLLOW);
        component.setShouldAttack(true);

        if (entity instanceof Mob mob) {
            mob.targetSelector.removeAllGoals(goal ->
                    goal instanceof StrollThroughVillageGoal ||
                    goal instanceof BreedGoal ||
                    goal instanceof MoveToBlockGoal ||
                    goal instanceof PanicGoal ||
                    goal instanceof RandomStrollGoal ||
                    goal instanceof TargetGoal
            );

            mob.goalSelector.addGoal(0, new MarionetteMovementGoal(mob));
            mob.goalSelector.addGoal(0, new EntityLoadChunksGoal(mob));
            mob.goalSelector.addGoal(1, new MarionetteMovementGoal(mob));
            mob.goalSelector.addGoal(1, new MarionetteUseAbilityGoal(mob));
            mob.targetSelector.addGoal(0, new MarionetteTargetGoal(mob));
            mob.goalSelector.addGoal(10, new MarionetteMaxDistanceGoal(mob));
            mob.setTarget(null);

            controller.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).addMarionette(entity.getUUID());
        }

        return true;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(!(entity.level() instanceof ServerLevel)) {
            return;
        }
        if (!isMarionette(entity)) {
            return;
        }

        String ownerUUID = entity.getData(ModAttachments.MARIONETTE_COMPONENT).getControllerUUID();
        Player owner = findPlayerAcrossAllLevels(ownerUUID, entity);
        if(owner != null) {
            owner.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT).removeMarionette(entity.getUUID());
        }
    }

    private static Player findPlayerAcrossAllLevels(String uuidString, LivingEntity marionette) {
        try {
            UUID uuid = UUID.fromString(uuidString);

            if (marionette.getServer() != null) {
                for (ServerLevel level : marionette.getServer().getAllLevels()) {
                    Player player = level.getPlayerByUUID(uuid);
                    if (player != null) {
                        return player;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
        }

        return null;
    }
}