package de.jakob.lotm.util.helper.marionettes;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.*;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class MarionetteUtils {

    public static boolean isMarionette(LivingEntity entity) {
        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT);
        return component.isMarionette();
    }

    public static boolean turnEntityIntoMarionette(LivingEntity entity, Player controller) {
        if (entity instanceof Player) {
            return false; // Players cannot be turned into marionettes
        }
        
        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (component.isMarionette()) {
            return false; // Already a marionette
        }
        
        // Set marionette data
        component.setMarionette(true);
        component.setControllerUUID(controller.getStringUUID());
        component.setFollowMode(true);
        component.setShouldAttack(true);
        
        // Clear existing goals and add marionette goals
        if (entity instanceof Mob mob) {
            // Remove hostile targeting goals
            mob.targetSelector.removeAllGoals(goal ->
                    goal instanceof StrollThroughVillageGoal ||
                    goal instanceof BreedGoal ||
                    goal instanceof MoveToBlockGoal ||
                    goal instanceof PanicGoal ||
                    goal instanceof RandomStrollGoal ||
                    goal instanceof TargetGoal
            );

            mob.goalSelector.addGoal(0, new MarionetteFollowGoal(mob));
            mob.goalSelector.addGoal(0, new MarionetteLoadChunksGoal(mob));
            mob.goalSelector.addGoal(1, new MarionetteStayGoal(mob));
            mob.goalSelector.addGoal(1, new MarionetteUseAbilityGoal(mob));
            mob.targetSelector.addGoal(0, new MarionetteTargetGoal(mob));
            mob.goalSelector.addGoal(10, new MarionetteLifelinkGoal(mob));
            mob.setTarget(null);
        }

        ItemStack controllerItem = createMarionetteController(entity);


        if (!controller.getInventory().add(controllerItem)) {
            controller.drop(controllerItem, false);
        }
        
        return true;
    }
    
    public static ItemStack createMarionetteController(LivingEntity marionette) {
        ItemStack controller = new ItemStack(ModItems.MARIONETTE_CONTROLLER.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("MarionetteUUID", marionette.getStringUUID());
        tag.putString("MarionetteType", marionette.getType().getDescription().getString());
        controller.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        controller.set(DataComponents.CUSTOM_NAME, Component.literal("Marionette Controller (" +
                marionette.getType().getDescription().getString() + ")"));

        if(BeyonderData.isBeyonder(marionette)) {
            if (BeyonderData.isBeyonder(marionette)) {
                controller.set(
                        DataComponents.LORE,
                        new ItemLore(List.of(
                                Component.literal("-------------------").withStyle(style -> style.withColor(0xFFa742f5).withItalic(false)),
                                Component.translatable("lotm.pathway").append(Component.literal(": ")).append(Component.literal(BeyonderData.pathwayInfos.get(BeyonderData.getPathway(marionette)).getSequenceName(9))).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                Component.translatable("lotm.sequence").append(Component.literal(": ")).append(Component.literal(BeyonderData.getSequence(marionette) + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false))
                        )));
            }
        }

        return controller;
    }
    
    public static void releaseMarionetteControl(LivingEntity entity) {
        MarionetteComponent component = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        component.setMarionette(false);
        component.setControllerUUID("");
        component.setFollowMode(false);
        
        if (entity instanceof Mob mob) {
            mob.goalSelector.getAvailableGoals().clear();
            mob.targetSelector.getAvailableGoals().clear();
        }
    }
}