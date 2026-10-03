package de.jakob.lotm.entity.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DamageTrackerEntity extends PathfinderMob {

    // Configurable regen delay in ticks (default 40 = 2 seconds)
    public static final EntityDataAccessor<Integer> REGEN_DELAY_TICKS =
            SynchedEntityData.defineId(DamageTrackerEntity.class, EntityDataSerializers.INT);

    private static final int DEFAULT_REGEN_DELAY = 40;
    private static final float MAX_HEALTH = 10000.0f;

    private float accumulatedDamage = 0.0f;
    private int regenCountdown = 0;

    public DamageTrackerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNoAi(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(REGEN_DELAY_TICKS, DEFAULT_REGEN_DELAY);
    }

    public int getRegenDelayTicks() {
        return this.entityData.get(REGEN_DELAY_TICKS);
    }

    public void setRegenDelayTicks(int ticks) {
        this.entityData.set(REGEN_DELAY_TICKS, ticks);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.level().isClientSide) {
            return false;
        }

        if (amount <= 0.0f) {
            return false;
        }

        // Reduce health but never below 1 to prevent death
        float currentHealth = this.getHealth();
        this.setHealth(Math.max(1.0f, currentHealth - amount));
        float actualDamage = amount;

        accumulatedDamage += actualDamage;
        if(regenCountdown <= 0)
            regenCountdown = getRegenDelayTicks();

        // Print individual hit damage to all players on the server
        broadcastMessage(Component.literal(
                "[DamageTracker] Hit! Damage: " + String.format("%.2f", actualDamage)
        ));

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        this.setRemainingFireTicks(0);

        if (regenCountdown > 0) {
            regenCountdown--;

            if (regenCountdown == 0 && accumulatedDamage > 0.0f) {
                float regenDelaySeconds = getRegenDelayTicks() / 20.0f;
                float dps = accumulatedDamage / regenDelaySeconds;

                broadcastMessage(Component.literal(
                        "[DamageTracker] Session over! Total damage: " + String.format("%.2f", accumulatedDamage)
                                + " | DPS: " + String.format("%.2f", dps) + " (over " + String.format("%.1f", regenDelaySeconds) + "s)"
                ));

                // Regenerate all health
                this.setHealth(MAX_HEALTH);
                accumulatedDamage = 0.0f;
            }
        }
    }

    @Override
    protected @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            this.discard();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void registerGoals() {
        // No AI goals - this entity is a static dummy
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    private void broadcastMessage(Component message) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    // Prevent natural death from damage
    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return false;
    }

    // Keep the entity alive even at 0 health (we handle regen ourselves)
    @Override
    protected void checkFallDamage(double y, boolean onGround,
            net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.core.BlockPos pos) {
        // No fall damage
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }
}
