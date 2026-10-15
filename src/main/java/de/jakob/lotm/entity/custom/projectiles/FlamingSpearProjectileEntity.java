package de.jakob.lotm.entity.custom.projectiles;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.data.PriestFlameParticle;
import de.jakob.lotm.util.data.PriestFlameVariants;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

public class FlamingSpearProjectileEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;
    private int ticks = 0;
    private int petrifiedTicks = 0;
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(FlamingSpearProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TRANSFORMED = SynchedEntityData.defineId(FlamingSpearProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> VARIANT_ID = SynchedEntityData.defineId(FlamingSpearProjectileEntity.class, EntityDataSerializers.INT);
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIZE, 1.0f);
        builder.define(VARIANT_ID, PriestFlameVariants.DEFAULT.getId());
        builder.define(TRANSFORMED, false);

    }

    public void setRide(boolean ride) {
        this.entityData.set(TRANSFORMED, ride);
    }
    public boolean isRide() {
        return this.entityData.get(TRANSFORMED);
    }
    public void setVariant(PriestFlameVariants variant) {
        this.entityData.set(VARIANT_ID, variant.getId());
    }

    public PriestFlameVariants getVariant() {
        return PriestFlameVariants.byId(this.entityData.get(VARIANT_ID));
    }

    public void setSize(float size) {
        this.entityData.set(SIZE, size);
    }

    public float getSize() {
        return this.entityData.get(SIZE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Size", this.getSize());
        compound.putInt("Variant", this.getVariant().getId());
        compound.putBoolean("Ride", this.isRide());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSize(compound.getFloat("Size"));
        if (compound.contains("Variant", 3)) {
            this.setVariant(PriestFlameVariants.byId(compound.getInt("Variant")));
        }
        this.setRide(compound.getBoolean("Ride"));
    }

    public FlamingSpearProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        this.setRide(false);
        this.setSize(1.0f);
        init();
    }

    public FlamingSpearProjectileEntity(Level level, LivingEntity owner, double damage, boolean griefing, float size, boolean ride) {
        super(ModEntities.FLAMING_SPEAR.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        this.setRide(ride);
        this.setSize(size);
        this.setBoundingBox(this.getBoundingBox().inflate(size));
        init();
    }

    private void init() {
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        // Petrification Logic -- run before super.tick() to stop movement completely
        if(getTags().contains("petrified")) {
            petrifiedTicks++;
            if(petrifiedTicks >= 20 * 5) {
                this.discard();
            }
            return;
        }

        super.tick();
        if(level.isClientSide)
            return;

        ticks++;
        if(this.owner instanceof Player) {
            if(this.isRide()){
                owner.startRiding(this);
                owner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 1, false, false, false));
                if(ticks > 5 * 20) {
                    this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
                    owner.stopRiding();
                    owner.removeEffect(MobEffects.INVISIBILITY);
                    this.setRide(false);
                    return;
                }
            }
        }

        if(ticks > 20 * 20) {
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
            return;
        }


        if(level.getBlockState(BlockPos.containing(position())).is(Blocks.WATER) || level.getBlockState(BlockPos.containing(position())).is(Blocks.LAVA)) {
            onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
        }
        SimpleParticleType bigtype = PriestFlameParticle.byId(this.getVariant().getId()).getType();
        int density = random.nextInt(1, 20);
        if(bigtype == ParticleTypes.DRAGON_BREATH) density = 30;
        ParticleUtil.spawnParticles((ServerLevel) level, bigtype, position(), density, .33, .045);
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        if(target.level().isClientSide) return false;
        if (this.owner != null) {
            if(this.owner.getStringUUID().equals(target.getStringUUID())) return false;
        }
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {


        this.discard();
        if(this.owner instanceof ServerPlayer player) {
            player.stopRiding();

        }
        if(!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }
        level.explode(owner, target.position().x, target.position().y, target.position().z, 3.5f, griefing, Level.ExplosionInteraction.NONE);
        if(owner != null) target.hurt(ModDamageTypes.source(level, ModDamageTypes.FIRE, owner), (float) damage);
        else              target.hurt(ModDamageTypes.source(level, ModDamageTypes.FIRE), (float) damage);
        target.setRemainingFireTicks(target.getRemainingFireTicks() + 20 * 6);

        if(!level.isClientSide && owner != null)
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, position(), owner, null, new String[]{"burning", "explosion"}, 3.5, 10));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if(this.owner instanceof ServerPlayer player && this.isRide()) player.stopRiding();
        this.discard();

        if(griefing) {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 4f, true, Level.ExplosionInteraction.MOB);
        }
        else {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 4f, false, Level.ExplosionInteraction.NONE);
        }
        if(!level.isClientSide && owner != null)
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, result.getLocation(), owner, null, new String[]{"burning", "explosion"}, 4, 10));
    }



    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.FOOL_Card.get());
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
}
