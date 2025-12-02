package de.jakob.lotm.abilities.error.handler;

import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TheftHandler {

    public static List<TheftLoot> getLootForEntity(LivingEntity entity) {
        List<TheftLoot> loot = new ArrayList<>();

        // --- Passive mobs ---
        if (entity instanceof Sheep sheep) {
            loot.add(new TheftLoot(new ItemStack(Items.WHITE_WOOL), 1, 3));
        } else if (entity instanceof Cow) {
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 1, 2));
        } else if (entity instanceof Pig) {
            loot.add(new TheftLoot(new ItemStack(Items.PORKCHOP), 1, 2));
        } else if (entity instanceof Chicken) {
            loot.add(new TheftLoot(new ItemStack(Items.FEATHER), 1, 5));
        } else if (entity instanceof Villager) {
            loot.add(new TheftLoot(new ItemStack(Items.EMERALD), 1, 4));
        } else if (entity instanceof IronGolem) {
            loot.add(new TheftLoot(new ItemStack(Items.IRON_INGOT), 1, 4));
        }


        // --- Undead mobs ---
        else if (entity instanceof Skeleton) {
            loot.add(new TheftLoot(new ItemStack(Items.BONE), 1, 2));
        } else if (entity instanceof Zombie) {
            loot.add(new TheftLoot(new ItemStack(Items.ROTTEN_FLESH), 1, 2));
        }

        // --- Aquatic ---
        else if (entity instanceof Guardian) {
            loot.add(new TheftLoot(new ItemStack(Items.PRISMARINE_CRYSTALS), 1, 1));
        }

        // --- Hostile mobs ---
        else if (entity instanceof Creeper) {
            loot.add(new TheftLoot(new ItemStack(Items.GUNPOWDER), 1, 3));
        } else if (entity instanceof EnderMan) {
            loot.add(new TheftLoot(new ItemStack(Items.ENDER_PEARL), 1, 1));
        } else if (entity instanceof Witch) {
            loot.add(new TheftLoot(new ItemStack(Items.REDSTONE), 1, 2));
            loot.add(new TheftLoot(new ItemStack(Items.GLOWSTONE_DUST), 1, 2));
        }

        // --- Nether ---
        else if (entity instanceof Piglin) {
            loot.add(new TheftLoot(new ItemStack(Items.GOLD_NUGGET), 1, 4));
        } else if (entity instanceof Blaze) {
            loot.add(new TheftLoot(new ItemStack(Items.BLAZE_POWDER), 1, 1));
        }

        return loot;
    }

    public static void stealItemsFromEntity(LivingEntity target, Player thief) {
        if(!(target.level() instanceof ServerLevel)) {
            return;
        }

        if(BeyonderData.isBeyonder(target) && AbilityUtil.isTargetSignificantlyStronger(thief, target)) {
            return;
        }

        if(target instanceof Player player) {
            stealFromPlayer(player, thief);
        }
        else {
            stealFromMob(target, thief);
        }
    }

    private static void stealFromMob(LivingEntity target, Player thief) {
        List<TheftLoot> possibleLoot = getLootForEntity(target);
        if (possibleLoot.isEmpty()) {
            return;
        }

        TheftLoot lootToSteal = possibleLoot.get((new Random()).nextInt(possibleLoot.size()));
        int amountToSteal = lootToSteal.minAmount + (new Random()).nextInt(lootToSteal.maxAmount - lootToSteal.minAmount + 1);
        ItemStack stolenItem = lootToSteal.loot.copy();
        stolenItem.setCount(amountToSteal);
        thief.getInventory().add(stolenItem);
    }

    private static void stealFromPlayer(Player player, Player thief) {
        ArrayList<Integer> nonEmptySlots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (!player.getInventory().items.get(i).isEmpty()) {
                nonEmptySlots.add(i);
            }
        }

        if (nonEmptySlots.isEmpty()) {
            return;
        }

        int slotToSteal = nonEmptySlots.get((new Random()).nextInt(nonEmptySlots.size()));
        ItemStack stolenItem = player.getInventory().items.get(slotToSteal).copy();
        player.getInventory().items.set(slotToSteal, ItemStack.EMPTY);
        thief.getInventory().add(stolenItem);
    }

    public record TheftLoot(ItemStack loot, int minAmount, int maxAmount) {

    }

}
