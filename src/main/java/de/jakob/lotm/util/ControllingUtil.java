package de.jakob.lotm.util;

import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.OriginalBodyComponent;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.OriginalBodyEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncBeyonderDataPacket;
import de.jakob.lotm.network.packets.toClient.SyncLivingEntityBeyonderDataPacket;
import de.jakob.lotm.network.packets.toServer.ShapeShiftingSelectedPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import static de.jakob.lotm.util.helper.AbilityWheelHelper.syncToClient;

public class ControllingUtil {
    public static void possess(ServerPlayer player, LivingEntity target) {
        if (player == null) {
            return ;
        }

        ServerLevel level = (ServerLevel) player.level();

        if (target == null || !target.isAlive()) {
            return ;
        }

        if (target instanceof Player) {
            return ;
        }

        OriginalBodyComponent data = player.getData(ModAttachments.ORIGINAL_BODY);

        if (data.getTargetUUID() != null) return ;

        OriginalBodyEntity originalBody = new OriginalBodyEntity(ModEntities.ORIGINAL_BODY.get(), level);

        data.setBodyUUID(originalBody.getUUID());
        data.setTargetUUID(target.getUUID());

        copyEntities(player, originalBody);
        copyPosition(player, originalBody);

        copyEntities(target, player);
        copyPosition(target, player);

        level.addFreshEntity(originalBody);

        CompoundTag entityTag = new CompoundTag();

        PacketDistributor.sendToServer(new ShapeShiftingSelectedPacket(EntityType.getKey(target.getType()).toString()));

        target.saveWithoutId(entityTag);

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        entityTag.putString("id", id.toString());
        data.setTargetEntity(entityTag);

        target.discard();
    }

    public static void reset(ServerPlayer player, ServerLevel level){
        OriginalBodyComponent data = player.getData(ModAttachments.ORIGINAL_BODY);

        Entity originalBodyEntity = level.getEntity(data.getBodyUUID());
        CompoundTag storedTag = data.getTargetEntity();

        // returning the target before returning to main body
        if (storedTag != null) {
            Entity respawnedEntity = EntityType.loadEntityRecursive(storedTag, level, (entity) -> {
                entity.moveTo(player.position());
                return entity;
            });
            if (respawnedEntity != null) {
                level.addFreshEntity(respawnedEntity);
                // clear data
                data.removeTargetEntity();
            }
        }

        if (originalBodyEntity instanceof LivingEntity originalBody) {
            copyEntities(originalBody, player);
            copyPosition(originalBody, player);
        }
        originalBodyEntity.discard();

        data.removeOwnerUUID();
        data.removeBodyUUID();
        data.removeTargetUUID();
    }

