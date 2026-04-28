package de.jakob.lotm.abilities.door;

import com.zigythebird.playeranimcore.math.Vec3f;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.TimeChangeEntity;
import de.jakob.lotm.events.PlayerEvents;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SealingAuthorityAbility extends SelectableAbility {

    private static SealedLocation currentlySealedLocation = null;
    private static final HashSet<UUID> sealedEntities = new HashSet<>();
    private static final HashSet<BlockPos> sealBlocksToRemove = new HashSet<>();

    private static final HashSet<TrappedEntity> trappedEntities = new HashSet<>();

    public SealingAuthorityAbility(String id) {
        super(id, 45);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("door", 0));
    }

    @Override
    protected float getSpiritualityCost() {
        return 50000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.sealing_authority.seal_target", "ability.lotmcraft.sealing_authority.make_trap", "ability.lotmcraft.sealing_authority.lock_dimension", "ability.lotmcraft.sealing_authority.seal_area"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(selectedAbility == 2) { // Not using switch since I need the client level as well for this one
            lockDimension(level, entity);
            return;
        }

        if(level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;
        switch (selectedAbility) {
            case 0 -> sealTarget(serverLevel, entity);
            case 1 -> makeTrap(serverLevel, entity);
            case 3 -> sealArea(serverLevel, entity);
        }
    }

    private void sealArea(ServerLevel serverLevel, LivingEntity entity) {
        if(currentlySealedLocation != null) {
            currentlySealedLocation.removeSeal();
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, currentlySealedLocation.loc.getPosition().add(0, .5, 0), 7, 550, .05);
            if(currentlySealedLocation.isEntityInside(entity)) {
                currentlySealedLocation = null;
                return;
            }
            currentlySealedLocation = null;
        }

        List<BlockPos> barrierBlocks = AbilityUtil.getBlocksInEllipsoid(serverLevel, entity.position(), 60*(int) Math.max(multiplier(entity)/4,1), 13*(int) Math.max(multiplier(entity)/4,1), false, false, false);

        TimeChangeEntity timeChangeEntity = new TimeChangeEntity(ModEntities.TIME_CHANGE.get(), serverLevel, 20 * 60 *(int) Math.max(multiplier(entity)/4,1), entity.getUUID(), 60*(int) Math.max(multiplier(entity)/4,1), 0.00001f);
        serverLevel.addFreshEntity(timeChangeEntity);
        timeChangeEntity.setPos(entity.position().add(0, 0, 0));

        currentlySealedLocation = new SealedLocation(entity.getUUID(), new Location(entity.position(), serverLevel), 60*(int) Math.max(multiplier(entity)/4,1), 20 * 60 *(int) Math.max(multiplier(entity)/4,1), barrierBlocks, timeChangeEntity);


        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_ACTIVATE, entity.getSoundSource(), 1.5f, 0.6f);
        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BELL_RESONATE, entity.getSoundSource(), 1.5f, 0.6f);

        ParticleOptions particleType = new DustParticleOptions(new Vector3f(131 / 255f, 225 / 255f, 235 / 255f), 2f);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, currentlySealedLocation.loc.getPosition().add(0, .5, 0), 5, 450, .05);

        AtomicInteger radius = new AtomicInteger(1);
        ServerScheduler.scheduleForDuration(0, 1, 80, () -> {
            ParticleUtil.spawnParticles(serverLevel, particleType, currentlySealedLocation.loc().getPosition().add(0, 5, 0), 30, .2, 6, .2, 0);
            ParticleUtil.spawnCircleParticles(serverLevel, ModParticles.STAR.get(), currentlySealedLocation.loc.getPosition(), radius.get(), radius.getAndAdd(1) * 20);
        });
    }

    private void lockDimension(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            ClientHandler.applyCameraShakeToPlayersInRadius(3, 40, (ClientLevel) level, entity.position(), 20*(int) Math.max(multiplier(entity)/4,1));
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        level.playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK,
                entity.getSoundSource(), 1.5f, 0.6f);

        level.playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.BEACON_ACTIVATE,
                entity.getSoundSource(), 2.5f, 1);

        Vec3 effectPos = entity.getEyePosition().subtract(0, .5, 0);

        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, effectPos, 4, 450, .025);
        ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.PORTAL, effectPos, 3, 250, .025);

        for(int i = 3; i < 8; i++) {
            ParticleUtil.spawnCircleParticles(serverLevel, ParticleTypes.ENCHANT, effectPos, i, i * 70);
        }

        ParticleUtil.spawnCircleParticles(serverLevel, new DustParticleOptions(new Vector3f(131 / 255f, 225 / 255f, 235 / 255f), 4f), effectPos, 9, 300);

        SealedDimensionData data = SealedDimensionData.get(serverLevel);
        if(data.isActive() && data.getDimensionLocation().equalsIgnoreCase(level.dimension().location().toString())) {
            data.setTicksRemaining(0);
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.sealing_authority.dimension_unsealed").withColor(BeyonderData.pathwayInfos.get("door").color()));
            return;
        }

        data.activate(20 * 60 * 8+(int) Math.max(multiplier(entity)/4,1), level.dimension().location().toString());
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.sealing_authority.dimension_sealed").withColor(BeyonderData.pathwayInfos.get("door").color()));
    }

    private void makeTrap(ServerLevel level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player)) {
            return;
        }

        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        if(offHandItem.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.sealing_authority.make_trap.no_item").withColor(0x6d32a8));
            return;
        }
        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
        level.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1f, 1f);

        offHandItem.set(ModDataComponents.IS_TRAP, true);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.ENCHANT, entity.getEyePosition().subtract(0, .5, 0), 3, 200);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.PORTAL, entity.getEyePosition().subtract(0, .5, 0), 3, 200);
    }

    private void sealTarget(ServerLevel level, LivingEntity entity) {
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 30*(int) Math.max(multiplier(entity)/4,1), 2);
        if(target == null || sealedEntities.contains(target.getUUID())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("lotmcraft.no_target").withColor(BeyonderData.pathwayInfos.get("door").color()));
            return;
        }
        int radius = Math.max(3, (int) target.getEyeHeight());

        Vec3 targetLoc = target.position();
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 1f, 1f);

        EffectManager.playEffect(EffectManager.Effect.ROTATING_RINGS, targetLoc.x, targetLoc.y, targetLoc.z, level);

        sealedEntities.add(target.getUUID());

        int duration = getSealingDuration(entity, target);

        DisabledAbilitiesComponent comp = target.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        comp.disableAbilityUsageForTime("sealed_sealing_authority", duration, target);

        List<BlockPos> sphereBlocks = AbilityUtil.getBlocksInSphereRadius(level, targetLoc, radius, false, false, false);
        sealBlocksToRemove.clear();
        sealBlocksToRemove.addAll(sphereBlocks);

        final UUID[] taskIdHolder = new UUID[1];
        taskIdHolder[0] = ServerScheduler.scheduleForDuration(0, 4, 20 * 14, () -> {
            Location sealLoc = new Location(targetLoc, level);

            if(InteractionHandler.isInteractionPossible(sealLoc, "explosion", entitySeq) || InteractionHandler.isInteractionPossible(sealLoc, "sealing_malfunction", entitySeq)) {
                ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, targetLoc, 200, 2, .2);
                ParticleUtil.spawnParticles(level, ParticleTypes.PORTAL, targetLoc, 200, 2, .2);

                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                comp.enableAbilityUsage("sealed_sealing_authority");
                if(!(target instanceof Player) && !BeyonderData.isBeyonder(target) && target instanceof Mob mob) {
                    mob.setNoAi(false);
                }

                sphereBlocks.forEach(b -> {
                    BlockState state = level.getBlockState(b);
                    if(!state.is(Blocks.BARRIER)) return;
                    level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
                });

                EffectManager.cancelEffectsNear(targetLoc.x, targetLoc.y, targetLoc.z, 1, level);
                sealBlocksToRemove.clear();

                if(taskIdHolder[0] != null) ServerScheduler.cancel(taskIdHolder[0]);
                return;
            }

            ParticleUtil.spawnCircleParticles(level, ParticleTypes.END_ROD, targetLoc, radius, 40);
            ParticleUtil.spawnCircleParticles(level, ParticleTypes.END_ROD, targetLoc, new Vec3(0, 0, 1), radius, 40);
            ParticleUtil.spawnCircleParticles(level, ParticleTypes.END_ROD, targetLoc, new Vec3(1, 0, 0), radius, 40);

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false, false));
            target.setDeltaMovement(new Vec3(0, 0, 0));
            target.hurtMarked = true;

            sphereBlocks.forEach(b -> {
                BlockState state = level.getBlockState(b);
                if(!state.getCollisionShape(level, b).isEmpty()) return;

                level.setBlockAndUpdate(b, Blocks.BARRIER.defaultBlockState());
            });
        }, () -> {
            if(target instanceof Mob mob) {
                mob.setNoAi(false);
            }
            sealedEntities.remove(target.getUUID());
            sphereBlocks.forEach(b -> {
                BlockState state = level.getBlockState(b);
                if(!state.is(Blocks.BARRIER)) return;
                level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
            });
            EffectManager.cancelEffectsNear(targetLoc.x, targetLoc.y, targetLoc.z, 1, level);
            sealBlocksToRemove.clear();
        }, level, () -> AbilityUtil.getTimeInArea(entity, new Location(targetLoc, level)));
    }

    private int getSealingDuration(LivingEntity entity, LivingEntity target) {
        int sequenceDifference = BeyonderData.getSequence(target) - BeyonderData.getSequence(entity);
        if(sequenceDifference >= 0) {
            return 20 * 18 * sequenceDifference;
        }
        return 20 * 3;
    }

    @SubscribeEvent
    public static void onEntityChangeDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        String dimensionLocation = event.getDimension().location().toString();
        SealedDimensionData data = SealedDimensionData.get(level);
        if(!data.isActive() || (!data.getDimensionLocation().equals(dimensionLocation) && !data.getDimensionLocation().equals(level.dimension().location().toString()))) {
            return;
        }

        event.setCanceled(true);
        ParticleUtil.spawnParticles(level, ModParticles.STAR.get(), event.getEntity().getEyePosition().subtract(0, .5, 0), 200, .8, .05);
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if(event.getLevel() instanceof ServerLevel serverLevel) {
            SealedDimensionData data = SealedDimensionData.get(serverLevel);
            if(data.isActive()) {
                data.setTicksRemaining(0);
            }

            if(currentlySealedLocation != null && currentlySealedLocation.loc.getLevel() == serverLevel) {
                currentlySealedLocation.removeSeal();
                currentlySealedLocation = null;
            }

            sealBlocksToRemove.forEach(b -> {
                BlockState state = serverLevel.getBlockState(b);
                if(!state.is(Blocks.BARRIER)) return;
                serverLevel.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
            });

            trappedEntities.forEach(te -> {
                if(te.entity.level() != serverLevel) return;
                te.entity.teleportTo(te.previousLevel, te.previousLoc.x, te.previousLoc.y, te.previousLoc.z, Set.of(), te.entity.getYRot(), te.entity.getXRot());
            });

        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        trappedEntities.stream().filter(te -> te.entity.getUUID().equals(event.getEntity().getUUID())).findFirst().ifPresent(te -> {
            te.entity.teleportTo(te.previousLevel, te.previousLoc.x, te.previousLoc.y, te.previousLoc.z, Set.of(), te.entity.getYRot(), te.entity.getXRot());
            trappedEntities.remove(te);
        });
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof LivingEntity entity)) return;

        if(entity.level().isClientSide()) return;

        if(BeyonderData.getSequence(entity) <= 0 && BeyonderData.getPathway(entity).equalsIgnoreCase("door")) return;

        List<ItemEntity> nearbyTraps = AbilityUtil.getAllNearbyEntities(null, (ServerLevel) entity.level(), entity.position(), 2.5).stream()
                .filter(e -> e instanceof net.minecraft.world.entity.item.ItemEntity)
                .map(e -> ((net.minecraft.world.entity.item.ItemEntity) e))
                .filter(s -> s.getItem().getOrDefault(ModDataComponents.IS_TRAP, false))
                .toList();

        boolean hasTrap = false;
        for (ItemStack item : entity.getAllSlots()) if (item.getOrDefault(ModDataComponents.IS_TRAP, false)) {
            hasTrap = true;
            break;
        }

        if(!hasTrap && nearbyTraps.isEmpty()) {
            return;
        }

        nearbyTraps.forEach(Entity::discard);
        clearItemsWithComponent(entity, ModDataComponents.IS_TRAP.get());

        ResourceKey<Level> spaceDimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "space"));
        ServerLevel spaceLevel = entity.level().getServer().getLevel(spaceDimension);
        if (spaceLevel == null) {
            return;
        }

        Vec3 previousPos = entity.position();
        ServerLevel previousLevel = (ServerLevel) entity.level();
        spaceLevel.setBlockAndUpdate(spaceLevel.getSharedSpawnPos(), Blocks.END_STONE.defaultBlockState());

        trappedEntities.add(new TrappedEntity(entity, previousLevel, previousPos));

        previousLevel.playSound(null, entity.position().x, entity.position().y, entity.position().z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1f, 1f);
        ParticleUtil.spawnSphereParticles(previousLevel, ParticleTypes.PORTAL, entity.position().add(0, .75, 0), 1.5, 40);
        ParticleUtil.spawnSphereParticles(previousLevel, ParticleTypes.END_ROD, entity.position().add(0, .75, 0), 2, 40);
        ParticleUtil.spawnSphereParticles(previousLevel, ParticleTypes.ENCHANT, entity.position().add(0, .75, 0), 1, 40);
        entity.teleportTo(spaceLevel,
                spaceLevel.getSharedSpawnPos().getX() + 0.5,
                spaceLevel.getSharedSpawnPos().getY() + 0.5,
                spaceLevel.getSharedSpawnPos().getZ() + 0.5,
                Set.of(),
                entity.getYRot(),
                entity.getXRot());

        int duration = getSealingDurationBySequence(BeyonderData.getSequence(entity));
        ServerScheduler.scheduleDelayed(duration, () -> {
            if(entity.isAlive()) {
                entity.teleportTo(previousLevel,
                        previousPos.x,
                        previousPos.y,
                        previousPos.z,
                        Set.of(),
                        entity.getYRot(),
                        entity.getXRot());
            }
            trappedEntities.removeIf(te -> te.entity.getUUID().equals(entity.getUUID()));
        });
    }

    // Method generated by claude because I was too lazy, sorry -_-
    public static void clearItemsWithComponent(LivingEntity entity, DataComponentType<?> componentType) {
        if (entity instanceof Player player) {
            // Full inventory for players
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty() && stack.has(componentType)) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        } else {
            // Equipped slots only for other living entities
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty() && stack.has(componentType)) {
                    entity.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    private static int getSealingDurationBySequence(int sequence) {
        return switch (sequence) {
            case 0 -> 20 * 8;
            case 1 -> 20 * 50;
            case 2 -> 20 * 70;
            default -> 20 * 60 * 5;
        };
    }

    @SubscribeEvent
    public static void onGlobalTick(ServerTickEvent.Post event) {
        if(currentlySealedLocation == null) return;

        ServerLevel level = currentlySealedLocation.loc().getLevel() instanceof ServerLevel serverLevel ? (ServerLevel) serverLevel: null;
        if(level == null) return;

        currentlySealedLocation = new SealedLocation(
                currentlySealedLocation.casterUUId,
                currentlySealedLocation.loc,
                currentlySealedLocation.radius,
                currentlySealedLocation.ticksRemaining - 1,
                currentlySealedLocation.barrierBlocks,
                currentlySealedLocation.timeChangeEntity
        );
        if(currentlySealedLocation.ticksRemaining <= 0) {
            currentlySealedLocation.removeSeal();
            currentlySealedLocation = null;
            return;
        }

        currentlySealedLocation.barrierBlocks.forEach(b -> {
            BlockState state = level.getBlockState(b);
            if(!state.getCollisionShape(level, b).isEmpty()) return;

            level.setBlockAndUpdate(b, Blocks.BARRIER.defaultBlockState());
        });

        AbilityUtil.getNearbyEntities(null, level, currentlySealedLocation.loc.getPosition(), currentlySealedLocation.radius).forEach(e -> {
            FogComponent fogComponent = e.getData(ModAttachments.FOG_COMPONENT);
            fogComponent.setActiveAndSync(true, e);
            fogComponent.setFogColorAndSync(new Vec3f(110 / 255f, 240 / 255f, 255 / 255f), e);
            fogComponent.setFogIndex(-1);

            if(currentlySealedLocation.casterUUId != null && e.getUUID().equals(currentlySealedLocation.casterUUId)) return;

            ParticleUtil.spawnParticles(level, ModParticles.STAR.get(), e.getEyePosition().subtract(0, .5, 0), 8, .3, .9, .3, 0.05);

            if(BeyonderData.getSequence(e) < 1) return;

            DisabledAbilitiesComponent comp = e.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            comp.disableAbilityUsageForTime("sealed_area_sealing_authority", 20, e);
        });
    }

    private record SealedLocation(UUID casterUUId, Location loc, int radius, int ticksRemaining, List<BlockPos> barrierBlocks, TimeChangeEntity timeChangeEntity) {
        public boolean isEntityInside(LivingEntity entity) {
            if(entity.level() != loc.getLevel()) return false;
            if(entity.position().distanceToSqr(loc.getPosition()) > radius * radius) return false;
            return true;
        }

        public void removeSeal() {
            currentlySealedLocation.barrierBlocks.forEach(b -> {
                BlockState state = currentlySealedLocation.loc().getLevel().getBlockState(b);
                if(!state.is(Blocks.BARRIER)) return;
                currentlySealedLocation.loc().getLevel().setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
            });
            timeChangeEntity.discard();
        }
    }

    private record TrappedEntity(LivingEntity entity, ServerLevel previousLevel, Vec3 previousLoc) { }

}
