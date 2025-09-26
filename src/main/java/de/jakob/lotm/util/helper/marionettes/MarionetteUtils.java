package de.jakob.lotm.util.helper.marionettes;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.MarionetteGoal;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Set;

public class MarionetteUtils {
    
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
        
        // Clear existing goals and add marionette goals
        if (entity instanceof Mob mob) {
            // Clear all existing goals
            mob.goalSelector.getAvailableGoals().clear();
            mob.targetSelector.getAvailableGoals().clear();
            
            // Add marionette goals
            mob.goalSelector.addGoal(0, new MarionetteGoal(mob));
        }

        ItemStack controllerItem = createMarionetteController(entity);
        if(BeyonderData.isBeyonder(entity)) {
            if (BeyonderData.isBeyonder(entity)) {
                controllerItem.set(
                        DataComponents.LORE,
                        new ItemLore(List.of(
                                Component.literal("-------------------").withStyle(style -> style.withColor(0xFF5555).withItalic(false)),
                                Component.translatable("lotm.pathway").append(Component.literal(": ")).append(Component.literal(BeyonderData.pathwayInfos.get(BeyonderData.getPathway(entity)).getSequenceName(9))).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                Component.translatable("lotm.sequence").append(Component.literal(": ")).append(Component.literal(BeyonderData.getSequence(entity) + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false))
                        )));
            }
        }

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