    public static void copyPosition(LivingEntity source, LivingEntity target) {
        // player position
        if (target instanceof ServerPlayer player) {
            player.teleportTo(
                    (net.minecraft.server.level.ServerLevel) source.level(),
                    source.getX(),
                    source.getY(),
                    source.getZ(),
                    source.getYRot(),
                    source.getXRot()
            );
            player.setYHeadRot(source.getYHeadRot());
        }
        // other entities
        else {
            target.moveTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());

            target.setYHeadRot(source.getYHeadRot());
            target.setYBodyRot(source.yBodyRot);
        }
    }

    public static void copyEntities (LivingEntity source, LivingEntity target) {
        copyEntitiesInventories(source, target);

        // copy ability wheel
        AbilityWheelComponent sourceData = source.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        AbilityWheelComponent targetData = target.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        targetData.setAbilities(sourceData.getAbilities());
        if (target instanceof ServerPlayer player) {
            syncToClient(player);
        }

        // copy persistent data for beyonders
        target.getPersistentData().putString("beyonder_pathway", source.getPersistentData().getString("beyonder_pathway"));
        target.getPersistentData().putInt("beyonder_sequence", source.getPersistentData().getInt("beyonder_sequence"));
        target.getPersistentData().putFloat("beyonder_spirituality", source.getPersistentData().getFloat("beyonder_spirituality"));

        // sync the changes to the client, I think this is necessary, however not sure if like this
        if(target instanceof ServerPlayer serverPlayer) {
            SyncBeyonderDataPacket packet = new SyncBeyonderDataPacket(
                    serverPlayer.getPersistentData().getString("beyonder_pathway"),
                    serverPlayer.getPersistentData().getInt("beyonder_sequence"),
                    serverPlayer.getPersistentData().getFloat("beyonder_spirituality"),
                    serverPlayer.getPersistentData().getBoolean("beyonder_griefing_enabled"),
                    serverPlayer.getPersistentData().getFloat("beyonder_digestion_progress")
            );
            PacketHandler.sendToPlayer(serverPlayer, packet);
        }
        else {
            SyncLivingEntityBeyonderDataPacket packet =
                    new SyncLivingEntityBeyonderDataPacket(
                            target.getId(),
                            target.getPersistentData().getString("beyonder_pathway"),
                            target.getPersistentData().getInt("beyonder_sequence"),
                            target.getPersistentData().getFloat("beyonder_spirituality")
                    );
            PacketHandler.sendToAllPlayers(packet);
        }

        // copy attributes--
        AttributeMap sourceAttributes = source.getAttributes();
        // go through EVERY attribute registered on the player
        sourceAttributes.getSyncableAttributes().forEach(sourceInstance -> {
            // pass attributes that can't be applied to the entity
            AttributeInstance targetInstance = target.getAttribute(sourceInstance.getAttribute());
            if (targetInstance != null) {
                targetInstance.setBaseValue(sourceInstance.getBaseValue());

                // clears old modifiers on the clone to prevent stacking duplicates, then add player's modifiers
                var modifierIds = targetInstance.getModifiers().stream()
                        .map(AttributeModifier::id)
                        .toList();

                modifierIds.forEach(targetInstance::removeModifier);
                sourceInstance.getModifiers().forEach(mod -> targetInstance.addTransientModifier(mod));
            }
        });

        // sync attributes to client
        if (target instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(
                    serverPlayer.getId(),
                    sourceAttributes.getSyncableAttributes()
            ));
        }

        // copy effects
        target.removeAllEffects();
        for (MobEffectInstance effect : source.getActiveEffects()) {
            target.addEffect(new MobEffectInstance(effect));
        }
        // copy health after effects
        target.setHealth(source.getHealth());

        // sync fool, health, and saturation as well
        if (target instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetHealthPacket(
                    serverPlayer.getHealth(),
                    serverPlayer.getFoodData().getFoodLevel(),
                    serverPlayer.getFoodData().getSaturationLevel()
            ));
        }
    }

    public static void copyEntitiesInventories(LivingEntity source, LivingEntity target) {
        // dont wanna think too much about this for now, probably there is a better way to do it but will change in the future
        if (source instanceof Player player) {
            SimpleContainer targetInv = target.getData(ModAttachments.COPIED_INVENTORY).getInv();
            targetInv.clearContent();

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                targetInv.setItem(i, player.getInventory().getItem(i).copy());
            }
        }
        else if (target instanceof Player player) {
            SimpleContainer sourceInv = source.getData(ModAttachments.COPIED_INVENTORY).getInv();

            for (int i = 0; i < Math.min(sourceInv.getContainerSize(), player.getInventory().getContainerSize()); i++) {
                player.getInventory().setItem(i, sourceInv.getItem(i).copy());
            }
        }
        else {
            SimpleContainer sourceInv = source.getData(ModAttachments.COPIED_INVENTORY).getInv();
            SimpleContainer targetInv = target.getData(ModAttachments.COPIED_INVENTORY).getInv();

            targetInv.clearContent();
            for (int i = 0; i < sourceInv.getContainerSize(); i++) {
                targetInv.setItem(i, sourceInv.getItem(i).copy());
            }
        }
        // make the entity equip armor - doesnt work for now
        target.setItemSlot(EquipmentSlot.HEAD, source.getItemBySlot(EquipmentSlot.HEAD).copy());
        target.setItemSlot(EquipmentSlot.CHEST, source.getItemBySlot(EquipmentSlot.CHEST).copy());
        target.setItemSlot(EquipmentSlot.LEGS, source.getItemBySlot(EquipmentSlot.LEGS).copy());
        target.setItemSlot(EquipmentSlot.FEET, source.getItemBySlot(EquipmentSlot.FEET).copy());

        // hand slots as well
        target.setItemSlot(EquipmentSlot.MAINHAND, source.getMainHandItem().copy());
        target.setItemSlot(EquipmentSlot.OFFHAND, source.getOffhandItem().copy());
    }
}
