package de.jakob.lotm.util.helper.subordinates;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.entity.custom.goals.SubordinateFollowGoal;
import de.jakob.lotm.entity.custom.goals.SubordinateLoadChunksGoal;
import de.jakob.lotm.entity.custom.goals.SubordinateTargetGoal;
import de.jakob.lotm.entity.custom.goals.SuordinateStayGoal;
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

public class SubordinateUtils {
    public static boolean turnEntityIntoSubordinate(LivingEntity entity, Player controller) {
        if (entity instanceof Player) {
            return false; // Players cannot be turned into marionettes
        }
        
        SubordinateComponent component = entity.getData(ModAttachments.SUBORDINATE_COMPONENT.get());
        if (component.isSubordinate()) {
            return false; // Already a marionette
        }
        
        // Set marionette data
        component.setSubordinate(true);
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

            mob.goalSelector.addGoal(0, new SubordinateFollowGoal(mob));
            mob.goalSelector.addGoal(0, new SubordinateLoadChunksGoal(mob));
            mob.goalSelector.addGoal(1, new SuordinateStayGoal(mob));
            mob.targetSelector.addGoal(0, new SubordinateTargetGoal(mob));
            mob.setTarget(null);
        }

        ItemStack controllerItem = createController(entity);


        if (!controller.getInventory().add(controllerItem)) {
            controller.drop(controllerItem, false);
        }
        
        return true;
    }
    
    public static ItemStack createController(LivingEntity marionette) {
        ItemStack controller = new ItemStack(ModItems.SUBORDINATE_CONTROLLER.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("SubordinateUUID", marionette.getStringUUID());
        tag.putString("SubordinateType", marionette.getType().getDescription().getString());
        controller.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        controller.set(DataComponents.CUSTOM_NAME, Component.literal("Subordinate Controller (" +
                marionette.getType().getDescription().getString() + ")"));

        if(BeyonderData.isBeyonder(marionette)) {
            if (BeyonderData.isBeyonder(marionette)) {
                controller.set(
                        DataComponents.LORE,
                        new ItemLore(List.of(
                                Component.literal("-------------------").withStyle(style -> style.withColor(0xFFf54242).withItalic(false)),
                                Component.translatable("lotm.pathway").append(Component.literal(": ")).append(Component.literal(BeyonderData.pathwayInfos.get(BeyonderData.getPathway(marionette)).getSequenceName(9))).withColor(0xc96f6f).withStyle(style -> style.withItalic(false)),
                                Component.translatable("lotm.sequence").append(Component.literal(": ")).append(Component.literal(BeyonderData.getSequence(marionette) + "")).withColor(0xc96f6f).withStyle(style -> style.withItalic(false))
                        )));
            }
        }

        return controller;
    }
}