package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class PaperDaggerProjectileEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;

    private int ticks = 0;

    public PaperDaggerProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        init();
    }

    public PaperDaggerProjectileEntity(Level level, LivingEntity owner, double damage) {
        super(ModEntities.PAPER_DAGGER.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
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
        if(ticks > 20 * 3) {
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
            return;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        LivingEntity target = (LivingEntity) result.getEntity();
        target.hurt(this.damageSources().mobAttack(owner), (float) damage);
        level.addFreshEntity(new ItemEntity(level, result.getLocation().x, result.getLocation().y, result.getLocation().z, new ItemStack(Items.PAPER)));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
        level.addFreshEntity(new ItemEntity(level, result.getLocation().x, result.getLocation().y, result.getLocation().z, new ItemStack(Items.PAPER)));
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
