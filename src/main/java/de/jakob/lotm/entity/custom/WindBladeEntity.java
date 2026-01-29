package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WindBladeEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;

    Vec3 lastPos = null;

    private int ticks = 0;

    public WindBladeEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        init();
    }

    public WindBladeEntity(Level level, LivingEntity owner, double damage, boolean griefing) {
        super(ModEntities.WIND_BLADE.get(), level);
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

        if(lastPos != null) {
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, lastPos, 1, .15, 1, .15, 0.02);
            ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.EFFECT, lastPos, 4, .15, 1, .15, 0.02);
        }

        lastPos = position();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity))
            return;
        LivingEntity target = (LivingEntity) result.getEntity();
        target.hurt(this.damageSources().mobAttack(owner), (float) damage);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        this.discard();
        if(griefing) {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 1.75f, false, Level.ExplosionInteraction.MOB);
        }
        else {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 1.75f, false, Level.ExplosionInteraction.NONE);
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
