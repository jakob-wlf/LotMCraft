package de.jakob.lotm.entity.custom.ability_entities.demoness_pathway;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChaosVortexEntity extends Entity {

    private int strength = 5;
    private int lifetime = 20 * 5;

    private Vec3 direction = null;

    public ChaosVortexEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ChaosVortexEntity(Level level, Vec3 pos, int strength, int lifetime) {
        super(ModEntities.CHAOS_VORTEX.get(), level);
        this.strength = strength;
        this.lifetime = lifetime;
        this.setPos(pos);
    }

    public ChaosVortexEntity(Level level, Vec3 pos, int strength, int lifetime, Vec3 direction) {
        this(level, pos, strength, lifetime);
        this.direction = direction;
        setPos(pos);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if(!(level() instanceof ServerLevel serverLevel)) return;

        Vec3 dir = direction != null ? direction : getLookAngle();

        EffectManager.playEffect(EffectIds.CHAOS_VORTEX, getX(), getY(), getZ(), serverLevel,
                EffectParams.of(lifetime, (float) dir.x, (float) dir.y, (float) dir.z));
    }

    @Override
    public void tick() {
        super.tick();

        if (lifetime > 0 && tickCount > lifetime) {
            this.discard();
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