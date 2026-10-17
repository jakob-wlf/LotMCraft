package de.jakob.lotm.entity.custom.projectiles;

import de.jakob.lotm.beyonders.abilities.black_emperor.BribeAbility;
import de.jakob.lotm.beyonders.abilities.black_emperor.BribeAbility.BribeTypes;
import de.jakob.lotm.entity.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class BribeProjectile extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Integer> DATA_BRIBE_TYPE =
            SynchedEntityData.defineId(BribeProjectile.class, EntityDataSerializers.INT);

    public BribeProjectile(EntityType<? extends BribeProjectile> type, Level level) {
        super(type, level);
    }

    public BribeProjectile(Level level, LivingEntity shooter, ItemStack item, BribeTypes bribeTypes) {
        super(ModEntities.BRIBE_PROJECTILE.get(), shooter, level);
        this.setItem(item.copy());
        this.entityData.set(DATA_BRIBE_TYPE, bribeTypes.ordinal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BRIBE_TYPE, 0);
    }

    public BribeTypes getBribeType() {
        return BribeTypes.VALUES[this.entityData.get(DATA_BRIBE_TYPE)];
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.GOLD_NUGGET;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (!(hit instanceof LivingEntity target)) return;
        if (!(this.getOwner() instanceof LivingEntity player)) return;
        ItemStack gift = this.getItem().copy();
        if (!gift.isEmpty()) {
            if (target instanceof Player targetPlayer) {
                if (!targetPlayer.getInventory().add(gift)) {
                    if (!gift.isEmpty()) {
                        targetPlayer.drop(gift, false);
                    }
                }
            } else {
                target.spawnAtLocation(gift, 0.5f);
            }
        }
        BribeAbility.apply(this.level(), player, target, getBribeType());
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.spawnAtLocation(this.getItem());
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}