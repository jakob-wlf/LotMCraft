package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FlamingSpearProjectileEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;

    private int ticks = 0;

    public FlamingSpearProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        init();
    }

    public FlamingSpearProjectileEntity(Level level, LivingEntity owner, double damage, boolean griefing) {
        super(ModEntities.FLAMING_SPEAR.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        init();
    }

    private void init() {
        this.setNoGravity(true);
    }

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

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, position(), random.nextInt(1, 15), .33, .045);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        LivingEntity target = (LivingEntity) result.getEntity();
        level.explode(owner, target.position().x, target.position().y, target.position().z, 3.5f, griefing, Level.ExplosionInteraction.NONE);
        target.hurt(this.damageSources().mobAttack(owner), (float) damage);
        target.setRemainingFireTicks(target.getRemainingFireTicks() + 20 * 6);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
        if(griefing) {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 4f, true, Level.ExplosionInteraction.MOB);
        }
        else {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 4f, false, Level.ExplosionInteraction.NONE);
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
