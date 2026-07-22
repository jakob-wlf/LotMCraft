package de.jakob.lotm.item.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.mixin.EntityAccessor;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.*;

public class MarionetteControllerItem extends Item {
    public MarionetteControllerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) return InteractionResultHolder.pass(stack);

            CompoundTag tag = customData.copyTag();
            String entityUUID = tag.getString("MarionetteUUID");
            boolean movementOnly = tag.getBoolean("MovementOnly");

            if (entityUUID.isEmpty()) return InteractionResultHolder.pass(stack);

            Entity entity = ((ServerLevel) level).getEntity(UUID.fromString(entityUUID));
            if (entity == null) entity = searchInOtherLevels(player, entityUUID);

            if (!(entity instanceof LivingEntity livingEntity)) {
                player.sendSystemMessage(Component.literal("Marionette not found!"));
                stack.shrink(1);
                return InteractionResultHolder.fail(stack);
            }

            MarionetteComponent component = livingEntity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if (!component.isMarionette()) {
                stack.shrink(1);
                return InteractionResultHolder.fail(stack);
            }

            HitResult hitResult = player.pick(20.0D, 0.0F, false);

            if (movementOnly) {
                //movement only (can shift once we create something new for manipulation
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hitResult;
                    BlockPos pos = blockHit.getBlockPos().above();

                    boolean isDimensionChange = livingEntity.level() != level;
                    UUID marionetteUUID = livingEntity.getUUID();

                    livingEntity.teleportTo((ServerLevel) level,
                            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                            Set.of(), livingEntity.getYRot(), livingEntity.getXRot());

                    if (isDimensionChange) {
                        ((ServerLevel) level).getServer().tell(new net.minecraft.server.TickTask(
                                ((ServerLevel) level).getServer().getTickCount() + 2, () -> {
                            Entity newEntity = ((ServerLevel) level).getEntity(marionetteUUID);
                            if (newEntity instanceof LivingEntity newMarionette) {
                                newMarionette.hurtMarked = true;
                                ((ServerLevel) level).getChunkSource().addEntity(newMarionette);
                                newMarionette.teleportTo(newMarionette.getX(), newMarionette.getY(), newMarionette.getZ());
                                setAggressiveToNearby(newMarionette, (ServerLevel) level, serverPlayer);
                                if (newMarionette instanceof ServerPlayer controlledPlayer) {
                                    forcePlayerAbilitiesToNearby(controlledPlayer, (ServerLevel) level, serverPlayer);
                                }
                            }
                        }));
                    } else {
                        livingEntity.hurtMarked = true;
                        if (livingEntity.level() instanceof ServerLevel sl) {
                            sl.getChunkSource().removeEntity(livingEntity);
                            ((ServerLevel) level).getChunkSource().addEntity(livingEntity);
                        }
                        setAggressiveToNearby(livingEntity, (ServerLevel) level, serverPlayer);
                        if (livingEntity instanceof ServerPlayer controlledPlayer) {
                            forcePlayerAbilitiesToNearby(controlledPlayer, (ServerLevel) level, serverPlayer);
                        }
                    }

                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_teleport").withColor(0xa26fc9));
                } else {
                    player.sendSystemMessage(
                            Component.literal("Point at a block to move.").withStyle(ChatFormatting.GRAY));
                }

            } else {

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    if (player.isShiftKeyDown()) {
                        component.setShouldAttack(!component.shouldAttack());
                        player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.attack")
                                .append(Component.literal(": "))
                                .append(Component.translatable(component.shouldAttack() ? "lotm.on" : "lotm.off"))
                                .withColor(0xa26fc9));
                        return InteractionResultHolder.sidedSuccess(stack, false);
                    }

                    BlockHitResult blockHit = (BlockHitResult) hitResult;
                    BlockPos pos = blockHit.getBlockPos().above();
                    Level entityLevel = livingEntity.level();

                    if (!(entityLevel instanceof ServerLevel entityServerLevel)) {
                        player.sendSystemMessage(Component.literal("Marionette not in a valid dimension!"));
                        return InteractionResultHolder.fail(stack);
                    }

                    UUID marionetteUUID = livingEntity.getUUID();
                    boolean isDimensionChange = entityLevel != level;

                    livingEntity.teleportTo((ServerLevel) level,
                            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                            Set.of(), livingEntity.getYRot(), livingEntity.getXRot());

                    if (isDimensionChange) {
                        ((ServerLevel) level).getServer().tell(new net.minecraft.server.TickTask(
                                ((ServerLevel) level).getServer().getTickCount() + 2, () -> {
                            Entity newEntity = ((ServerLevel) level).getEntity(marionetteUUID);
                            if (newEntity instanceof LivingEntity newMarionette) {
                                newMarionette.hurtMarked = true;
                                newMarionette.teleportTo(newMarionette.getX(), newMarionette.getY(), newMarionette.getZ());
                            }
                        }));
                    } else {
                        livingEntity.hurtMarked = true;
                    }

                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_teleport").withColor(0xa26fc9));

                } else {
                    if (player.isShiftKeyDown()) {
                        livingEntity.hurt(livingEntity.damageSources().generic(), Float.MAX_VALUE);
                        stack.shrink(1);
                        player.sendSystemMessage(Component.translatable("ability.lotm.puppeteering.entity_released").withColor(0xa26fc9));
                        return InteractionResultHolder.sidedSuccess(stack, false);
                    }
                    component.setFollowMode(!component.isFollowMode());
                    if (!component.isFollowMode() && livingEntity instanceof Mob mob) {
                        mob.setTarget(null);
                        mob.getNavigation().stop();
                    }
                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.follow_mode")
                            .append(Component.literal(": "))
                            .append(Component.translatable(component.isFollowMode() ? "lotm.on" : "lotm.off"))
                            .withColor(0xa26fc9));
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }


    /*
      For mob targets: finds the nearest living entity within 4 blocks and sets it as target.
     */
    private static void setAggressiveToNearby(LivingEntity mob, ServerLevel level, ServerPlayer controller) {
        if (!(mob instanceof Mob m)) return;

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                mob.getBoundingBox().inflate(4.0),
                e -> !e.getUUID().equals(mob.getUUID())
                        && !e.getUUID().equals(controller.getUUID())
                        && e.isAlive());

        if (nearby.isEmpty()) return;

        LivingEntity closest = nearby.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(mob)))
                .orElse(null);
        if (closest == null) return;

        m.setTarget(closest);
    }

    /*
      For player targets: finds the nearest living entity within 4 blocks and forces
      the controlled player to fire random abilities at them repeatedly for 10s.
     */
    private static void forcePlayerAbilitiesToNearby(ServerPlayer controlled, ServerLevel level,
                                                     ServerPlayer controller) {
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                controlled.getBoundingBox().inflate(4.0),
                e -> !e.getUUID().equals(controlled.getUUID())
                        && !e.getUUID().equals(controller.getUUID())
                        && e.isAlive());

        if (nearby.isEmpty()) return;

        LivingEntity closest = nearby.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(controlled)))
                .orElse(null);
        if (closest == null) return;

        String pathway = BeyonderData.getPathway(controlled);
        int sequence = BeyonderData.getSequence(controlled);
        List<de.jakob.lotm.beyonders.abilities.core.Ability> abilities = new ArrayList<>(
                LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence));
        if (abilities.isEmpty()) return;

        LivingEntity target = closest;
        Random rand = new Random();

        ServerScheduler.scheduleForDuration(0, 20 * 3, 20 * 10, () -> {
            if (controlled.isRemoved() || !controlled.isAlive()) return;
            if (target.isRemoved() || !target.isAlive()) return;
            de.jakob.lotm.beyonders.abilities.core.Ability chosen = abilities.get(rand.nextInt(abilities.size()));
            chosen.useAbility(level, controlled);
        }, level);
    }

    private static Entity searchInOtherLevels(Player player, String entityUUID) {
        for (ServerLevel level : player.getServer().getAllLevels()) {
            Entity entity = level.getEntity(UUID.fromString(entityUUID));
            if (entity != null) return entity;
        }
        return null;
    }

    public static void onHold(Player player, ItemStack itemStack) {
        Level level = player.level();
        if (!(player instanceof ServerPlayer) || level.isClientSide) return;

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        CompoundTag tag = customData.copyTag();
        String entityUUID = tag.getString("MarionetteUUID");
        if (entityUUID.isEmpty()) return;

        Entity entity = ((ServerLevel) level).getEntity(UUID.fromString(entityUUID));
        if (entity == null) entity = searchInOtherLevels(player, entityUUID);
        if (!(entity instanceof LivingEntity livingEntity)) {
            player.sendSystemMessage(Component.literal("Marionette not found!"));
            itemStack.shrink(1);
            return;
        }

        MarionetteComponent component = livingEntity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) {
            itemStack.shrink(1);
            return;
        }

        setGlowingForPlayer(livingEntity, (ServerPlayer) player, true);
        ServerScheduler.scheduleDelayed(10, () -> {
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (mainHand == itemStack && mainHand.getItem() instanceof MarionetteControllerItem) return;
            setGlowingForPlayer(livingEntity, (ServerPlayer) player, false);
        });
    }

    public static void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {
        EntityDataAccessor<Byte> FLAGS = EntityAccessor.getSharedFlagsId();
        byte flags = entity.getEntityData().get(FLAGS);
        if (glowing) flags |= 0x40;
        else flags &= ~0x40;

        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(FLAGS, flags));
        player.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), values));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            String entityName = tag.getString("MarionetteType");
            boolean movementOnly = tag.getBoolean("MovementOnly");

            tooltip.add(Component.literal("--------------------------").withColor(0xFFa742f5));
            tooltip.add(Component.literal("Controls: " + entityName).withStyle(ChatFormatting.GRAY));

            if (movementOnly) {
                tooltip.add(Component.literal("Right-click on Block: Move").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("(Movement only — expires in 10s)").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.add(Component.literal("Right-click on Block: Set Position").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Shift-Right-click on Block: Set Should Attack").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Right-click: Toggle Follow Mode").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Shift-Right-click: Release").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
