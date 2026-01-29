package de.jakob.lotm.entity.custom;

import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MisfortuneWordsEntity extends Entity {

    public MisfortuneWordsEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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

                e.addEffect(new MobEffectInstance(ModEffects.UNLUCK, 20 * 60 * 5, 12, false, false, false));
            });
        }
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
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