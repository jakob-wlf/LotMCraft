package de.jakob.lotm.entity.custom.projectiles;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonBlockEffectPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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

import java.util.ConcurrentModificationException;
import java.util.List;

public class WaterBoltEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;

    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(WaterBoltEntity.class, EntityDataSerializers.FLOAT);

    Vec3 lastPos = null;
    Double blocksPerTick = null;

    private int ticks = 0;
    private EntityEffectExecutor executor;

    public WaterBoltEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        this.setSize(1.0f);
        init();
    }

    public WaterBoltEntity(Level level, LivingEntity owner, double damage, boolean griefing) {
        super(ModEntities.WATER_BOLT.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        this.setSize(1.0f);
        init();
    }

    public WaterBoltEntity(Level level, LivingEntity owner, double damage, boolean griefing, float size) {
        super(ModEntities.WATER_BOLT.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        this.setSize(size);
        this.setBoundingBox(this.getBoundingBox().inflate(size));
        init();
    }

    private void init() {
        this.setNoGravity(true);
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(1.0f, .95f, .95f), 2.0f);

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "water_bolt");
        FX fx = FXHelper.getFX(id);

        executor = new EntityEffectExecutor(fx, level(), this, EntityEffectExecutor.AutoRotate.NONE);
        executor.setForcedDeath(true);
        executor.setScale(0.8, 0.8, 0.8);

        try {
            executor.start();
        } catch (ConcurrentModificationException ignored) {

        }
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)
            return;

        ticks++;
        if(ticks > 20 * 8) {
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
            return;
        }

        blocksPerTick = lastPos != null ? lastPos.distanceTo(position()) : null;
        lastPos = position();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity target) || result.getEntity() == owner)
            return;

        // check if the owner exists before - to not crash
        if (this.getOwner() instanceof LivingEntity livingOwner) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, livingOwner), (float) damage);
        } else {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC), (float) damage);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if(level.isClientSide && executor != null) {
        }
        this.discard();
        if(griefing && level instanceof ServerLevel serverLevel) {
            List<BlockPos> waterBlocks = AbilityUtil.getBlocksInSphereRadius(serverLevel, result.getBlockPos().getCenter(), 2, true);
            for(BlockPos pos : waterBlocks) {
                if (level.getBlockState(pos).isAir() || level.getBlockState(pos).getBlock() == Blocks.WATER)
                    level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            }
        }
    }



    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIZE, 1.0f);
    }

    public void setSize(float size) {
        this.entityData.set(SIZE, size);
    }

    public float getSize() {
        return this.entityData.get(SIZE);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.FOOL_Card.get());
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Size", this.getSize());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSize(compound.getFloat("Size"));
    }
}
