package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ControllingDataComponent;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

import static de.jakob.lotm.util.helper.AbilityWheelHelper.syncToClient;
import static de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil.resetShape;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ControllingUtil {

    public static void possess(ServerPlayer player, LivingEntity target) {
        // checks
        if (player == null) return;
        ServerLevel level = (ServerLevel) player.level();

        if (target == null || !target.isAlive() || target instanceof Player) return;

        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);

        if (data.getTargetUUID() != null) return ;

        // making the original body and setting his owner
        OriginalBodyEntity originalBody = new OriginalBodyEntity(ModEntities.ORIGINAL_BODY.get(), level);
        ControllingDataComponent originalBodyData = originalBody.getData(ModAttachments.CONTROLLING_DATA);
        originalBodyData.setOwnerUUID(player.getUUID());

        // setting body data to the player
        data.setBodyUUID(originalBody.getUUID());
        data.setTargetUUID(target.getUUID());

        //copy the player and his position to original body
        copyEntities(player, originalBody);
        copyPosition(player, originalBody);

        // copy the target and his position to the player
        copyEntities(target, player);
        copyPosition(target, player);

        // save the target to the player
        CompoundTag entityTag = new CompoundTag();
        target.saveWithoutId(entityTag);

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        entityTag.putString("id", id.toString());
        data.setTargetEntity(entityTag);

        // change the player shape to the target
        PacketDistributor.sendToServer(new ShapeShiftingSelectedPacket(EntityType.getKey(target.getType()).toString()));

        // add the original body and remove the target
        level.addFreshEntity(originalBody);
        target.discard();
    }

    public static void reset(ServerPlayer player, ServerLevel level, boolean resetData){
        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);

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
                if (resetData) {
                    data.removeTargetEntity();
                }
            }
        }

        // returning to main body
        if (originalBodyEntity instanceof LivingEntity originalBody) {
            copyEntities(originalBody, player);
            copyPosition(originalBody, player);
        }
        originalBodyEntity.discard();

        // resetting shape
        resetShape(player);

        // clearing data
        if (resetData) {
            data.removeOwnerUUID();
            data.removeBodyUUID();
            data.removeTargetUUID();
        }
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

        // copy attributes - something is not working here i think its modifiers
        AttributeMap sourceAttributes = source.getAttributes();
        // go through every attribute registered on the player
        sourceAttributes.getSyncableAttributes().forEach(sourceInstance -> {
            // pass attributes that can't be applied to the entity
            AttributeInstance targetInstance = target.getAttribute(sourceInstance.getAttribute());
            if (targetInstance != null) {
                targetInstance.setBaseValue(sourceInstance.getBaseValue());

                // clears old modifiers on the clone to prevent stacking duplicates, then add player's modifiers
                // i think the clear is not needed, need to test
                var modifierIds = targetInstance.getModifiers().stream()
                        .map(AttributeModifier::id)
                        .toList();

                modifierIds.forEach(targetInstance::removeModifier);
                sourceInstance.getModifiers().forEach(mod -> targetInstance.addTransientModifier(mod));
            }
        });

        // sync attributes to client - probably this is wrong, its not syncing
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

        // sync food, health, and saturation as well
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

    @SubscribeEvent
    public static void onOriginalBodyDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntity();

        if (entity instanceof OriginalBodyEntity originalBody) {
            ControllingDataComponent originalBodyData = originalBody.getData(ModAttachments.CONTROLLING_DATA);
            ServerPlayer player = getPlayerByUUID(originalBodyData.getOwnerUUID());

            if (player == null) return;

            event.setCanceled(true);

            // reset the player
            if (entity.level() instanceof ServerLevel serverLevel) {
                reset(player,serverLevel, false);
            }
            // kill the player for he has sinned
            player.hurt(event.getSource(), Float.MAX_VALUE + 10);
            ControllingDataComponent playerData = player.getData(ModAttachments.CONTROLLING_DATA);
            playerData.removeTargetEntity();
            playerData.removeOwnerUUID();
            playerData.removeBodyUUID();
            playerData.removeTargetUUID();
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntity();

        if (entity instanceof ServerPlayer serverPlayer) {
            ControllingDataComponent data = serverPlayer.getData(ModAttachments.CONTROLLING_DATA);
            if (data.getTargetUUID() == null) return ;

            event.setCanceled(true);

            // reset the player
            if (entity.level() instanceof ServerLevel serverLevel) {
                reset(serverPlayer,serverLevel, false);
                // kill the target entity instead
                serverLevel.getEntity(data.getTargetUUID()).hurt(event.getSource(), Float.MAX_VALUE + 10);
                data.removeTargetEntity();
                data.removeOwnerUUID();
                data.removeBodyUUID();
                data.removeTargetUUID();
            }
        }
    }

    // reset before logout
    @SubscribeEvent
    public static void onPlayerLogout (PlayerEvent.PlayerLoggedOutEvent event){
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            reset(serverPlayer,serverLevel, true);
        }
    }

    public static ServerPlayer getPlayerByUUID(UUID uuid) {
        var server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            return server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }
}
