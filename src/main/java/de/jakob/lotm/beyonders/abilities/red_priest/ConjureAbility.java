package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ConjureAbility extends ToggleAbility {
    public ConjureAbility(String id) {
        super(id, "burning");
    }
    private final int[] SWORDS = new int[]{7, 8, 9};
    private static ItemStack sword;

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(entity.level().isClientSide) return;
        if(!hasSword((Player) entity)) {
            cancel((ServerLevel) level, entity);
            return;
        }
        if(sword == null) {
            cancel((ServerLevel) level, entity);
            return;
        }
        if(BeyonderData.getSpirituality(entity) <= 5) {
            cancel((ServerLevel) level, entity);
            entity.sendSystemMessage(Component.literal("You exhausted your spirituality and your weapon vanished, take a rest").withStyle(ChatFormatting.RED));
            return;
        }
        BeyonderData.reduceSpirituality(entity, 5);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(entity.level().isClientSide) return;
        Player player = (Player) entity;
        if(hasSword(player)) removeSword(player);
        if(!hasEmptyHotbarSlot((Player) entity)) {
            entity.sendSystemMessage(Component.literal("You need empty slot in your hotbar").withStyle(ChatFormatting.RED));
            return;
        }
        sword = getSword(player);
        BeyonderData.reduceSpirituality(player, 50);
    }


    @Override
    public void stop(Level level, LivingEntity entity) {
        if(entity.level().isClientSide) return;
        removeSword((Player) entity);
        sword = null;
    }

    // Protect the sword
    @SubscribeEvent
    public static void onDrop(ItemTossEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(sword == null) return;
        if(event.getEntity().getItem().is(ModItems.CONJURED_SWORD)) {
            event.getPlayer().getInventory().add(event.getEntity().getItem());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDroppedLoot(LivingDropsEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(sword == null) return;
        Iterator<ItemEntity> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemEntity drop = drops.next();
            ItemStack stack = drop.getItem();
            if(stack.is(ModItems.CONJURED_SWORD)) {
                event.getDrops().remove(drop);
                drops.remove();
                sword = null;
            }
        }
    }

    // Sword Usage

    // Burn blocks
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if(sword == null) return;
        if(event.getItemStack().is(ModItems.CONJURED_SWORD)) {
            Direction faceClicked = event.getFace();
            Level level = event.getLevel();
            if (faceClicked == null) return;

            BlockPos firePos = event.getPos().relative(faceClicked);

            if (level.isEmptyBlock(firePos)) {
                BlockState fireState = Blocks.FIRE.defaultBlockState();
                level.setBlockAndUpdate(firePos, fireState);
                event.getEntity().swing(event.getHand(), true);
                event.setCanceled(true);
            }
        }
    }

    // Explosive attacks
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if(sword == null) return;
        if (event.getEntity().getMainHandItem().is(ModItems.CONJURED_SWORD)) {
            Level level = event.getEntity().level();
            Player player = event.getEntity();
            Entity target = event.getTarget();
//            event.setCanceled(true);
//            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC), (float) DamageLookup.lookupDamage(7, 1.8f)*(float) AbilityUtil.getMultiplierWithArt(player, new ConjureAbility("conjure_ability")));
            ExplosionDamageCalculator zeroDamageCalc = new ExplosionDamageCalculator() {
                @Override
                public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState blockState, FluidState fluidState) {
                    return Optional.empty();
                }

                @Override
                public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                    if (entity.is(event.getTarget()) || entity.is(player)) return false;
                    return true;
                }
            };

            level.explode(
                    player,
                    ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC),
                    zeroDamageCalc,
                    target.getX(), target.getY(), target.getZ(),
                    2.0F,
                    false,
                    Level.ExplosionInteraction.NONE
            );

        }
    }
    private boolean hasSword(Player player) {
        for(ItemStack item: player.getInventory().items) {
            if(item.is(ModItems.CONJURED_SWORD)) return true;
        }
        return false;
    }

    private ItemStack getSword(Player entity) {
        int Seq = BeyonderData.getSequence(entity);
        int SWORD = (Seq <= 2) ? SWORDS[2] : (Seq <= 4) ? SWORDS[1] : SWORDS[0];
        ItemStack stack = new ItemStack((ItemLike) ModItems.CONJURED_SWORD);
        AttributeModifier damageModifier = new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("mymod", "custom_weapon_damage"),
                SWORD,
                AttributeModifier.Operation.ADD_VALUE
        );

        // 3. Create the modifiers component specifically targeting the MAINHAND slot
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, damageModifier, EquipmentSlotGroup.MAINHAND)
                .build();

        // 4. Apply the component to the item stack
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        int item = getNearestEmpty(entity);
        entity.getInventory().setItem(item, stack);
        return stack;
    }

    private void removeSword(Player entity) {
        for(ItemStack stack : entity.getInventory().items) {
            if(stack.is(ModItems.CONJURED_SWORD)) {
                entity.getInventory().removeItem(stack);
            }
        }
    }

    private int getNearestEmpty(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return i; // Found an empty hotbar slot
            }
        }
        return 0;
    }

    public boolean hasEmptyHotbarSlot(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return true; // Found an empty hotbar slot
            }
        }
        return false; // Hotbar is completely full
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

}
