package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.packets.toServer.WhipSlashPayload;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ConjureAbility extends SelectableAbility {
    public ConjureAbility(String id) {
        super(id, 2.0f ,"burning");
    }
    private final int[] SWORDS = new int[]{7, 8, 9};
    private final int[] SP_COSTS = new int[]{50, 120, 250};
    public static boolean sword = false;
    private static int SP_COST;

    @Override
    protected String[] getAbilityNames() {
        return new String[] {
                "ability.lotmcraft.conjure.sword",
                "ability.lotmcraft.conjure.whip",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch(selectedAbility) {
            case 0 -> getSword(level, entity, ModItems.CONJURED_SWORD);
            case 1 -> getSword(level, entity, ModItems.CONJURED_WHIP);
        }
    }

    private void getSword(Level level, Entity entity1, DeferredItem<Item> weapon) {
        if(level.isClientSide()) return;
        if(!(entity1 instanceof Player entity)) return;
        int Seq = BeyonderData.getSequence(entity);
        SP_COST=(Seq <= 2) ? SP_COSTS[2] : (Seq <= 4) ? SP_COSTS[1] : SP_COSTS[0];
        int SWORD = (Seq <= 2) ? SWORDS[2] : (Seq <= 4) ? SWORDS[1] : SWORDS[0];

        AtomicBoolean stop = new AtomicBoolean(true);

        if(hasSword(entity, weapon)) {
            removeSword(entity, weapon);
            stop.set(true);
            return;
        }

        if(BeyonderData.getSpirituality(entity) < SP_COST) return;
        if(!hasEmptyHotbarSlot(entity)) {
            entity.sendSystemMessage(Component.literal("You need empty slot in your hotbar").withStyle(ChatFormatting.RED));
            return;
        }

        ItemStack stack = new ItemStack((ItemLike) weapon);

        editModifiers(SWORD, stack, weapon);

        int item = getNearestEmpty(entity);
        entity.getInventory().setItem(item, stack);
        sword = true;
        stop.set(false);

        BeyonderData.reduceSpirituality(entity, SP_COST);
        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            BeyonderData.reduceSpirituality(entity, SP_COST/10f);
            if(BeyonderData.getSpirituality(entity) <= 0) {
                removeSword(entity, weapon);
                stop.set(true);
                return;
            }
            if(!hasSword(entity, weapon)) {
                stop.set(true);
            }
        }, 5, null, stop);
    }



    private void removeSword(Player entity, DeferredItem<Item> weapon) {
        for(ItemStack stack : entity.getInventory().items) {
            if(stack.is(weapon)) {
                entity.getInventory().removeItem(stack);
            }
        }
    }


    // Protect the sword
    @SubscribeEvent
    public static void onDrop(ItemTossEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(!sword) return;
        if(event.getEntity().getItem().getItem() instanceof TieredItem tieredItem) {
            if (tieredItem.getTier() == ModItems.CONJURED_TOOL_TIER) {
                event.getPlayer().getInventory().add(event.getEntity().getItem());
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onDroppedLoot(LivingDropsEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(!sword) return;
        Iterator<ItemEntity> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            ItemEntity drop = drops.next();
            ItemStack stack = drop.getItem();
            if(stack.getItem() instanceof TieredItem tieredItem) {
                if(tieredItem.getTier() == ModItems.CONJURED_TOOL_TIER) {
                    event.getDrops().remove(drop);
                    drops.remove();
                    sword = false;
                }
            }

        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(!sword) return;
        for (ItemStack itemStack: event.getEntity().getInventory().items) {
            if(itemStack.getItem() instanceof TieredItem tieredItem) {
                if (tieredItem.getTier() == ModItems.CONJURED_TOOL_TIER) {
                    event.getEntity().getInventory().removeItem(itemStack);
                }
            }
        }

    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity().level().isClientSide) return;
        if(!sword) return;
        for (ItemStack itemStack: event.getEntity().getInventory().items) {
            if(itemStack.getItem() instanceof TieredItem tieredItem) {
                if (tieredItem.getTier() == ModItems.CONJURED_TOOL_TIER) {
                    event.getEntity().getInventory().removeItem(itemStack);
                }
            }
        }

    }

    // Sword Usage

    // Burn blocks
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if(!sword) return;
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

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player.getMainHandItem().is(ModItems.CONJURED_WHIP)) {
            PacketDistributor.sendToServer(new WhipSlashPayload());
        }
    }

    // Explosive attacks
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if(!sword) return;
        if(!(event.getEntity().getMainHandItem().getItem() instanceof TieredItem tieredItem)) return;
        if (tieredItem.getTier() == ModItems.CONJURED_TOOL_TIER) {
            Level level = event.getEntity().level();
            Player player = event.getEntity();
            Entity target = event.getTarget();
            if(event.getEntity().getMainHandItem().is(ModItems.CONJURED_WHIP)) {
                if (level instanceof ServerLevel serverLevel) {
                    Vec3 start = whipStart(player);
                    Vec3 end = new Vec3(
                            target.getX(),
                            target.getY() + target.getBbHeight() * 0.5,
                            target.getZ()
                    );
                    makeFireSlash(serverLevel, start, end);
                }
            }
            ExplosionDamageCalculator zeroDamageCalc = new ExplosionDamageCalculator() {
                @Override
                public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion explosion, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState blockState, @NotNull FluidState fluidState) {
                    return Optional.empty();
                }

                @Override
                public boolean shouldDamageEntity(@NotNull Explosion explosion, Entity entity) {
                    return !entity.is(event.getTarget()) && !entity.is(player);
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



    private boolean hasSword(Player player, DeferredItem<Item> weapon) {
        for(ItemStack item: player.getInventory().items) {
            if(item.is(weapon)) return true;
        }
        return false;
    }

    private void editModifiers(int SWORD, ItemStack stack, DeferredItem<Item> weapon) {
        if(weapon.equals(ModItems.CONJURED_SWORD)) {

            AttributeModifier damageModifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "custom_weapon_damage"),
                    SWORD,
                    AttributeModifier.Operation.ADD_VALUE
            );

            ItemAttributeModifiers damage = ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, damageModifier, EquipmentSlotGroup.MAINHAND)
                    .build();

            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, damage);
        } else if (weapon.equals(ModItems.CONJURED_WHIP)) {
            AttributeModifier damageModifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "custom_weapon_damage"),
                    SWORD-2,
                    AttributeModifier.Operation.ADD_VALUE
            );

            AttributeModifier rangeModifier = new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "custom_weapon_range"),
                    5,
                    AttributeModifier.Operation.ADD_VALUE
            );

            ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                    .add(Attributes.ENTITY_INTERACTION_RANGE, rangeModifier, EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_DAMAGE, damageModifier, EquipmentSlotGroup.MAINHAND)
                    .build();
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        }
    }

    private int getNearestEmpty(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return 0;
    }

    public boolean hasEmptyHotbarSlot(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void makeFireSlash(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 axis = end.subtract(start);
        double dist = Math.max(axis.length(), 0.1);
        Vec3 dir = axis.normalize();

        Vec3 ref = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 perp1 = dir.cross(ref).normalize();
        Vec3 perp2 = perp1.cross(dir).normalize();

        double roll = level.random.nextDouble() * Math.PI * 2.0;
        Vec3 bowDir = perp1.scale(Math.cos(roll)).add(perp2.scale(Math.sin(roll)));

        double bow = dist * (0.16 + level.random.nextDouble() * 0.08);
        Vec3 control = start.add(end).scale(0.5)
                .add(bowDir.scale(bow))
                .add(dir.scale(dist * 0.06));

        int segments = 70;
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double u = 1.0 - t;
            Vec3 p = start.scale(u * u)
                    .add(control.scale(2.0 * u * t))
                    .add(end.scale(t * t));

            double body = Math.sin(Math.PI * Math.pow(t, 0.85));

            level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 1, 0, 0, 0, 0.0);

            if (body > 0.35 && i % 2 == 0) {
                level.sendParticles(ParticleTypes.SMALL_FLAME, p.x, p.y, p.z, 1, 0, 0, 0, 0.0);
            }
            if (level.random.nextFloat() < 0.15F) {
                level.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 1, 0, 0, 0, 0.0);
            }
            if (level.random.nextFloat() < 0.28F * (1.0 - t)) {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        p.x, p.y, p.z, 1, 0.0, 0.01, 0.0, 0.005);
            }
        }

        level.sendParticles(ParticleTypes.FLAME, end.x, end.y, end.z, 4, 0.12, 0.12, 0.12, 0.02);
        level.sendParticles(ParticleTypes.LAVA, end.x, end.y, end.z, 3, 0.15, 0.15, 0.15, 0.0);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, end.x, end.y, end.z, 2, 0.10, 0.10, 0.10, 0.0);
    }

    public static Vec3 whipStart(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(-Math.cos(Math.toRadians(player.getYRot())),
                    0,
                    Math.sin(Math.toRadians(player.getYRot())));
        }
        right = right.normalize();

        return player.getEyePosition()
                .add(look.scale(0.4))
                .add(right.scale(0.45))
                .add(0, -0.25, 0);
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
