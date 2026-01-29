package de.jakob.lotm.abilities.error;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GiftAbility extends Ability {
    public GiftAbility(String id) {
        super(id, 1);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 30;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player)) {
            if(entity instanceof Player player && entity.level().isClientSide) {
                player.playSound(SoundEvents.BELL_RESONATE, 1, 1);
            }
            return;
        }

        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        if(offHandItem.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_item").withColor(0x6d32a8));
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.gift.no_target").withColor(0x6d32a8));
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.GIFTING_PARTICLES, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), player);

        if(!isItemWithEffect(offHandItem)) {
            var capability = target.getCapability(Capabilities.ItemHandler.ENTITY);

            if(capability != null && hasInventory(target)) {
                ItemStack toInsert = offHandItem.copy();

                // Try to insert into all slots
                for(int i = 0; i < capability.getSlots(); i++) {
                    toInsert = capability.insertItem(i, toInsert, false);
                    if(toInsert.isEmpty()) {
                        break; // Successfully inserted everything
                    }
                }

                // Drop any remainder
                if(!toInsert.isEmpty()) {
                    target.spawnAtLocation(toInsert);
                }
            } else {
                // No inventory or capability, drop at target's location
                target.spawnAtLocation(offHandItem.copy());
            }
        }
        else {
            handleGiftEffect(level, entity, target, offHandItem);
        }

        offHandItem.setCount(0);
    }

    private void handleGiftEffect(Level level, LivingEntity entity, LivingEntity target, ItemStack offHandItem) {
        if(offHandItem.is(Items.TNT)) {
            Random rand = new Random();
            for(int i = 0; i < Math.min(offHandItem.getCount(), 30); i++) {
                PrimedTnt tnt = new PrimedTnt(level, target.getX() + rand.nextDouble(-1, 1), target.getY() + rand.nextDouble(-1, 1), target.getZ() + rand.nextDouble(-1, 1), entity);
                tnt.setFuse(10);
                level.addFreshEntity(tnt);
            }
        }
        else if(offHandItem.is(Items.ANVIL)) {
            for(int i = 0; i < offHandItem.getCount(); i++) {
                FallingBlockEntity anvil = FallingBlockEntity.fall(level, BlockPos.containing(target.getX(), target.getY() + 10 + i, target.getZ()), Blocks.ANVIL.defaultBlockState());
                anvil.disableDrop();
                anvil.setHurtsEntities(6, 50);
            }
        }
        else if(offHandItem.is(Items.ENDER_PEARL)) {
            target.teleportTo(entity.getX(), entity.getY(), entity.getZ());
        }
        else if(offHandItem.is(Items.FIRE_CHARGE)) {
            target.setRemainingFireTicks(target.getRemainingFireTicks() + 20 * offHandItem.getCount());
        }
        else if(offHandItem.is(Items.LAVA_BUCKET)) {
            target.setRemainingFireTicks(target.getRemainingFireTicks() + 30 * offHandItem.getCount());
        }
        else if(offHandItem.is(Items.WATER_BUCKET)) {
            for(int i = 0; i < offHandItem.getCount(); i++) {
                target.clearFire();
            }
        }
    }

    public boolean hasInventory(Entity entity) {
        return entity.getCapability(Capabilities.ItemHandler.ENTITY) != null;
    }

    private final Item[] itemsWithEffects = new Item[]{
            Items.TNT,
            Items.ANVIL,
            Items.ENDER_PEARL,
            Items.FIRE_CHARGE,
            Items.LAVA_BUCKET,
            Items.WATER_BUCKET,

    };

    private boolean isItemWithEffect(ItemStack offHandItem) {
        for (Item item : itemsWithEffects) {
            if (offHandItem.is(item)) {
                return true;
            }
        }
        return false;
    }
}
