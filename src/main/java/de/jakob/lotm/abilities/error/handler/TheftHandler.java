package de.jakob.lotm.abilities.error.handler;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TheftHandler {

    public static List<TheftLoot> getLootForEntity(LivingEntity entity) {
        List<TheftLoot> loot = new ArrayList<>();

        // --- Passive mobs ---
        if (entity instanceof Sheep) {
            loot.add(new TheftLoot(new ItemStack(Items.WHITE_WOOL), 1, 7));
            loot.add(new TheftLoot(new ItemStack(Items.MUTTON), 1, 5));
            loot.add(new TheftLoot(new ItemStack(Items.SHEARS), 0, 1));
        } else if (entity instanceof Cow) {
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 1, 6));
            loot.add(new TheftLoot(new ItemStack(Items.BEEF), 1, 5));
            loot.add(new TheftLoot(new ItemStack(Items.MILK_BUCKET), 0, 2));
        } else if (entity instanceof Pig) {
            loot.add(new TheftLoot(new ItemStack(Items.PORKCHOP), 1, 5));
            loot.add(new TheftLoot(new ItemStack(Items.CARROT), 0, 2));
        } else if (entity instanceof Chicken) {
            loot.add(new TheftLoot(new ItemStack(Items.FEATHER), 2, 9));
            loot.add(new TheftLoot(new ItemStack(Items.CHICKEN), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.EGG), 0, 4));
        } else if (entity instanceof Rabbit) {
            loot.add(new TheftLoot(new ItemStack(Items.RABBIT_HIDE), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.RABBIT), 1, 3));
            loot.add(new TheftLoot(new ItemStack(Items.RABBIT_FOOT), 0, 2));
        } else if (entity instanceof Turtle) {
            loot.add(new TheftLoot(new ItemStack(Items.TURTLE_SCUTE), 0, 2));
            loot.add(new TheftLoot(new ItemStack(Items.SEAGRASS), 0, 4));
        } else if (entity instanceof Villager) {
            loot.add(new TheftLoot(new ItemStack(Items.EMERALD), 1, 8));
            loot.add(new TheftLoot(new ItemStack(Items.BREAD), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.PAPER), 0, 3));
        } else if (entity instanceof IronGolem) {
            loot.add(new TheftLoot(new ItemStack(Items.IRON_INGOT), 2, 8));
            loot.add(new TheftLoot(new ItemStack(Items.POPPY), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.REDSTONE), 0, 2));
        } else if (entity instanceof Horse) {
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.SADDLE), 0, 1));
        } else if (entity instanceof Donkey) {
            loot.add(new TheftLoot(new ItemStack(Items.CHEST), 0, 1));
            loot.add(new TheftLoot(new ItemStack(Items.SADDLE), 0, 1));
        } else if (entity instanceof Mule) {
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 1, 3));
            loot.add(new TheftLoot(new ItemStack(Items.SADDLE), 0, 1));
        } else if (entity instanceof Llama) {
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 1, 3));
            loot.add(new TheftLoot(new ItemStack(Items.RED_CARPET), 0, 2));
        }

        // --- Tameable / neutral mobs ---
        else if (entity instanceof Wolf) {
            loot.add(new TheftLoot(new ItemStack(Items.BONE), 1, 3));
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 0, 2));
        } else if (entity instanceof Cat) {
            loot.add(new TheftLoot(new ItemStack(Items.STRING), 0, 2));
            loot.add(new TheftLoot(new ItemStack(Items.COD), 0, 2));
        } else if (entity instanceof Fox) {
            loot.add(new TheftLoot(new ItemStack(Items.RABBIT), 0, 2));
            loot.add(new TheftLoot(new ItemStack(Items.SWEET_BERRIES), 0, 3));
        } else if (entity instanceof Ocelot) {
            loot.add(new TheftLoot(new ItemStack(Items.COD), 0, 2));
        } else if (entity instanceof Parrot) {
            loot.add(new TheftLoot(new ItemStack(Items.PARROT_SPAWN_EGG), 0, 1));
        }

        // --- Aquatic ---
        else if (entity instanceof Cod) {
            loot.add(new TheftLoot(new ItemStack(Items.COD), 1, 4));
        } else if (entity instanceof Salmon) {
            loot.add(new TheftLoot(new ItemStack(Items.SALMON), 1, 4));
        } else if (entity instanceof TropicalFish) {
            loot.add(new TheftLoot(new ItemStack(Items.TROPICAL_FISH), 1, 3));
        } else if (entity instanceof Pufferfish) {
            loot.add(new TheftLoot(new ItemStack(Items.PUFFERFISH), 0, 2));
        } else if (entity instanceof Squid) {
            loot.add(new TheftLoot(new ItemStack(Items.INK_SAC), 1, 5));
        } else if (entity instanceof GlowSquid) {
            loot.add(new TheftLoot(new ItemStack(Items.GLOW_INK_SAC), 0, 2));
        } else if (entity instanceof Dolphin) {
            loot.add(new TheftLoot(new ItemStack(Items.COD), 0, 3));
            loot.add(new TheftLoot(new ItemStack(Items.NAUTILUS_SHELL), 0, 1));
        } else if (entity instanceof Guardian) {
            loot.add(new TheftLoot(new ItemStack(Items.PRISMARINE_CRYSTALS), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.PRISMARINE_SHARD), 1, 5));
            loot.add(new TheftLoot(new ItemStack(Items.COD), 0, 3));
        } else if (entity instanceof ElderGuardian) {
            loot.add(new TheftLoot(new ItemStack(Items.PRISMARINE_CRYSTALS), 2, 6));
            loot.add(new TheftLoot(new ItemStack(Items.SPONGE), 0, 2));
        }

        // --- Undead mobs ---
        else if (entity instanceof Skeleton) {
            loot.add(new TheftLoot(new ItemStack(Items.BONE), 1, 6));
            loot.add(new TheftLoot(new ItemStack(Items.ARROW), 0, 4));
        } else if (entity instanceof WitherSkeleton) {
            loot.add(new TheftLoot(new ItemStack(Items.BONE), 2, 6));
            loot.add(new TheftLoot(new ItemStack(Items.COAL), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.WITHER_SKELETON_SKULL), 0, 1));
        } else if (entity instanceof Zombie) {
            loot.add(new TheftLoot(new ItemStack(Items.ROTTEN_FLESH), 1, 6));
            loot.add(new TheftLoot(new ItemStack(Items.IRON_INGOT), 0, 2));
        } else if (entity instanceof Drowned) {
            loot.add(new TheftLoot(new ItemStack(Items.ROTTEN_FLESH), 2, 6));
            loot.add(new TheftLoot(new ItemStack(Items.COPPER_INGOT), 0, 3));
        } else if (entity instanceof Husk) {
            loot.add(new TheftLoot(new ItemStack(Items.ROTTEN_FLESH), 2, 6));
            loot.add(new TheftLoot(new ItemStack(Items.SAND), 0, 3));
        } else if (entity instanceof Phantom) {
            loot.add(new TheftLoot(new ItemStack(Items.PHANTOM_MEMBRANE), 1, 3));
        } else if (entity instanceof Endermite) {
            loot.add(new TheftLoot(new ItemStack(Items.ENDER_PEARL), 0, 1));
        }

        // --- Hostile mobs ---
        else if (entity instanceof Creeper) {
            loot.add(new TheftLoot(new ItemStack(Items.GUNPOWDER), 2, 7));
        } else if (entity instanceof EnderMan) {
            loot.add(new TheftLoot(new ItemStack(Items.ENDER_PEARL), 1, 3));
        } else if (entity instanceof Spider) {
            loot.add(new TheftLoot(new ItemStack(Items.STRING), 1, 6));
            loot.add(new TheftLoot(new ItemStack(Items.SPIDER_EYE), 0, 3));
        } else if (entity instanceof CaveSpider) {
            loot.add(new TheftLoot(new ItemStack(Items.STRING), 2, 7));
            loot.add(new TheftLoot(new ItemStack(Items.SPIDER_EYE), 1, 3));
        } else if (entity instanceof Slime) {
            loot.add(new TheftLoot(new ItemStack(Items.SLIME_BALL), 1, 6));
        } else if (entity instanceof MagmaCube) {
            loot.add(new TheftLoot(new ItemStack(Items.MAGMA_CREAM), 1, 4));
        } else if (entity instanceof Witch) {
            loot.add(new TheftLoot(new ItemStack(Items.REDSTONE), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.GLOWSTONE_DUST), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.SUGAR), 0, 3));
            loot.add(new TheftLoot(new ItemStack(Items.GLASS_BOTTLE), 0, 4));
        } else if (entity instanceof Ghast) {
            loot.add(new TheftLoot(new ItemStack(Items.GHAST_TEAR), 0, 2));
            loot.add(new TheftLoot(new ItemStack(Items.GUNPOWDER), 1, 4));
        }

        // --- Nether mobs ---
        else if (entity instanceof Piglin) {
            loot.add(new TheftLoot(new ItemStack(Items.GOLD_NUGGET), 2, 9));
            loot.add(new TheftLoot(new ItemStack(Items.GOLD_INGOT), 0, 3));
        } else if (entity instanceof PiglinBrute) {
            loot.add(new TheftLoot(new ItemStack(Items.GOLD_INGOT), 1, 4));
        } else if (entity instanceof Blaze) {
            loot.add(new TheftLoot(new ItemStack(Items.BLAZE_POWDER), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.BLAZE_ROD), 0, 2));
        }

        // --- End / stronghold ---
        else if (entity instanceof EnderDragon) {
            loot.add(new TheftLoot(new ItemStack(Items.DRAGON_BREATH), 1, 4));
            loot.add(new TheftLoot(new ItemStack(Items.DRAGON_EGG), 0, 1));
        } else if (entity instanceof Shulker) {
            loot.add(new TheftLoot(new ItemStack(Items.SHULKER_SHELL), 0, 2));
        } else if (entity instanceof Silverfish) {
            loot.add(new TheftLoot(new ItemStack(Items.STONE), 0, 3));
        }

        // --- Illager family ---
        else if (entity instanceof Evoker) {
            loot.add(new TheftLoot(new ItemStack(Items.TOTEM_OF_UNDYING), 0, 1));
        } else if (entity instanceof Vindicator) {
            loot.add(new TheftLoot(new ItemStack(Items.IRON_AXE), 0, 1));
        } else if (entity instanceof Pillager) {
            loot.add(new TheftLoot(new ItemStack(Items.CROSSBOW), 0, 1));
            loot.add(new TheftLoot(new ItemStack(Items.ARROW), 0, 4));
        } else if (entity instanceof Ravager) {
            loot.add(new TheftLoot(new ItemStack(Items.SADDLE), 0, 1));
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 1, 4));
        }

        // --- Misc / ambient ---
        else if (entity instanceof Bat) {
            loot.add(new TheftLoot(new ItemStack(Items.LEATHER), 0, 1));
        } else if (entity instanceof Bee) {
            loot.add(new TheftLoot(new ItemStack(Items.HONEY_BOTTLE), 0, 2));
            loot.add(new TheftLoot(new ItemStack(Items.HONEYCOMB), 0, 3));
        }

        return loot;
    }

    public static List<Item> getStealableItemsForEntity(LivingEntity entity) {
        List<Item> items = new ArrayList<>();

        for (TheftLoot loot : getLootForEntity(entity)) {
            items.add(loot.loot.getItem());
        }

        if(entity instanceof Player player) {
            for(ItemStack stack : player.getInventory().items) {
                if(!stack.isEmpty() && !items.contains(stack.getItem()) && !(stack.getItem() instanceof AbilityItem)) {
                    items.add(stack.getItem());
                }
            }
        }

        return items;
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
            if (!player.getInventory().items.get(i).isEmpty() && !(player.getInventory().items.get(i).getItem() instanceof AbilityItem)) {
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
