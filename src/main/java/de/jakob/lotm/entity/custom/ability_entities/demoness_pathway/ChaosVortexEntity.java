package de.jakob.lotm.entity.custom.ability_entities.demoness_pathway;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.events.custom.TargetLocationEvent;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ChaosVortexEntity extends Entity {

    private int strength = 5;
    private int lifetime = 20 * 5;
    private LivingEntity source = null;
    private double damage = 20;

    private Vec3 direction = null;

    private static final HashMap<LivingEntity, ChaosVortexEntity> activeVortices = new HashMap<>();

    public ChaosVortexEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ChaosVortexEntity(Level level, Vec3 pos, int strength, int lifetime) {
        super(ModEntities.CHAOS_VORTEX.get(), level);
        this.strength = strength;
        this.lifetime = lifetime;
        this.setPos(pos);
    }

    public ChaosVortexEntity(Level level, Vec3 pos, int strength, int lifetime, Vec3 direction, LivingEntity source, double damage) {
        this(level, pos, strength, lifetime);
        this.direction = direction;
        this.source = source;
        this.damage = damage;
        setPos(pos);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if(!(level() instanceof ServerLevel serverLevel)) return;

        Vec3 dir = direction != null ? direction : getLookAngle();

        EffectManager.playEffect(EffectIds.CHAOS_VORTEX, getX(), getY(), getZ(), serverLevel,
                EffectParams.of(lifetime, (float) dir.x, (float) dir.y, (float) dir.z));

        if (source != null) {
            activeVortices.put(source, this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(!(level() instanceof ServerLevel serverLevel)) return;

        if (lifetime > 0 && tickCount > lifetime) {
            this.discard();
            return;
        }

        if(tickCount % 10 == 0) {
             level().playSound(null, BlockPos.containing(position()), SoundEvents.WITHER_HURT, source != null ? source.getSoundSource() : getSoundSource(), 1, 1);
             level().playSound(null, BlockPos.containing(position()), SoundEvents.ENDER_DRAGON_FLAP, source != null ? source.getSoundSource() : getSoundSource(), 1, 1);
        }

        for(Entity entity : AbilityUtil.getAllNearbyEntities(source, serverLevel, position(), 60)) {
            float distance = (float) position().distanceTo(entity.position());
            Vec3 directionToVortex = position().subtract(entity.position()).normalize().scale(strength / distance);
            Vec3 deltaMovement = entity.getDeltaMovement().add(directionToVortex);
            if(deltaMovement.length() > 1) {
                deltaMovement = deltaMovement.normalize().scale(1);
            }
            entity.setDeltaMovement(deltaMovement);
            entity.hurtMarked = true;
        }

        AbilityUtil.damageNearbyEntities(serverLevel, source, 17, damage, position(), true, false, false, 10);

    }

    @SubscribeEvent
    public static void onAbilityTargetLocation(TargetLocationEvent event) {
        for(Map.Entry<LivingEntity, ChaosVortexEntity> entry : activeVortices.entrySet()) {
            LivingEntity vortexOwner = entry.getKey();
            ChaosVortexEntity vortex = entry.getValue();
            LivingEntity source = event.getSourceEntity();

            if (vortex == null || vortex.isRemoved() || vortexOwner == null || vortexOwner.isRemoved()) {
                continue;
            }

            if(!AbilityUtil.mayTarget(source, vortexOwner) || !AbilityUtil.mayTarget(vortexOwner, source)) {
                continue;
            }

            Vec3 previousTarget = event.getTargetLocation();

            if(previousTarget.distanceToSqr(vortex.position()) >= 50 * 50) {
                continue;
            }

            event.setTargetLocation(vortex.position());
        }
    }

    @SubscribeEvent
    public static void onAbilityTargetLocation(TargetEntityEvent event) {
        for(Map.Entry<LivingEntity, ChaosVortexEntity> entry : activeVortices.entrySet()) {
            LivingEntity vortexOwner = entry.getKey();
            ChaosVortexEntity vortex = entry.getValue();
            LivingEntity source = event.getSourceEntity();

            if (vortex == null || vortex.isRemoved() || vortexOwner == null || vortexOwner.isRemoved()) {
                continue;
            }

            if(!AbilityUtil.mayTarget(source, vortexOwner) || !AbilityUtil.mayTarget(vortexOwner, source)) {
                continue;
            }

            LivingEntity target = event.getTargetEntity();
            if(target == null || target.isRemoved()) {
                continue;
            }
            Vec3 previousTarget = event.getTargetEntity().position();

            if(previousTarget.distanceToSqr(vortex.position()) >= 50 * 50) {
                continue;
            }

            event.setTargetEntity(null);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }
}