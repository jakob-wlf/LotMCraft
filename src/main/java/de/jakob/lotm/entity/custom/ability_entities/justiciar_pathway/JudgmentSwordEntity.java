package de.jakob.lotm.entity.custom.ability_entities.justiciar_pathway;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;
import java.util.UUID;

public class JudgmentSwordEntity extends Entity {

    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(JudgmentSwordEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(JudgmentSwordEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> TARGET_UUID =
            SynchedEntityData.defineId(JudgmentSwordEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private Ability ability;
    boolean hasHitGround = false;
    int lifeTime = 0;

    public JudgmentSwordEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setDamage(10.0F);
        setOwnerUUID(Optional.empty());
        setTargetUUID(Optional.empty());
    }

    public JudgmentSwordEntity(Level level, Vec3 position, float damage,
                                LivingEntity owner, LivingEntity target, Ability ability) {
        this(ModEntities.JUDGMENT_SWORD.get(), level);
        this.setPos(position);
        this.setDamage(damage);
        this.ability = ability;
        if (owner != null) setOwnerUUID(Optional.of(owner.getUUID()));
        if (target != null) setTargetUUID(Optional.of(target.getUUID()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 10.0F);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(TARGET_UUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setDamage(tag.getFloat("Damage"));
        if (tag.contains("OwnerUUID")) setOwnerUUID(Optional.of(tag.getUUID("OwnerUUID")));
        if (tag.contains("TargetUUID")) setTargetUUID(Optional.of(tag.getUUID("TargetUUID")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", getDamage());
        if (getOwnerUUID() != null) tag.putUUID("OwnerUUID", getOwnerUUID());
        if (getTargetUUID() != null) tag.putUUID("TargetUUID", getTargetUUID());
    }

    public void setDamage(float v) { entityData.set(DAMAGE, v); }
    public float getDamage()       { return entityData.get(DAMAGE); }

    public void setOwnerUUID(Optional<UUID> id) { entityData.set(OWNER_UUID, id); }
    public UUID getOwnerUUID() { return entityData.get(OWNER_UUID).orElse(null); }

    public void setTargetUUID(Optional<UUID> id) { entityData.set(TARGET_UUID, id); }
    public UUID getTargetUUID() { return entityData.get(TARGET_UUID).orElse(null); }

    private LivingEntity getOwner(ServerLevel lvl) {
        UUID id = getOwnerUUID();
        if (id == null) return null;
        Entity e = lvl.getEntity(id);
        return e instanceof LivingEntity le ? le : null;
    }

    private LivingEntity getTarget(ServerLevel lvl) {
        UUID id = getTargetUUID();
        if (id == null) return null;
        Entity e = lvl.getEntity(id);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override
    public void tick() {
        super.tick();

        lifeTime++;
        if (lifeTime > 20 * 5) {
            discard();
            return;
        }

        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0, -1D, 0));
        }
        move(MoverType.SELF, getDeltaMovement());

        if (onGround() && !hasHitGround) {
            hasHitGround = true;

            if (level().isClientSide()) {
                ClientHandler.applyCameraShakeToPlayersInRadius(3f, 25, (ClientLevel) level(), position(), 50);
            } else {
                ServerLevel serverLevel = (ServerLevel) level();
                LivingEntity target = getTarget(serverLevel);
                if (target != null) {
                    target.setHealth(target.getHealth() - getDamage());
                    target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, getOwner(serverLevel)), 1);
                }

                // Gold-white impact ring
                RingEffectManager.createRingForAll(position(), 6, 30,
                        1.0f, 0.96f, 0.72f, 0.85f, 1f, 3.5f, serverLevel);

                // Scatter surface blocks outward
                AbilityUtil.getBlocksInCircleOutline(serverLevel, position().subtract(0, 1, 0), 4)
                        .forEach(b -> spawnFallingBlocks(serverLevel, position(), b));

                if (ability != null) {
                    LivingEntity owner = getOwner(serverLevel);
                    if (owner != null) {
                        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, position(), owner, ability,
                                ability.getInteractionFlags(), ability.getInteractionRadius(),
                                ability.getInteractionCacheTicks()));
                    }
                }
            }
        }
    }

    private void spawnFallingBlocks(Level level, Vec3 startPos, BlockPos b) {
        if (random.nextInt(3) != 0) return;
        BlockState state = level.getBlockState(b);
        BlockState above = level.getBlockState(b.above());
        if (state.getCollisionShape(level, b).isEmpty() ||
                !above.getCollisionShape(level, b.above()).isEmpty()) return;

        Vec3 dir = new Vec3(b.getX() + 0.5 - startPos.x, 0, b.getZ() + 0.5 - startPos.z).normalize();
        FallingBlockEntity block = FallingBlockEntity.fall(level, b.above(), state);
        block.setDeltaMovement(new Vec3(dir.x, 1, dir.z).normalize().scale(0.65));
        block.disableDrop();
        block.hurtMarked = true;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}