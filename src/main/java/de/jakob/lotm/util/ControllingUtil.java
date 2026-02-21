package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.OriginalBodyEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncOriginalBodyOwnerPacket;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

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
        CompoundTag targetTag = new CompoundTag();
        target.saveWithoutId(targetTag);

        ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        targetTag.putString("id", targetId.toString());
        data.setTargetEntity(targetTag);

        // save the main body to the player
        CompoundTag bodyTag = new CompoundTag();
        originalBody.saveWithoutId(bodyTag);

            // save location as well
        ListTag posTag = new ListTag();
        posTag.add(DoubleTag.valueOf(player.getX()));
        posTag.add(DoubleTag.valueOf(player.getY()));
        posTag.add(DoubleTag.valueOf(player.getZ()));

        ResourceLocation bodyId = BuiltInRegistries.ENTITY_TYPE.getKey(originalBody.getType());
        bodyTag.putString("id", bodyId.toString());
        bodyTag.put("Pos", posTag);
        data.setBodyEntity(bodyTag);

        // add the main body to the allies
        AllyUtil.addAllyOneWay(player, originalBody.getUUID());

        // change the player shape to the target
        String entityType = ShapeShiftingUtil.getEntityTypeString(target);
        ShapeShiftingUtil.shapeShift(player, entityType);

        // add the original body and remove the target
        level.addFreshEntity(originalBody);
        originalBody.getData(ModAttachments.CONTROLLING_DATA).setOwnerUUID(player.getUUID());
        originalBody.getData(ModAttachments.CONTROLLING_DATA).setOwnerName(player.getName().getString());
        PacketDistributor.sendToPlayersTrackingEntity(originalBody,
                new SyncOriginalBodyOwnerPacket(originalBody.getId(), player.getUUID(), player.getName().getString())
        );
        ControllingDataComponent bodyData = originalBody.getData(ModAttachments.CONTROLLING_DATA);
        System.out.println("body uuid owner : " + bodyData.getOwnerUUID());
        target.discard();
    }

    public static void reset(ServerPlayer player, ServerLevel level, boolean resetData){
        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);
        CompoundTag targetTag = data.getTargetEntity();

        // remove body from allies (to clean it up)
        AllyUtil.removeAllyOneWay(player, data.getBodyUUID());

        // returning the target before returning to main body
        if (targetTag != null) {
            Entity targetEntity = EntityType.loadEntityRecursive(targetTag, level, (entity) -> {
                entity.moveTo(player.position());

                // copy player inventory to the target, otherwise items will be lost
                if (entity instanceof LivingEntity target) {
                    copyEntitiesInventories(player, target);
                }
                return entity;
            });
            if (targetEntity != null) {
                level.addFreshEntity(targetEntity);

                // copy wheel abilities
                AbilityWheelComponent sourceWheelData = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
                AbilityWheelComponent targetWheelData = targetEntity.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
                targetWheelData.setAbilities(sourceWheelData.getAbilities());
                AbilityWheelHelper.syncToClient(player);

                // copy bar abilities
                AbilityBarComponent sourceBarData = player.getData(ModAttachments.ABILITY_BAR_COMPONENT);
                AbilityBarComponent targetBarData = targetEntity.getData(ModAttachments.ABILITY_BAR_COMPONENT);
                targetBarData.setAbilities(sourceBarData.getAbilities());

                // clear data
                if (resetData) {
                    data.removeTargetEntity();
                }
            }
        }

        Entity originalBodyEntity = level.getEntity(data.getBodyUUID());

        // returning to main body
        if (originalBodyEntity != null) {
            if (originalBodyEntity instanceof LivingEntity originalBody) {
                copyEntities(originalBody, player);
                copyPosition(originalBody, player);
            }
            originalBodyEntity.discard();
        } else {
            // only a fallback in case the body didn't exist or was unloaded for some reason (it's not tested if this works or not)
            CompoundTag bodyTag = data.getBodyEntity();
            if (bodyTag != null) {
                Entity bodyEntity = EntityType.loadEntityRecursive(bodyTag, level, (entity) -> {
                    ListTag posList = bodyTag.getList("Pos", 6);
                    if (posList.size() >= 3) {
                        entity.moveTo(posList.getDouble(0),posList.getDouble(1),posList.getDouble(2));
                    }
                    return entity;
                });
                if (bodyEntity != null) {
                    level.addFreshEntity(bodyEntity);
                    if (bodyEntity instanceof LivingEntity originalBody) {
                        copyEntities(originalBody, player);
                        copyPosition(originalBody, player);
                    }
                    bodyEntity.discard();

                }
            }
        }
        // resetting shape
        ShapeShiftingUtil.resetShape(player);

        // clearing data
        data.removeBodyEntity();
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

        ToggleAbility.setActiveAbilities(target, ToggleAbility.getActiveAbilitiesForEntity(source));

        // copy wheel abilities
        AbilityWheelComponent sourceWheelData = source.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        AbilityWheelComponent targetWheelData = target.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        targetWheelData.setAbilities(sourceWheelData.getAbilities());
        if (target instanceof ServerPlayer player) {
            AbilityWheelHelper.syncToClient(player);

        }
        // copy bar abilities
        AbilityBarComponent sourceBarData = source.getData(ModAttachments.ABILITY_BAR_COMPONENT);
        AbilityBarComponent targetBarData = target.getData(ModAttachments.ABILITY_BAR_COMPONENT);
        targetBarData.setAbilities(sourceBarData.getAbilities());

        // copy persistent data for beyonders
        if (source.getPersistentData().getString("beyonder_pathway").isEmpty()) {
            target.getPersistentData().remove("beyonder_pathway");
            PhysicalEnhancementsAbility.resetEnhancements(target.getUUID());
        } else {
            target.getPersistentData().putString("beyonder_pathway", source.getPersistentData().getString("beyonder_pathway"));
        }

        if (source.getPersistentData().getInt("beyonder_sequence") == 0) {
            target.getPersistentData().remove("beyonder_sequence");
            PhysicalEnhancementsAbility.resetEnhancements(target.getUUID());
        } else {
            target.getPersistentData().putInt("beyonder_sequence", source.getPersistentData().getInt("beyonder_sequence"));
        }

        if (source.getPersistentData().getFloat("beyonder_spirituality") == 0.0f) {
            target.getPersistentData().remove("beyonder_spirituality");
            PhysicalEnhancementsAbility.resetEnhancements(target.getUUID());
        } else {
            target.getPersistentData().putFloat("beyonder_spirituality", source.getPersistentData().getFloat("beyonder_spirituality"));
        }

        // sync the changes to the client
        if(target instanceof ServerPlayer serverPlayer) {
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
        else {
            PacketHandler.syncBeyonderDataToEntity(target);
        }

        AttributeMap sourceMap = source.getAttributes();
        AttributeMap targetMap = target.getAttributes();

        AttributeInstance sourceHealth = sourceMap.getInstance(Attributes.MAX_HEALTH);

        if (sourceHealth == null) {
            sourceHealth.setBaseValue(source.getHealth());
        }

        for (AttributeInstance sourceInstance : sourceMap.getSyncableAttributes()) {
            AttributeInstance targetInstance = targetMap.getInstance(sourceInstance.getAttribute());

            // pass attributes that can't be applied to the entity
            if (targetInstance == null) continue;

            // remove old modifiers to not stack
            targetInstance.getModifiers().forEach(mod ->
                    targetInstance.removeModifier(mod.id())
            );

            // copy attribute base values
            targetInstance.setBaseValue(sourceInstance.getBaseValue());

            // copy modifiers
            for (AttributeModifier modifier : sourceInstance.getModifiers()) {
                targetInstance.addTransientModifier(modifier);
            }
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
    public static void onOriginalBodyDeath(LivingIncomingDamageEvent event){
        LivingEntity entity = event.getEntity();
        float damage = event.getAmount();
        if (damage >= entity.getHealth()) {
            if (entity instanceof OriginalBodyEntity originalBody) {
                ControllingDataComponent originalBodyData = originalBody.getData(ModAttachments.CONTROLLING_DATA);
                ServerPlayer player = getPlayerByUUID(originalBodyData.getOwnerUUID());

                if (player == null) return;

                event.setCanceled(true);

                // reset the player
                if (entity.level() instanceof ServerLevel serverLevel) {
                    reset(player,serverLevel, false);
                }

                // clean up data
                ControllingDataComponent playerData = player.getData(ModAttachments.CONTROLLING_DATA);
                playerData.removeTargetEntity();
                playerData.removeOwnerUUID();
                playerData.removeBodyUUID();
                playerData.removeTargetUUID();

                // kill the player for he has sinned
                player.hurt(event.getSource(), Float.MAX_VALUE);
            }
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
                serverLevel.getEntity(data.getTargetUUID()).hurt(event.getSource(), Float.MAX_VALUE);
                data.removeTargetEntity();
                data.removeOwnerUUID();
                data.removeBodyUUID();
                data.removeTargetUUID();
            }
        }
    }

    // drop targets inventory after death
    @SubscribeEvent
    public static void onTargetDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntity();

        CopiedInventoryComponent data = entity.getData(ModAttachments.COPIED_INVENTORY);
        SimpleContainer inv = data.getInv();

        if (inv != null && !inv.isEmpty()) {
            Containers.dropContents(entity.level(), entity.blockPosition(), inv);
        }
    }

    // reset before logout
    @SubscribeEvent
    public static void onPlayerLogout (PlayerEvent.PlayerLoggedOutEvent event){
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);
            if (data.getTargetUUID() != null || data.getBodyUUID() != null) {
                reset(serverPlayer,serverLevel, true);
            }
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
