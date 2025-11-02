package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.helper.RingExpansionRenderer;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

import java.util.Optional;
import java.util.UUID;

public class JusticeSwordEntity extends Entity {

    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(JusticeSwordEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(JusticeSwordEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public JusticeSwordEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);

        setDamage(10.0F);
        setOwnerUUID(Optional.empty());
    }

    public JusticeSwordEntity(Level level, Vec3 position, float damage, LivingEntity owner) {
        this(ModEntities.JUSTICE_SWORD.get(), level);
        this.setPos(position);
        this.setDamage(damage);
        if (owner != null) {
            this.setOwnerUUID(Optional.of(owner.getUUID()));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 10.0F);
        builder.define(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setDamage(compoundTag.getFloat("Damage"));
        if(compoundTag.contains("OwnerUUID")) {
            setOwnerUUID(Optional.of(compoundTag.getUUID("OwnerUUID")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putFloat("Damage", getDamage());
        if(getOwnerUUID() != null) {
            compoundTag.putUUID("OwnerUUID", getOwnerUUID());
        }
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    public void setOwnerUUID(Optional<UUID> ownerUUID) {
        this.entityData.set(OWNER_UUID, ownerUUID);
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).isPresent() ? this.entityData.get(OWNER_UUID).get() : null;
    }

    public LivingEntity getOwner(ServerLevel level) {
        UUID ownerUUID = getOwnerUUID();
        if(ownerUUID == null)
            return null;

        Entity entity = level.getEntity(ownerUUID);
        if(entity instanceof LivingEntity living)
            return living;

        return null;
    }


    boolean hasHitGround = false;

    int lifeTime = 0;

    @Override
    public void tick() {
        super.tick();

        lifeTime++;
        if(lifeTime > 20 * 5) {
            this.discard();
            return;
        }

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04D, 0)); // gravity
        }
        this.move(MoverType.SELF, this.getDeltaMovement());

        if(onGround() && !hasHitGround) {
            hasHitGround = true;
            playHitAnimation();

            if(!level().isClientSide()) {
                AbilityUtil.damageNearbyEntities((ServerLevel) level(), getOwner((ServerLevel) level()), 3.75f, getDamage(), position(), true, false);
            }
        }
    }

    private void playHitAnimation() {
        if(level().isClientSide()) {
            ClientHandler.applyCameraShake(4f, 35);
        }
        else {
            RingEffectManager.createRingForAll(position(), 8, 35, 252 / 255f, 177 / 255f, 3 / 255f, .75f, 1f, 4f, (ServerLevel) level());
            AbilityUtil.getBlocksInCircleOutline((ServerLevel) level(), position().subtract(0, 1, 0), 5).forEach(b -> spawnFallingBlocks(level(), position(), b, false));
            AbilityUtil.getBlocksInCircleOutline((ServerLevel) level(), position().subtract(0, 1, 0), 3).forEach(b -> spawnFallingBlocks(level(), position(), b, false));
        }
    }

    private void spawnFallingBlocks(Level level, Vec3 startPos, BlockPos b, boolean griefing) {
        if(random.nextInt(2) != 0)
            return;

        BlockState state = level.getBlockState(b);
        BlockState above = level.getBlockState(b.above());
        if(state.getCollisionShape(level, b).isEmpty() || !above.getCollisionShape(level, b.above()).isEmpty())
            return;

        Vec3 vectorFromCenter = new Vec3(b.getX() + 0.5 - startPos.x, 0, b.getZ() + 0.5 - startPos.z).normalize();
        Vec3 movement = (new Vec3(vectorFromCenter.x, 1, vectorFromCenter.z)).normalize().scale(.75);

        FallingBlockEntity block = FallingBlockEntity.fall(level, b.above(), state);
        block.setDeltaMovement(movement);
        if(!griefing)
            block.disableDrop();
        else {
            level.setBlockAndUpdate(b, Blocks.AIR.defaultBlockState());
        }
        block.hurtMarked = true;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

}
