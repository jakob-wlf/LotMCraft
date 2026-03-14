package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class TimeChangeEntity extends Entity {

    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.defineId(TimeChangeEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> RADIUS =
            SynchedEntityData.defineId(TimeChangeEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Float> TIME_MULTIPLIER =
            SynchedEntityData.defineId(TimeChangeEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(TimeChangeEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final Map<UUID, Float> controlledEntities = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> tickAccumulators = new ConcurrentHashMap<>();

    public TimeChangeEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setDuration(20 * 60 * 2);
        setRadius(25);
        setTimeMultiplier(1f);
        this.noPhysics = true;
        this.noCulling = true;

    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if(level().isClientSide)
            return;

        if(getDuration() <= 0)
            setDuration(20 * 60 * 2);
        if(getRadius() <= 0)
            setRadius(25);
    }

    public TimeChangeEntity(EntityType<?> entityType, Level level, int ticks, UUID casterUUID, int radius, float timeMultiplier) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setDuration(ticks);
        this.setCasterUUID(casterUUID);
        this.setRadius(radius);
        this.setTimeMultiplier(timeMultiplier);
    }

    int lifetime = 0;

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        lifetime++;
        if (lifetime >= getDuration()) {
            // Release all entities we were controlling
            releaseAll();
            discard();
            return;
        }

        float multiplier = getTimeMultiplier();
        int radius = getRadius();

        AABB searchBox = new AABB(
                getX() - radius, getY() - radius, getZ() - radius,
                getX() + radius, getY() + radius, getZ() + radius
        );

        Set<UUID> currentInZone = level()
                .getEntitiesOfClass(Entity.class, searchBox,
                        e -> e.distanceTo(this) <= radius
                                && e != this
                                && (!(e instanceof LivingEntity le) || AbilityUtil.mayTarget(getCasterEntity(), le)))
                .stream()
                .map(Entity::getUUID)
                .collect(Collectors.toSet());

        // Release entities that left the radius
        controlledEntities.keySet().stream()
                .filter(uuid -> !currentInZone.contains(uuid))
                .forEach(uuid -> {
                    controlledEntities.remove(uuid);
                    tickAccumulators.remove(uuid);
                });

        // Register/update entities currently in radius
        for (UUID uuid : currentInZone) {
            controlledEntities.put(uuid, multiplier);
            tickAccumulators.putIfAbsent(uuid, 0f);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        releaseAll();
    }

    private void releaseAll() {
        controlledEntities.clear();
        tickAccumulators.clear();
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;

        UUID uuid = event.getEntity().getUUID();
        Float multiplier = controlledEntities.get(uuid);
        if (multiplier == null) return;

        float acc = tickAccumulators.getOrDefault(uuid, 0f) + multiplier;

        if (acc >= 1.0f) {
            // Allow one tick, carry over remainder for Post to consume
            tickAccumulators.put(uuid, acc - 1.0f);
        } else {
            // Not enough credit — skip
            tickAccumulators.put(uuid, acc);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        UUID uuid = event.getEntity().getUUID();
        if (!controlledEntities.containsKey(uuid)) return;

        // Fire extra ticks for all remaining credit above 1.0
        float acc = tickAccumulators.getOrDefault(uuid, 0f);
        while (acc >= 1.0f) {
            event.getEntity().tick();
            acc -= 1.0f;
        }
        tickAccumulators.put(uuid, acc);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, duration);
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    public void setCasterUUID(UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
    }

    public UUID getCasterUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public int getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setRadius(int radius) {
        this.entityData.set(RADIUS, radius);
    }

    public float getTimeMultiplier() {
        return this.entityData.get(TIME_MULTIPLIER);
    }

    public void setTimeMultiplier(float multiplier) {
        this.entityData.set(TIME_MULTIPLIER, multiplier);
    }


    public LivingEntity getCasterEntity() {
        if(level().isClientSide) {
            return null;
        }
        UUID casterUUID = this.getCasterUUID();
        if (casterUUID == null) {
            return null;
        }
        Entity entity = ((ServerLevel) level()).getEntity(casterUUID);
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DURATION, 20 * 60 * 2);
        builder.define(OWNER, Optional.empty());
        builder.define(RADIUS, 25);
        builder.define(TIME_MULTIPLIER, 1f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setDuration(compoundTag.getInt("duration"));
        setRadius(compoundTag.getInt("radius"));
        setTimeMultiplier(compoundTag.getFloat("time_multiplier"));
        if (compoundTag.hasUUID("owner")) {
            setCasterUUID(compoundTag.getUUID("owner"));
        } else {
            setCasterUUID(null);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("duration", getDuration());
        compoundTag.putInt("radius", getRadius());
        compoundTag.putFloat("time_multiplier", getTimeMultiplier());
        if (getCasterUUID() != null) {
            compoundTag.putUUID("owner", getCasterUUID());
        }
    }
}
