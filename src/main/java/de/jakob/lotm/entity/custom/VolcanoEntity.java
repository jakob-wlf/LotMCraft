package de.jakob.lotm.entity.custom;

import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ExplodingFallingBlockHelper;
import de.jakob.lotm.util.helper.RingEffectManager;
import de.jakob.lotm.util.scheduling.ServerScheduler;
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

import java.util.Optional;
import java.util.UUID;

public class VolcanoEntity extends Entity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(VolcanoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(VolcanoEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public VolcanoEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);

        setDamage(10.0F);
        setOwnerUUID(Optional.empty());
    }

    public VolcanoEntity(Level level, Vec3 position, float damage, LivingEntity owner) {
        this(ModEntities.VOLCANO.get(), level);
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


    int lifeTime = 0;

    @Override
    public void tick() {
        super.tick();

        lifeTime++;
        if(lifeTime > 20 * 60) {
            this.discard();
            return;
        }

        if(!level().isClientSide()) {
            boolean griefing = BeyonderData.isGriefingEnabled(getOwner((ServerLevel) level()));
            AbilityUtil.damageNearbyEntities((ServerLevel) level(), getOwner((ServerLevel) level()), griefing ? 16.5f : 34, getDamage() / 4, position(), true, false, 20 * 10);
            for(int i = 0; i < 3; i++) {
                spawnFallingBlocks(level(), position().add(0, 20.5, 0), BeyonderData.isGriefingEnabled(getOwner((ServerLevel) level())), getOwner((ServerLevel) level()));
            }
        }
    }

    private void spawnFallingBlocks(Level level, Vec3 startPos, boolean griefing, LivingEntity owner) {
        BlockPos blockPos = BlockPos.containing(startPos);
        BlockState state = switch (random.nextInt(5)) {
            default -> Blocks.MAGMA_BLOCK.defaultBlockState();
            case 1 -> griefing ? Blocks.LAVA.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState();
            case 2 -> Blocks.OBSIDIAN.defaultBlockState();
        };

        Vec3 velocity = new Vec3(
                (random.nextDouble() - 0.5) * 1.5,
                random.nextDouble() * 0.4 + 0.6,
                (random.nextDouble() - 0.5) * 1.5
        ).scale(1.5);

        ExplodingFallingBlockHelper.spawnExplodingFallingBlock(
                level,
                startPos,
                state,
                velocity,
                griefing,
                5.0F,
                getDamage(),
                false,
                owner
        );
    }
}