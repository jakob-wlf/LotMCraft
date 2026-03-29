package de.jakob.lotm.entity.custom.ability_entities.projectiles;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class UnshadowedSpearProjectileEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;
    private final Ability ability;

    private int ticks = 0;
    private int petrifiedTicks = 0;

    public UnshadowedSpearProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        this.ability = null;
        init();
    }

    public UnshadowedSpearProjectileEntity(Level level, LivingEntity owner, double damage, boolean griefing, Ability ability) {
        super(ModEntities.UNSHADOWED_SPEAR.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        this.ability = ability;
        init();
    }

    private void init() {
        this.setNoGravity(true);
    }

    private void postAbilityUsedEvent(Vec3 impactPos) {
        if(owner != null && ability != null && level instanceof ServerLevel serverLevel) {
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, impactPos, owner, ability,
                    ability.getInteractionFlags(), ability.getInteractionRadius(), ability.getInteractionCacheTicks()));
        }
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(255 / 255f, 177 / 255f, 10 / 255f),
            1.5f
    );

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
        if(ticks > 20 * 20) {
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
            return;
        }


        if(level.getBlockState(BlockPos.containing(position())).is(Blocks.WATER) || level.getBlockState(BlockPos.containing(position())).is(Blocks.LAVA)) {
            onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
        }

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.END_ROD, position(), 5, .3, .3, .3, 0);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity))
            return;
        LivingEntity target = (LivingEntity) result.getEntity();
        level.explode(owner, target.position().x, target.position().y, target.position().z, 3.5f, false, Level.ExplosionInteraction.NONE);
        // check if the owner exists before - to not crash
        if (this.getOwner() instanceof LivingEntity livingOwner) {
            target.hurt(this.damageSources().mobAttack(livingOwner), (float) damage);
        } else {
            target.hurt(this.damageSources().thrown(this, null), (float) damage);
        }
        postAbilityUsedEvent(target.position());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        postAbilityUsedEvent(result.getLocation());
        this.discard();
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
