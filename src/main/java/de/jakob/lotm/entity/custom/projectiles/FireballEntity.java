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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FireballEntity extends AbstractArrow {

    private final Level level;
    private final LivingEntity owner;
    private final double damage;
    private final boolean griefing;

    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.FLOAT);

    Vec3 lastPos = null;

    private int ticks = 0;
    private int petrifiedTicks = 0;

    public FireballEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.level = level;
        this.owner = null;
        this.damage = 0;
        this.griefing = false;
        this.setSize(1.0f);
        init();
    }

    public FireballEntity(Level level, LivingEntity owner, double damage, boolean griefing) {
        super(ModEntities.FIREBALL.get(), level);
        this.level = level;
        this.owner = owner;
        this.damage = damage;
        this.griefing = griefing;
        this.setSize(1.0f);
        init();
    }

    public FireballEntity(Level level, LivingEntity owner, double damage, boolean griefing, float size) {
        super(ModEntities.FIREBALL.get(), level);
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
        if(ticks > 20 * 8) {
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), BlockPos.containing(this.position()), false));
            return;
        }

        if(lastPos != null) {
            float size = getSize();
            if(size < 1.5)
                ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.SMOKE, lastPos, Math.round(4 * size), .2 * size, .02);
            else
                ParticleUtil.spawnParticles((ServerLevel) level, dust, lastPos, Math.round(3 * size), .2 * size, .02);

            BlockPos blockPos = BlockPos.containing(lastPos);

            double offsetX = lastPos.x - (blockPos.getX() + 0.5);
            double offsetY = lastPos.y - (blockPos.getY() + 0.5);
            double offsetZ = lastPos.z - (blockPos.getZ() + 0.5);

            PacketHandler.sendToNearbyPlayers(
                    new PlayPhotonBlockEffectPacket("flame_particle", blockPos, offsetX, offsetY, offsetZ, 1.5, null, -1, false, true),
                    (ServerLevel) level, lastPos, 128
            );

            double velocity = this.getDeltaMovement().length();
            double blocksPerTick = velocity * 20;
            Vec3 halfwayPos = lastPos.add(this.getDeltaMovement().normalize().scale(blocksPerTick * -0.5));

            blockPos = BlockPos.containing(halfwayPos);

            offsetX = lastPos.x - (blockPos.getX() + 0.5);
            offsetY = lastPos.y - (blockPos.getY() + 0.5);
            offsetZ = lastPos.z - (blockPos.getZ() + 0.5);

            PacketHandler.sendToNearbyPlayers(
                    new PlayPhotonBlockEffectPacket("flame_particle", blockPos, offsetX, offsetY, offsetZ, 1.5, null, -1, false, true),
                    (ServerLevel) level, lastPos, 128
            );
        }

        lastPos = position();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.discard();
        if(!(result.getEntity() instanceof LivingEntity target) || result.getEntity() == owner)
            return;

        level.explode(owner, target.position().x, target.position().y, target.position().z, 3.5f, griefing, Level.ExplosionInteraction.NONE);
        // check if the owner exists before - to not crash
        if (this.getOwner() instanceof LivingEntity livingOwner) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, livingOwner), (float) damage);
        } else {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC), (float) damage);
        }
        target.setRemainingFireTicks(target.getRemainingFireTicks() + 20 * 6);
        if(!level.isClientSide)
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, position(), owner, null, new String[]{"explosion", "burning"}, 3, 10));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
        if(griefing) {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 4f, true, Level.ExplosionInteraction.MOB);
        }
        else {
            level.explode(owner, result.getLocation().x, result.getLocation().y, result.getLocation().z, 4f, false, Level.ExplosionInteraction.NONE);
        }
        if(!level.isClientSide)
            NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, position(), owner, null, new String[]{"explosion", "burning"}, 3, 10));
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
