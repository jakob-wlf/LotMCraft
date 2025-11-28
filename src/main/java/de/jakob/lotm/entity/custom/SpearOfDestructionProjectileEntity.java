package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class SpearOfDestructionProjectileEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;

    private int ticks = 0;

    public SpearOfDestructionProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        init();
    }

    public SpearOfDestructionProjectileEntity(Level level, LivingEntity owner, double damage, boolean griefing) {
        super(ModEntities.SPEAR_OF_DESTRUCTION.get(), level);
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

        if(level.getBlockState(BlockPos.containing(position())).is(Blocks.WATER) || level.getBlockState(BlockPos.containing(position())).is(Blocks.LAVA)) {
            onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
        }

        ParticleUtil.spawnParticles((ServerLevel) level, ModParticles.PURPLE_FLAME.get(), position(), 8, .3, .3, .3, 0);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.FLAME, position(), 8, .3, .3, .3, 0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity) result.getEntity();
        target.hurt(this.damageSources().mobAttack(owner), (float) damage);

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.CONQUERING, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), serverLevel);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        EffectManager.playEffect(EffectManager.Effect.CONQUERING, result.getLocation().x, result.getLocation().y, result.getLocation().z, serverLevel);

        serverLevel.explode(getOwner(), result.getLocation().x, result.getLocation().y, result.getLocation().z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        serverLevel.explode(getOwner(), result.getLocation().x, result.getLocation().y, result.getLocation().z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        serverLevel.explode(getOwner(), result.getLocation().x, result.getLocation().y, result.getLocation().z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        serverLevel.explode(getOwner(), result.getLocation().x, result.getLocation().y, result.getLocation().z, 9, griefing, griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
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
