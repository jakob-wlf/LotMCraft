package de.jakob.lotm.entity.custom.ability_entities.twilight_giant;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class HurricaneOfLightEntity extends TwilightVisualEntity {
    public static final float HEIGHT = 10.0F;
    public static final float BASE_RADIUS = 6.2F;
    public static final float TOP_RADIUS = 11.5F;
    public static final int FADE_TICKS = 13;

    private static final EntityDataAccessor<Integer> OWNER =
            SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RELEASING =
            SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RELEASE_AGE =
            SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TWILIGHT =
            SynchedEntityData.defineId(HurricaneOfLightEntity.class, EntityDataSerializers.BOOLEAN);

    public HurricaneOfLightEntity(EntityType<? extends HurricaneOfLightEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, -1);
        builder.define(RELEASING, false);
        builder.define(RELEASE_AGE, 0);
        builder.define(TWILIGHT, false);
    }

    public void setTwilight(boolean twilight) {
        this.entityData.set(TWILIGHT, twilight);
    }

    public boolean twilight() {
        return this.entityData.get(TWILIGHT);
    }

    public void setOwner(Entity owner) {
        this.entityData.set(OWNER, owner.getId());
    }

    public Entity owner() {
        return this.level().getEntity(this.entityData.get(OWNER));
    }

    public boolean releasing() {
        return this.entityData.get(RELEASING);
    }

    public void release() {
        if (releasing()) return;
        this.entityData.set(RELEASING, true);
        this.entityData.set(RELEASE_AGE, this.age);
    }

    public float fade(float partialTick) {
        if (!releasing()) return 0.0F;
        return Mth.clamp((getAge(partialTick) - this.entityData.get(RELEASE_AGE)) / FADE_TICKS, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = owner();
        if (owner != null) {
            setPos(owner.getX(), owner.getY(), owner.getZ());
        } else if (!this.level().isClientSide && this.age > 2 && !releasing()) {
            release();
        }
        if (releasing() && this.age >= this.entityData.get(RELEASE_AGE) + FADE_TICKS + (this.level().isClientSide ? 8 : 0)) {
            discard();
        }
    }
}
