package de.jakob.lotm.item.custom;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import de.jakob.lotm.util.mixin.EntityAccessor;
import de.jakob.lotm.util.scheduling.ServerScheduler;
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
import net.minecraft.world.InteractionResult;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarionetteControllerItem extends Item {
    public MarionetteControllerItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                return InteractionResultHolder.pass(stack);
            }

            CompoundTag tag = customData.copyTag();
            String entityUUID = tag.getString("MarionetteUUID");
            if (entityUUID.isEmpty()) {
                return InteractionResultHolder.pass(stack);
            }

            Entity entity = ((ServerLevel) level).getEntity(UUID.fromString(entityUUID));
            if (!(entity instanceof LivingEntity livingEntity)) {
                player.sendSystemMessage(Component.literal("Marionette not found!"));
                stack.consume(1, player);
                return InteractionResultHolder.fail(stack);
            }
            
            MarionetteComponent component = livingEntity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
            if (!component.isMarionette()) {
                stack.consume(1, player);
                return InteractionResultHolder.fail(stack);
            }

            HitResult hitResult = player.pick(20.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // Toggle attack mode
                if(player.isShiftKeyDown()) {
                    component.setShouldAttack(!component.shouldAttack());
                    player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.attack").append(Component.literal(": ")).append(Component.translatable(component.shouldAttack() ? "lotm.on" : "lotm.off")).withColor(0xa26fc9));
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                BlockPos pos = blockHit.getBlockPos().above();
                
                // Position the marionette
                livingEntity.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.entity_teleport").withColor(0xa26fc9));
            } else {
                // Release marionette
                if(player.isShiftKeyDown()) {
                    component.setMarionette(false);
                    component.setControllerUUID("");
                    livingEntity.setHealth(0);
                    stack.consume(1, player);
                    player.sendSystemMessage(Component.translatable("ability.lotm.puppeteering.entity_released").withColor(0xa26fc9));
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }
                // Toggle follow mode
                component.setFollowMode(!component.isFollowMode());
                if(!component.isFollowMode() && livingEntity instanceof Mob mob) {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                }
                player.sendSystemMessage(Component.translatable("ability.lotmcraft.puppeteering.follow_mode").append(Component.literal(": ")).append(Component.translatable(component.isFollowMode() ? "lotm.on" : "lotm.off")).withColor(0xa26fc9));
            }
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void onHold(Player player, ItemStack itemStack) {
        Level level = player.level();
        if(!(player instanceof ServerPlayer) || level.isClientSide)
            return;

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();
        String entityUUID = tag.getString("MarionetteUUID");
        if (entityUUID.isEmpty()) {
            return;
        }

        Entity entity = ((ServerLevel) level).getEntity(UUID.fromString(entityUUID));
        if (!(entity instanceof LivingEntity livingEntity)) {
            player.sendSystemMessage(Component.literal("Marionette not found!"));
            itemStack.consume(1, player);
            return;
        }

        MarionetteComponent component = livingEntity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if (!component.isMarionette()) {
            itemStack.consume(1, player);
            return;
        }

        setGlowingForPlayer(livingEntity, (ServerPlayer) player, true);
        ServerScheduler.scheduleDelayed(10, () -> {
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if(mainHand == itemStack && mainHand.getItem() instanceof MarionetteControllerItem)
                return;
            setGlowingForPlayer(livingEntity, (ServerPlayer) player, false);
        });

    }

    public static void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {
        EntityDataAccessor<Byte> FLAGS = EntityAccessor.getSharedFlagsId();

        // Current flags from the entity
        byte flags = entity.getEntityData().get(FLAGS);

        if (glowing) {
            flags |= 0x40; // glowing bit
        } else {
            flags &= ~0x40; // clear glowing bit
        }

        // Build a list of data values (only the one we care about)
        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(FLAGS, flags));

        // Send metadata update ONLY to that player
        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(entity.getId(), values);
        player.connection.send(packet);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            String entityName = customData.copyTag().getString("MarionetteType");
            tooltip.add(Component.literal("Controls: " + entityName));
            tooltip.add(Component.literal("Right-click on Block: Set Position"));
            tooltip.add(Component.literal("Shift-Right-click on Block: Set Should Attack"));
            tooltip.add(Component.literal("Right-click: Toggle Follow Mode"));
            tooltip.add(Component.literal("Shift-Right-click: Release"));
        }
    }
}