package de.jakob.lotm.entity.custom.projectiles;

import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Random;
import org.joml.Vector3f;

import java.awt.*;

public class SpiritBallEntity extends AbstractHurtingProjectile {

    private final float damage;

    public SpiritBallEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.damage = 5;
        this.setNoGravity(true);
    }

    public SpiritBallEntity(Level level, LivingEntity owner, float damage, Vec3 direction, float speed) {
        super(ModEntities.SPIRIT_BALL.get(), owner, direction.normalize().scale(speed), level);
        this.damage = damage;
        this.setNoGravity(true);
    }


    @Override
    public void tick() {
        super.tick();
        if(level().isClientSide)
            return;

        if (this.tickCount > 20 * 5) {
            this.discard();
            return;
        }

        Random random = new Random(getUUID().getMostSignificantBits());
        float hue        = random.nextFloat();
        float saturation = 0.7f + random.nextFloat() * 0.3f;
        float brightness = 0.8f + random.nextFloat() * 0.2f;

        int rgb   = Color.HSBtoRGB(hue, saturation, brightness);
        float red   = ((rgb >> 16) & 0xFF) / 255f;
        float green = ((rgb >> 8)  & 0xFF) / 255f;
        float blue  = ( rgb        & 0xFF) / 255f;

        final DustParticleOptions ballDust = new DustParticleOptions(new Vector3f(red, green, blue), 1);

        ParticleUtil.spawnParticles((ServerLevel) level(), ballDust, this.position(), 2, .05);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity target) || result.getEntity() == this.getOwner())
            return;

        // check if the owner exists before - to not crash
        if (this.getOwner() instanceof LivingEntity livingOwner) {
            target.hurt(ModDamageTypes.source(level(), ModDamageTypes.BEYONDER_GENERIC, livingOwner), (float) damage);
        } else {
            target.hurt(ModDamageTypes.source(level(), ModDamageTypes.BEYONDER_GENERIC), (float) damage);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
    }
}
