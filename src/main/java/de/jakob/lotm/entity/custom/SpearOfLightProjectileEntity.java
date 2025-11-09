package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class SpearOfLightProjectileEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;

    private int ticks = 0;

    public SpearOfLightProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        init();
    }

    public SpearOfLightProjectileEntity(Level level, LivingEntity owner, double damage, boolean griefing) {
        super(ModEntities.SPEAR_OF_LIGHT.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        init();
    }

    private void init() {
        this.setNoGravity(true);
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(255 / 255f, 177 / 255f, 10 / 255f),
            1.5f
    );

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)
            return;

        ticks++;
        if(ticks > 20 * 20) {
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
            return;
        }

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, position(), 8, .3, .3, .3, 0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) result.getEntity();
        target.hurt(this.damageSources().mobAttack(owner), (float) damage);

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 8, 10, false, false, false));

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetPos = target.position();

        ServerScheduler.scheduleForDuration(0, 4, 20 * 8, () -> {
            ParticleUtil.spawnSphereParticles(serverLevel, ParticleTypes.END_ROD, targetPos, 3.5f, 100);
        });
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(griefing) {
            AbilityUtil.getBlocksInSphereRadius(serverLevel, result.getLocation(), 7, true, true, false).forEach(blockPos -> {
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, blockPos.getCenter(), 3, 0.1, 0);
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            });
        }
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
