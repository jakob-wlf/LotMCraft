package de.jakob.lotm.entity.custom;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.SpiritWorldHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class TravelersDoorEntity extends Entity {
    private double destX;
    private double destY;
    private double destZ;

    /**
     * 0 = coordinates
     * 1 = spectating
     * 2 = spirit world
     */
    private int use = 0;

    private static final double TELEPORT_RANGE = 1.0;


    public TravelersDoorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.destX = 0.0;
        this.destY = 0.0;
        this.destZ = 0.0;
        this.use = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public TravelersDoorEntity(EntityType<? extends TravelersDoorEntity> type, Level level, Vec3 facing, Vec3 center, int use) {
        this(type, level);

        Vec3 dir = new Vec3(facing.x, 0.0, facing.z);
        float yaw = yawFromVector(dir);
        float pitch = 0.0F;

        this.moveTo(center.x, center.y, center.z, yaw, pitch);

        this.use = use;
    }

    // Constructor with destination coordinates
    public TravelersDoorEntity(EntityType<? extends TravelersDoorEntity> type, Level level, Vec3 facing, Vec3 center, double destX, double destY, double destZ) {
        this(type, level, facing, center, 0);
        this.destX = destX;
        this.destY = destY;
        this.destZ = destZ;

        while(!level.getBlockState(BlockPos.containing(destX, this.destY, destZ)).getCollisionShape(level, BlockPos.containing(destX, this.destY, destZ)).isEmpty()) {
            this.destY += 1.0;
        }
    }

    private static float yawFromVector(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6) return 0.0F;
        return (float)(Math.toDegrees(Math.atan2(-dir.x, dir.z)));
    }

    @Override
    public void tick() {
        super.tick();

        if(!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.setChunkForced(chunkPosition().x, chunkPosition().z, true);


        if(tickCount > 20 * 6) {
            this.discard();
            return;
        }

        switch(use) {
            case 0 -> teleportNearbyEntities();
            case 2 -> spiritWorldHandling();
        }
    }

    private void spiritWorldHandling() {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(TELEPORT_RANGE), e -> e != this && e.isAlive())) {
                ResourceKey<Level> spiritWorld = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "spirit_world"));
                ServerLevel spiritWorldLevel = serverLevel.getServer().getLevel(spiritWorld);
                if (spiritWorldLevel == null) return;

                if (!serverLevel.dimension().equals(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY)) {

                    Vec3 coords = SpiritWorldHandler.getCoordinatesInSpiritWorld(entity.position());
                    BlockPos pos = BlockPos.containing(coords);

                    // elevate until not inside a block
                    while (!spiritWorldLevel.getBlockState(pos).isAir()) {
                        pos = pos.above();
                    }

                    // ensure block beneath exists
                    BlockPos below = pos.below();
                    if (spiritWorldLevel.getBlockState(below).isAir()) {
                        spiritWorldLevel.setBlockAndUpdate(below, Blocks.END_STONE.defaultBlockState());
                    }

                    entity.teleportTo(spiritWorldLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                            Set.of(), entity.getYRot(), entity.getXRot());

                } else {
                    ResourceKey<Level> OVERWORLD = Level.OVERWORLD;
                    ServerLevel overworldLevel = serverLevel.getServer().getLevel(OVERWORLD);
                    if (overworldLevel == null) return;

                    Vec3 coords = SpiritWorldHandler.getCoordinatesInOverworld(entity.position());
                    BlockPos pos = BlockPos.containing(coords);

                    while (!overworldLevel.getBlockState(pos).isAir()) {
                        pos = pos.above();
                    }

                    BlockPos below = pos.below();
                    if (overworldLevel.getBlockState(below).isAir()) {
                        overworldLevel.setBlockAndUpdate(below, Blocks.STONE.defaultBlockState());
                    }

                    entity.teleportTo(overworldLevel, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                            Set.of(), entity.getYRot(), entity.getXRot());
                }
            }
        }
    }


    private void teleportNearbyEntities() {
        if (!this.level().isClientSide) {
            for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(TELEPORT_RANGE), e -> e != this && e.isAlive())) {
                entity.teleportTo(destX, destY, destZ);

                entity.resetFallDistance();
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("DestX")) {
            this.destX = compoundTag.getDouble("DestX");
        }
        if (compoundTag.contains("DestY")) {
            this.destY = compoundTag.getDouble("DestY");
        }
        if (compoundTag.contains("DestZ")) {
            this.destZ = compoundTag.getDouble("DestZ");
        }
        if(compoundTag.contains("Use")) {
            this.use = compoundTag.getInt("Use");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putDouble("DestX", this.destX);
        compoundTag.putDouble("DestY", this.destY);
        compoundTag.putDouble("DestZ", this.destZ);
        compoundTag.putInt("Use", this.use);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    public double getDestX() {
        return destX;
    }

    public void setDestX(double destX) {
        this.destX = destX;
    }

    public double getDestY() {
        return destY;
    }

    public void setDestY(double destY) {
        this.destY = destY;
    }

    public double getDestZ() {
        return destZ;
    }

    public void setDestZ(double destZ) {
        this.destZ = destZ;
    }

    public void setDestination(double x, double y, double z) {
        this.destX = x;
        this.destY = y;
        this.destZ = z;
    }
}