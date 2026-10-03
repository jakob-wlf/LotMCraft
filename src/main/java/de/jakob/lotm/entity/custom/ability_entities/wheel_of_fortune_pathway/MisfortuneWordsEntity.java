package de.jakob.lotm.entity.custom.ability_entities.wheel_of_fortune_pathway;

import de.jakob.lotm.attachments.LuckComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class MisfortuneWordsEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(MisfortuneWordsEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Integer> AFFECTED_ENTITIES_COUNT =
            SynchedEntityData.defineId(MisfortuneWordsEntity.class, EntityDataSerializers.INT);

    private final ArrayList<UUID> affectedEntities = new ArrayList<>();

    public MisfortuneWordsEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
        builder.define(AFFECTED_ENTITIES_COUNT, 0);
    }

    public MisfortuneWordsEntity(Level level, Vec3 pos) {
        this(ModEntities.MISFORTUNE_WORDS.get(), level);
        this.setPos(pos);
        this.setXRot(90);
        this.setYRot(0);
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level() instanceof ServerLevel serverLevel) {
            AbilityUtil.getNearbyEntities(null, serverLevel, this.position(), this.getBoundingBox().getXsize()).forEach(e -> {
                if(BeyonderData.isBeyonder(e) && BeyonderData.getPathway(e).equalsIgnoreCase("wheel_of_fortune") && BeyonderData.getSequence(e) <= 2)
                    return;

                if(getCasterEntity() != null && !AbilityUtil.mayTarget(getCasterEntity(), e))
                    return;

                LuckComponent luckComponent = e.getData(ModAttachments.LUCK_COMPONENT.get());
                int luck = luckComponent.getLuck();

                if(luck < 1000 && luck > -3000) {
                    luckComponent.setLuck(-3000);
                }

                if(!affectedEntities.contains(e.getUUID())) {
                    affectedEntities.add(e.getUUID());
                    setAffectedEntitiesCount(getAffectedEntitiesCount() + (e instanceof Player ? 10 : 1));
                }
            });

            if(getAffectedEntitiesCount() >= 30) {
                this.discard();
            }
        }
    }

    public LivingEntity getCasterEntity() {
        if(level().isClientSide) {
            return null;
        }
        UUID casterUUID = this.getCasterUUID();
        if (casterUUID == null) {
            return null;
        }
        return ((ServerLevel) level()).getEntity(casterUUID) != null && ((ServerLevel) level()).getEntity(casterUUID) instanceof LivingEntity livingEntity ?
                livingEntity: null;
    }

    public void setCasterUUID(UUID uuid) {
        this.entityData.set(OWNER, Optional.ofNullable(uuid));
    }

    public UUID getCasterUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setAffectedEntitiesCount(int count) {
        this.entityData.set(AFFECTED_ENTITIES_COUNT, count);
    }

    public int getAffectedEntitiesCount() {
        return this.entityData.get(AFFECTED_ENTITIES_COUNT);
    }


    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("owner")) {
            setCasterUUID(compoundTag.getUUID("owner"));
        } else {
            setCasterUUID(null);
        }

        setAffectedEntitiesCount(compoundTag.getInt("affectedEntitiesCount"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (getCasterUUID() != null) {
            compoundTag.putUUID("owner", getCasterUUID());
        }
        compoundTag.putInt("affectedEntitiesCount", getAffectedEntitiesCount());
    }


    @Override
    public boolean isPickable() {
        return false; // Players can't interact with it directly
    }

    @Override
    public boolean isPushable() {
        return false; // Can't be pushed by other entities
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false; // No passengers allowed
    }
}