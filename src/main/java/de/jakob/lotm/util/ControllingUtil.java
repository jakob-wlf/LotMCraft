package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.OriginalBodyEntity;
import de.jakob.lotm.network.packets.toClient.SyncOriginalBodyOwnerPacket;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import de.jakob.lotm.util.playerMap.PlayerMap;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import de.jakob.lotm.util.shapeShifting.ShapeShiftingUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collector;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ControllingUtil {

    public static void possess(ServerPlayer player, LivingEntity target, boolean spawnOriginalBody) {
        // checks
        if (player == null) return;
        ServerLevel level = (ServerLevel) player.level();

        if (target == null || !target.isAlive()) return;

        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA.get());

        if (data.isControlling()) return ;

        boolean isTargetPlayer = target instanceof ServerPlayer;

        // making the original body and setting his owner
        OriginalBodyEntity originalBody = new OriginalBodyEntity(ModEntities.ORIGINAL_BODY.get(), level);

        // setting body data to the player
        data.setBodyUUID(originalBody.getUUID());

        data.setTargetUUID(target.getUUID());
        data.setControlling(true, player);

        //copy the player and his position to original body
        copyEntities(player, originalBody);
        copyPosition(player, originalBody);

        // copy the target and his position to the player
        copyEntities(target, player);
        copyPosition(target, player);

        // save the target to the player
        if (!isTargetPlayer){
            CompoundTag targetTag = new CompoundTag();
            target.saveWithoutId(targetTag);

            ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
            targetTag.putString("id", targetId.toString());
            data.setTargetEntity(targetTag);
        }

        // save the main body to the player
        CompoundTag bodyTag = new CompoundTag();
        originalBody.saveWithoutId(bodyTag);

        ResourceLocation bodyId = BuiltInRegistries.ENTITY_TYPE.getKey(originalBody.getType());
        bodyTag.putString("id", bodyId.toString());
        if (!spawnOriginalBody) {
            // if the main body didn't spawn, pass an empty tag to return the player to his current location when resetting
            bodyTag.put("Pos", new ListTag());
        }
        data.setBodyEntity(bodyTag, player);

        if (spawnOriginalBody) {
            // add the main body to the allies
            AllyUtil.addAllyOneWay(player, originalBody.getUUID());

            // add the original body
            level.addFreshEntity(originalBody);
            originalBody.getData(ModAttachments.CONTROLLING_DATA).setOwnerUUID(player.getUUID());
            originalBody.getData(ModAttachments.CONTROLLING_DATA).setOwnerName(player.getName().getString());
            PacketDistributor.sendToPlayersTrackingEntity(originalBody,
                    new SyncOriginalBodyOwnerPacket(originalBody.getId(), player.getUUID(), player.getName().getString())
            );
        }

        // change the player shape to the target
        ShapeShiftingUtil.shapeShift(player, target, false);

        // remove the target if he is not a player
        if (!(target instanceof ServerPlayer serverTarget)){
            target.discard();
        } else {
            ControllingDataComponent targetData = target.getData(ModAttachments.CONTROLLING_DATA);
            targetData.setOwnerUUID(player.getUUID());
            targetData.setIsControlled(true);
            serverTarget.setGameMode(GameType.SPECTATOR);
            serverTarget.setCamera(player);
        }
    }

    public static void reset(ServerPlayer player, ServerLevel level, boolean resetData){
        if (player == null) return;
        ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);
        CompoundTag targetTag = data.getTargetEntity();

        // remove body from allies (to clean it up)
        AllyUtil.removeAllyOneWay(player, data.getBodyUUID());

        // Track the restored target so we can update its controller item after the body is restored
        LivingEntity restoredTarget = null;

        // returning the target before returning to main body
        if (targetTag != null) {
            // Patch the saved tag with the player's current Beyonder state so sequence regressions persist
            String currentPathway = BeyonderData.getPathway(player);
            int currentSequence = BeyonderData.getSequence(player);
            if (!currentPathway.isEmpty() && currentSequence >= 0) {
                // idk why that is here, "Will" added it and idk why so i'll leave it here anyways
                targetTag.putString("Pathway", currentPathway);
                targetTag.putInt("Sequence", currentSequence);

                // also patch NeoForgeData persistent data inside the tag
                if (targetTag.contains("neoforge:attachments")) {
                    CompoundTag nfd = targetTag.getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");
                    nfd.putString("pathway", currentPathway);
                    nfd.putInt("sequence", currentSequence);
                    nfd.putFloat("digestionProgress", BeyonderData.getDigestionProgress(player));
                    
                    ListTag characteristicListTag = new ListTag();
                    for(Characteristic characteristic : BeyonderData.getCharList(player)){
                        characteristicListTag.add(characteristic.toNBT(level.registryAccess()));
                    }
                    nfd.put("characteristicList", characteristicListTag);
                    ArrayList<Characteristic> charList = BeyonderData.getCharList(player);
                    int charSeq = charList.stream().mapToInt(c -> c.sequence()).max().orElse(-1);
                    String charPath = charList.stream().filter(c -> c.sequence() == charSeq).findFirst().map(c -> c.pathway()).orElse("");
                    LOTMCraft.LOGGER.info("reset: patching saved target tag with player Beyonder state: player={} uuid={} currentPathway={} currentSequence={} charCount={}",
                            player.getDisplayName().getString(), player.getUUID(), currentPathway, currentSequence, charList.size());
                    // Normalize values: if no characteristics found, treat as non-beyonder to avoid negative sequence indices
                    int normalizedSeq = charSeq < 0 ? de.jakob.lotm.LOTMCraft.NON_BEYONDER_SEQ : charSeq;
                    String normalizedPath = (charPath == null || charPath.isEmpty()) ? "none" : charPath;
                    // Avoid mutating the global PlayerMap during this patch operation; putIntoMap=false
                    BeyonderData.setBeyonder(player, normalizedPath, normalizedSeq, false, false, false, false, false, false);
                    LOTMCraft.LOGGER.info("Patched Beyonder data for player {}: pathway={}, sequence={}, digestionProgress={}, characteristicListSize={}",
                            player.getDisplayName().getString(), normalizedPath, normalizedSeq, BeyonderData.getDigestionProgress(player), characteristicListTag.size());
                }
            }
        }

        Entity targetEntity = null;
        if (targetTag != null) {
            targetEntity = EntityType.loadEntityRecursive(targetTag, level, (entity) -> {
                entity.setPos(player.getX(), player.getY(), player.getZ());
                return entity;
            });
        } else if (data.getTargetUUID() != null) {
            targetEntity = getPlayerByUUID(data.getTargetUUID());
        }

        // copying some (important) data from the player to the target
        if (targetEntity != null) {

            // copy player inventory to the target
            if (targetEntity instanceof LivingEntity target) {
                copyInventories(player, target);
                //copyEntities(player, target);
            }

            level.addFreshEntity(targetEntity);

            // Carry over digestion progress (stored in persistent data, not NPC fields)
            if (targetEntity instanceof LivingEntity targetLiving) {
                BeyonderData.setDigestionProgress(targetLiving, BeyonderData.getDigestionProgress(player));
                restoredTarget = targetLiving;
            }

            // copy wheel abilities
            AbilityWheelComponent sourceWheelData = player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            AbilityWheelComponent targetWheelData = targetEntity.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            targetWheelData.setAbilities(new ArrayList<>(sourceWheelData.getAbilities()));
            AbilityWheelHelper.syncToClient(player);

            // copy bar abilities
            AbilityBarComponent sourceBarData = player.getData(ModAttachments.ABILITY_BAR_COMPONENT);
            AbilityBarComponent targetBarData = targetEntity.getData(ModAttachments.ABILITY_BAR_COMPONENT);
            targetBarData.setAbilities(new ArrayList<>(sourceBarData.getAbilities()));

            // preserve the targets health
            if (targetEntity instanceof LivingEntity target) {
                target.setHealth(player.getHealth());
            }

            if (targetEntity instanceof ServerPlayer serverTarget) {
                ControllingDataComponent targetData = serverTarget.getData(ModAttachments.CONTROLLING_DATA);
                targetData.setIsControlled(false);
                targetData.setOwnerUUID(null);
                serverTarget.setGameMode(GameType.SURVIVAL);
                serverTarget.setCamera(serverTarget);
            }

            // clear data
            if (resetData) {
                data.setTargetEntity(null);
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
            // only a fallback in case the body didn't exist or was unloaded for some reason
            CompoundTag bodyTag = data.getBodyEntity();
            if (bodyTag != null) {
                Entity bodyEntity = EntityType.loadEntityRecursive(bodyTag, level, (entity) -> {
                    ListTag posList = bodyTag.getList("Pos", 6);
                    if (posList.size() >= 3) {
                        entity.setPos(posList.getDouble(0),posList.getDouble(1),posList.getDouble(2));
                    } else {
                        entity.setPos(player.position());
                    }
                    return entity;
                });
                if (bodyEntity != null) {
                    if (bodyEntity instanceof LivingEntity originalBody) {
                        copyEntities(originalBody, player);
                        copyPosition(originalBody, player);

                        PhysicalEnhancementsAbility.resetEnhancements(player);

                        float health = bodyTag.getFloat("Health");
                        ServerScheduler.scheduleDelayed(5, () -> {
                            player.setHealth(health);
                        }, level);
                    }
                }
            }
        }
        // Update the marionette controller item lore to reflect any sequence changes (must be after body restore)
        if (restoredTarget != null) {
            String targetUUIDStr = restoredTarget.getStringUUID();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if (customData == null) continue;
                if (targetUUIDStr.equals(customData.copyTag().getString("MarionetteUUID"))) {
                    player.getInventory().setItem(i, MarionetteUtils.createMarionetteController(restoredTarget));
                    break;
                }
            }
        }

        // resetting shape
        ShapeShiftingUtil.resetShape(player);

        // clearing data
        data.setBodyEntity(null, player);
        data.setControlling(false, player);
        if (resetData) {
            data.setOwnerUUID(null);
            data.setBodyUUID(null);
            data.setTargetUUID(null);
        }
    }

    private static void copyPosition(LivingEntity source, LivingEntity target) {
        target.teleportTo(
                (net.minecraft.server.level.ServerLevel) source.level(),
                source.getX(),
                source.getY(),
                source.getZ(),
                Set.of(),
                source.getYRot(),
                source.getXRot()
        );

        target.setYHeadRot(source.getYHeadRot());
        target.setYBodyRot(source.yBodyRot);
    }

    private static void copyData(LivingEntity source, LivingEntity target) {
        // copy togglable abilities
        ToggleAbility.setActiveAbilities(target, new HashSet<>(ToggleAbility.getActiveAbilitiesForEntity(source)));

        // copy wheel abilities
        AbilityWheelComponent sourceWheelData = source.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        AbilityWheelComponent targetWheelData = target.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        targetWheelData.setAbilities(new ArrayList<>(sourceWheelData.getAbilities()));
        if (target instanceof ServerPlayer player) {
            AbilityWheelHelper.syncToClient(player);
        }

        // copy bar abilities
        AbilityBarComponent sourceBarData = source.getData(ModAttachments.ABILITY_BAR_COMPONENT);
        AbilityBarComponent targetBarData = target.getData(ModAttachments.ABILITY_BAR_COMPONENT);
        targetBarData.setAbilities(new ArrayList<>(sourceBarData.getAbilities()));

        if (BeyonderData.isBeyonder(source) || BeyonderData.isBeyonder(target)) {
            LOTMCraft.LOGGER.info("copyData: copying Beyonder from {} ({}) to {} ({})", source.getDisplayName().getString(), source.getUUID(), target.getDisplayName().getString(), target.getUUID());

            // Set the pathway/sequence/etc using the standard setter to keep PlayerMap and passive effects consistent
            // Avoid mutating PlayerMap during transient copies: set putIntoMap=false so the global map is only updated when players are restored
            BeyonderData.setBeyonder(target, BeyonderData.getPathway(source), BeyonderData.getSequence(source), false, false, false, false, false, false);

            // Build a defensive deep-copy of the characteristic list and set it in one operation to avoid
            // repeated setCharacteristic calls which can cause intermediate PlayerMap or packet sync side-effects.
            ArrayList<Characteristic> charCopy = new ArrayList<>();
            for (Characteristic characteristic : BeyonderData.getCharList(source)) {
                charCopy.add(new Characteristic(characteristic.pathway(), characteristic.stack(), characteristic.sequence()));
            }
            target.getData(ModAttachments.BEYONDER_COMPONENT).setCharacteristicList(charCopy);

            // Sync digestion/griefing for players
            if (source instanceof Player sourcePlayer && target instanceof Player targetPlayer) {
                BeyonderData.digest(targetPlayer, BeyonderData.getDigestionProgress(sourcePlayer), false);
                BeyonderData.setGriefingEnabled(targetPlayer, BeyonderData.isGriefingEnabled(sourcePlayer));
            }
        } else {
            BeyonderData.clearBeyonderData(target);
        }
    }

    private static void copyEntities (LivingEntity source, LivingEntity target) {
        copyInventories(source, target);

        copyData(source, target);

        AttributeMap sourceMap = source.getAttributes();
        AttributeMap targetMap = target.getAttributes();

        boolean healthSynced = false;
        boolean armorSynced = false;
        boolean armorToughnessSynced = false;
        boolean attackDamageSynced = false;

        for (AttributeInstance sourceInstance : sourceMap.getSyncableAttributes()) {
            AttributeInstance targetInstance = targetMap.getInstance(sourceInstance.getAttribute());

            // for movement speed "correct" the value
            if (sourceInstance.getAttribute().equals(Attributes.MOVEMENT_SPEED)) {
                AttributeInstance sourceSpeed = source.getAttribute(Attributes.MOVEMENT_SPEED);
                AttributeInstance targetSpeed = target.getAttribute(Attributes.MOVEMENT_SPEED);

                var sourceDefaultAttributes = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) source.getType());
                var targetDefaultAttributes = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) target.getType());

                double sourceDefault = sourceDefaultAttributes.getBaseValue(Attributes.MOVEMENT_SPEED);
                double targetDefault = targetDefaultAttributes.getBaseValue(Attributes.MOVEMENT_SPEED);

                double ratio = sourceSpeed.getBaseValue() / sourceDefault;

                targetSpeed.getModifiers().forEach(mod -> {
                    targetSpeed.removeModifier(mod.id());
                });

                targetSpeed.setBaseValue(targetDefault * ratio);

                for (AttributeModifier modifier : sourceSpeed.getModifiers()) {
                    targetSpeed.addTransientModifier(modifier);
                }
                continue;
            }

            // pass attributes that can't be applied to the entity
            if (targetInstance == null) continue;

            // mark core attributes as synced
            if (targetInstance.getAttribute().equals(Attributes.MAX_HEALTH)) healthSynced = true;
            if (targetInstance.getAttribute().equals(Attributes.ARMOR)) armorSynced = true;
            if (targetInstance.getAttribute().equals(Attributes.ARMOR_TOUGHNESS)) armorToughnessSynced = true;
            if (targetInstance.getAttribute().equals(Attributes.ATTACK_DAMAGE)) attackDamageSynced = true;

            // remove old modifiers to not stack
            targetInstance.getModifiers().forEach(mod -> targetInstance.removeModifier(mod.id()));

            // copy attribute base values
            targetInstance.setBaseValue(sourceInstance.getBaseValue());

            // copy modifiers
            for (AttributeModifier modifier : sourceInstance.getModifiers()) {
                targetInstance.addTransientModifier(modifier);
            }

            // sync max health
            if (targetInstance.getAttribute().equals(Attributes.MAX_HEALTH)) {}
        }

        // copy effects
        target.removeAllEffects();
        for (MobEffectInstance effect : source.getActiveEffects()) {
            target.addEffect(new MobEffectInstance(effect));
        }

        // force health
        if (!healthSynced) {
            syncDefaultAttribute(source, target, Attributes.MAX_HEALTH);
        }

        // force armor
        if (!armorSynced) {
            syncDefaultAttribute(source, target, Attributes.ARMOR);
        }

        // force armor toughness
        if (!armorToughnessSynced) {
            syncDefaultAttribute(source, target, Attributes.ARMOR_TOUGHNESS);
        }

        // force attack damage
        if (!attackDamageSynced) {
            syncDefaultAttribute(source, target, Attributes.ATTACK_DAMAGE);
        }

        // set current health
        target.setHealth(source.getHealth());

        // copy water bubbles
        target.setAirSupply(source.getAirSupply());

        // copy burning state
        target.setRemainingFireTicks(source.getRemainingFireTicks());
    }

    private static void syncDefaultAttribute(LivingEntity source, LivingEntity target, Holder<Attribute> attribute) {
        AttributeInstance sourceInst = source.getAttribute(attribute);
        AttributeInstance targetInst = target.getAttribute(attribute);
        if (targetInst != null) {
            targetInst.getModifiers().forEach(mod -> targetInst.removeModifier(mod.id()));
            if (sourceInst != null) {
                targetInst.setBaseValue(sourceInst.getBaseValue());
            } else {
                // if source doesn't have an attribute, remove all modifiers for it
                targetInst.getModifiers().forEach(mod -> targetInst.removeModifier(mod.id()));
            }
        }
    }

    private static void copyInventories(LivingEntity source, LivingEntity target) {
        if (source instanceof Player sourcePlayer && target instanceof Player targetPlayer) {
            Inventory sourceInv = sourcePlayer.getInventory();
            Inventory targetInv = targetPlayer.getInventory();
            for (int i = 0; i < Math.min(sourceInv.getContainerSize(), targetInv.getContainerSize()); i++) {
                targetInv.setItem(i, sourceInv.getItem(i).copy());
            }
        }
        else if (source instanceof Player player) {
            SimpleContainer targetInv = target.getData(ModAttachments.COPIED_INVENTORY).getInv();
            targetInv.clearContent();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                targetInv.setItem(i, player.getInventory().getItem(i).copy());
            }
            target.setItemSlot(EquipmentSlot.MAINHAND, source.getMainHandItem().copy());
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
            target.setItemSlot(EquipmentSlot.MAINHAND, source.getMainHandItem().copy());
        }

        target.setItemSlot(EquipmentSlot.OFFHAND, source.getOffhandItem().copy());
        target.setItemSlot(EquipmentSlot.HEAD, source.getItemBySlot(EquipmentSlot.HEAD).copy());
        target.setItemSlot(EquipmentSlot.CHEST, source.getItemBySlot(EquipmentSlot.CHEST).copy());
        target.setItemSlot(EquipmentSlot.LEGS, source.getItemBySlot(EquipmentSlot.LEGS).copy());
        target.setItemSlot(EquipmentSlot.FEET, source.getItemBySlot(EquipmentSlot.FEET).copy());
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
                ControllingDataComponent playerData = player.getData(ModAttachments.CONTROLLING_DATA);

                // if the player is not controlling anything => discard the main body so it doesn't drop anything
                if (!playerData.isControlling()) {
                    originalBody.discard();
                    return;
                }

                event.setCanceled(true);

                // reset the player
                if (entity.level() instanceof ServerLevel serverLevel) {
                    reset(player,serverLevel, false);
                }

                // clean up data
                playerData.setControlling(false, player);
                playerData.setTargetEntity(null);
                playerData.setOwnerUUID(null);
                playerData.setBodyUUID(null);
                playerData.setTargetUUID(null);

                // kill the player for he has sinned
                player.hurt(event.getSource(), Float.MAX_VALUE);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingIncomingDamageEvent event){
        LivingEntity entity = event.getEntity();
        float damage = event.getAmount();
        if (damage >= entity.getHealth()) {
            if (entity instanceof ServerPlayer serverPlayer) {
                ControllingDataComponent data = serverPlayer.getData(ModAttachments.CONTROLLING_DATA);
                if (!data.isControlling()) return;
                event.setCanceled(true);

                // reset the player
                if (entity.level() instanceof ServerLevel serverLevel) {
                    reset(serverPlayer, serverLevel, false);
                    // kill the target entity instead
                    serverLevel.getEntity(data.getTargetUUID()).hurt(event.getSource(), Float.MAX_VALUE);
                    data.setControlling(false, serverPlayer);
                    data.setTargetEntity(null);
                    data.setOwnerUUID(null);
                    data.setBodyUUID(null);
                    data.setTargetUUID(null);
                }
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
            if (data.isControlling() || data.getBodyUUID() != null) {
                reset(serverPlayer,serverLevel, true);
            }
            if (data.isControlled() && data.getOwnerUUID() != null) {
                reset(getPlayerByUUID(data.getOwnerUUID()), serverLevel, true);
            }
        }
    }

    // reset before logout
    @SubscribeEvent
    public static void onPlayerChangedDimension (EntityTravelToDimensionEvent event){
        if (event.getEntity().level() instanceof ServerLevel serverLevel && event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.serverLevel().dimension().equals(event.getDimension())) {
                ControllingDataComponent data = serverPlayer.getData(ModAttachments.CONTROLLING_DATA);
                if (data.isControlling() || data.getBodyUUID() != null) {
                    event.setCanceled(true);
                    reset(serverPlayer,serverLevel, true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBodyTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof OriginalBodyEntity body) {
            ControllingDataComponent data = body.getData(ModAttachments.CONTROLLING_DATA);
            if (data.getOwnerUUID() != null && data.getOwnerName() != null) {
                PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(),
                        new SyncOriginalBodyOwnerPacket(body.getId(), data.getOwnerUUID(), data.getOwnerName())
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTargetTick(PlayerTickEvent.Post event) {
        Player target = event.getEntity();

        if (!(target instanceof ServerPlayer serverTarget)) return;

        ControllingDataComponent targetData = serverTarget.getData(ModAttachments.CONTROLLING_DATA);
        if (targetData.getOwnerUUID() == null) return;

        Player owner = getPlayerByUUID(targetData.getOwnerUUID());
        if (targetData.isControlled()) {
            if (owner != null) {
                serverTarget.setGameMode(GameType.SPECTATOR);
                serverTarget.setCamera(owner);
            } else {
                serverTarget.setGameMode(GameType.SURVIVAL);
                serverTarget.setCamera(serverTarget);
            }
        }
    }

    // track the distance between the main body and the player
    @SubscribeEvent
    public static void onPlayerTickDistanceFromBody (PlayerTickEvent.Post event){
        Player player = event.getEntity();

        if (player.level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {

            // run every 15s
            if (player.tickCount % 300 != 0) return;

            ControllingDataComponent data = player.getData(ModAttachments.CONTROLLING_DATA);
            if (data.isControlling() || data.getBodyUUID() != null) {
                Entity mainBodyEntity = serverLevel.getEntity(data.getBodyUUID());
                // dont reset if main body doesn't exist
                if (mainBodyEntity == null) return;

                CompoundTag bodyData = data.getBodyEntity().getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");

                // get the seq of main body and not the current player
                int sequence = bodyData.getInt("sequence");
                if (sequence == 0) return;

                int controllingDistance;
                switch (sequence) {
                    case 5 -> controllingDistance = 500;
                    case 4 -> controllingDistance = 1250;
                    case 3 -> controllingDistance = 2000;
                    case 2 -> controllingDistance = 5000;
                    case 1 -> controllingDistance = 15000;
                    default -> controllingDistance = 250;
                }

                // calculate the distance between main body and player
                double dx = serverPlayer.getX() - mainBodyEntity.getX();
                double dy = serverPlayer.getY() - mainBodyEntity.getY();
                double dz = serverPlayer.getZ() - mainBodyEntity.getZ();

                if (controllingDistance < Math.sqrt(dx * dx + dy * dy + dz * dz)) {
                    reset(serverPlayer,serverLevel, true);
                }
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
