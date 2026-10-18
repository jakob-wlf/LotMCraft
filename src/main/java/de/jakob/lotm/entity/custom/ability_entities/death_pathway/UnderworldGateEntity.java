package de.jakob.lotm.entity.custom.ability_entities.death_pathway;

import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

public class UnderworldGateEntity extends Entity {

    public enum Mode {
        SPIRITS, TENTACLES, GATE;

        public static Mode byId(int id) {
            Mode[] values = values();
            return values[Mth.clamp(id, 0, values.length - 1)];
        }
    }

    private static final int ABILITY_DURATION_TICKS = 20 * 20;
    private static final int GATE_DURATION_TICKS = 20 * 60 * 2;

    private static final int SPAWN_INTERVAL = 20 * 4;
    private static final int MOB_LIFETIME_TICKS = 20 * 60;

    private static final int DAMAGE_DELAY = 10;
    private static final int DAMAGE_INTERVAL = 10;
    private static final double DAMAGE_RADIUS = 6.5;

    private static final int TELEPORT_TICKS = 20 * 2;
    private static final double GATE_HALF_WIDTH = 1.0;
    private static final double GATE_HEIGHT = 2.5;

    private static final int UNDERWORLD_MIN_Y = 80;
    private static final int UNDERWORLD_MAX_Y = 120;
    private static final int CARVE_RADIUS = 1;
    private static final int CARVE_HEIGHT = 3;
    private static final Block PLATFORM_BLOCK = Blocks.DEEPSLATE;

    private static final DustParticleOptions DARK_DUST =
            new DustParticleOptions(new Vector3f(0.05f, 0.0f, 0.15f), 2.0f);

    private static final Map<UUID, UnderworldGateEntity> OPEN_GATES = new HashMap<>();
    private static final Map<UUID, List<Mob>> SUMMONS = new HashMap<>();


    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(UnderworldGateEntity.class, EntityDataSerializers.INT);

    public AnimationState openAnimationState = new AnimationState();
    public AnimationState tentacleAnimationState = new AnimationState();

    private UUID ownerUUID;
    private double logicalAge = 0;
    private double nextActionAt = 0;

    private ToDoubleFunction<LivingEntity> damageFunction;

    private final Map<UUID, Integer> gateProgress = new HashMap<>();

    public UnderworldGateEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public UnderworldGateEntity(Level level, Mode mode) {
        super(ModEntities.UNDERWORLD_GATE.get(), level);
        setMode(mode);
    }

    public static UnderworldGateEntity open(ServerLevel level, LivingEntity owner, Mode mode, Vec3 position, Vec3 lookAt) {
        UnderworldGateEntity gate = new UnderworldGateEntity(level, mode);
        gate.ownerUUID = owner.getUUID();
        gate.setPos(position.x, position.y - .6, position.z);
        gate.setYRot(yawFromVector(lookAt.subtract(position).normalize()));
        gate.setXRot(0);
        level.addFreshEntity(gate);

        OPEN_GATES.put(owner.getUUID(), gate);
        return gate;
    }

    public void setDamageFunction(ToDoubleFunction<LivingEntity> damageFunction) {
        this.damageFunction = damageFunction;
    }

    public static boolean closeOpenGate(UUID ownerId) {
        UnderworldGateEntity gate = OPEN_GATES.remove(ownerId);
        if (gate == null) return false;
        gate.discard();
        return true;
    }

    public static boolean release(UUID ownerId) {
        boolean closed = closeOpenGate(ownerId);
        List<Mob> mobs = SUMMONS.remove(ownerId);

        boolean hadMobs = mobs != null && !mobs.isEmpty();
        if (mobs != null) {
            for (Mob mob : mobs) {
                if (mob.isAlive()) mob.discard();
            }
        }
        return closed || hadMobs;
    }

    private static float yawFromVector(Vec3 dir) {
        if (dir.lengthSqr() < 1.0E-6) return 0.0F;
        return (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            if (this.tickCount == 1) {
                openAnimationState.start(0);
            }
            if (this.tickCount >= 22) {
                tentacleAnimationState.startIfStopped(0);
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level();
        Mode mode = getMode();

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL, position().add(getLookAngle().normalize().scale(.85)), 12, .8, 0.25, .8, 0);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.ENCHANT, getEyePosition().subtract(0, .2, 0), 8, 1.5, 1.5, 1.5, 0);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, getEyePosition().subtract(0, .2, 0), 8, 1.5, 1.5, 1.5, 0);

        switch (mode) {
            case SPIRITS -> tickSpirits(serverLevel);
            case TENTACLES -> tickTentacles(serverLevel);
            case GATE -> tickGate(serverLevel);
        }
    }

    private void tickSpirits(ServerLevel level) {
        ServerPlayer owner = ownerUUID == null ? null : level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner == null) {
            discard();
            return;
        }

        logicalAge += AbilityUtil.getTimeInArea(owner, new Location(position(), level));

        if (logicalAge >= nextActionAt) {
            nextActionAt += SPAWN_INTERVAL;
            spawnWave(level, owner);
        }

        if (logicalAge >= ABILITY_DURATION_TICKS) {
            discard();
        }
    }

    private void spawnWave(ServerLevel level, ServerPlayer owner) {
        spawnMob(level, owner, pickUndead(level));
        spawnMob(level, owner, pickSpirit(level, BeyonderData.getSequence(owner)));
    }

    private void spawnMob(ServerLevel level, ServerPlayer owner, Mob mob) {
        if (mob == null) return;

        double angle = random.nextDouble() * Math.PI * 2;
        double r = random.nextDouble() * 1.25;
        Vec3 spawnPos = position().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);

        BlockPos blockPos = BlockPos.containing(spawnPos);
        while (blockPos.getY() > level.getMinBuildHeight() && level.getBlockState(blockPos).isAir()) {
            blockPos = blockPos.below();
        }
        Vec3 groundPos = Vec3.atBottomCenterOf(blockPos.above());

        mob.setPos(groundPos.x, groundPos.y, groundPos.z);
        level.addFreshEntity(mob);

        SubordinateUtils.turnEntityIntoSubordinate(mob, owner, false);

        SUMMONS.computeIfAbsent(owner.getUUID(), k -> new ArrayList<>()).add(mob);

        ParticleUtil.spawnSphereParticles(level, DARK_DUST, groundPos.add(0, 1, 0), 0.8, 20);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                groundPos.x, groundPos.y + 0.5, groundPos.z, 8, 0.3, 0.4, 0.3, 0.05);

        ServerScheduler.scheduleDelayed(MOB_LIFETIME_TICKS, () -> {
            if (mob.isAlive()) {
                mob.discard();
            }
        }, level);
    }

    private Mob pickUndead(ServerLevel level) {
        return switch (random.nextInt(6)) {
            case 0 -> new Zombie(EntityType.ZOMBIE, level);
            case 1 -> new Skeleton(EntityType.SKELETON, level);
            case 2 -> new Husk(EntityType.HUSK, level);
            case 3 -> new Drowned(EntityType.DROWNED, level);
            case 4 -> new Stray(EntityType.STRAY, level);
            default -> new WitherSkeleton(EntityType.WITHER_SKELETON, level);
        };
    }

    private Mob pickSpirit(ServerLevel level, int ownerSequence) {
        if(ownerSequence <= 4) {
            if(random.nextInt(12) == 0) {
                return random.nextBoolean() ?
                        ModEntities.GRIM_REAPER.get().create(level) :
                        ModEntities.HEADLESS_BRIDE.get().create(level);
            }
        }

        return switch (random.nextInt(7)) {
            case 0 -> ModEntities.SPIRIT_GHOST.get().create(level);
            case 1 -> ModEntities.SPIRIT_DERVISH_ENTITY.get().create(level);
            case 2 -> ModEntities.SPIRIT_BUBBLES_ENTITY.get().create(level);
            case 3 -> ModEntities.SPIRIT_BLUE_WIZARD.get().create(level);
            case 4 -> ModEntities.SPIRIT_MALMOUTH.get().create(level);
            case 5 -> ModEntities.ABSCESSED_HAND.get().create(level);
            default -> ModEntities.SPIRIT_TRANSLUCENT_WIZARD.get().create(level);
        };
    }

    private void tickTentacles(ServerLevel level) {
        Entity ownerEntity = ownerUUID == null ? null : level.getEntity(ownerUUID);
        if (!(ownerEntity instanceof LivingEntity owner) || !owner.isAlive() || damageFunction == null) {
            discard();
            return;
        }

        logicalAge += 1;

        if (logicalAge >= DAMAGE_DELAY + nextActionAt) {
            nextActionAt += DAMAGE_INTERVAL;
            AbilityUtil.damageNearbyEntities(level,
                    owner,
                    DAMAGE_RADIUS,
                    damageFunction.applyAsDouble(owner),
                    position().add(getLookAngle().normalize().scale(2)),
                    true,
                    false
            );
        }

        if (logicalAge >= ABILITY_DURATION_TICKS) {
            discard();
        }
    }

    private AABB getTeleportZone() {
        Vec3 center = position().add(getLookAngle().normalize().scale(.85));
        return new AABB(
                center.x - GATE_HALF_WIDTH, getY() - .5, center.z - GATE_HALF_WIDTH,
                center.x + GATE_HALF_WIDTH, getY() + GATE_HEIGHT, center.z + GATE_HALF_WIDTH
        );
    }

    private void tickGate(ServerLevel level) {
        logicalAge += 1;
        if (logicalAge >= GATE_DURATION_TICKS) {
            discard();
            return;
        }

        Set<UUID> present = new HashSet<>();

        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getTeleportZone(), p -> p.isAlive() && !p.isSpectator())) {
            present.add(player.getUUID());
            int progress = gateProgress.merge(player.getUUID(), 1, Integer::sum);

            spawnChargeParticles(level, player, progress);

            if (progress >= TELEPORT_TICKS) {
                gateProgress.remove(player.getUUID());
                travelThroughGate(level, player);
            }
        }

        gateProgress.keySet().retainAll(present);
    }

    private void spawnChargeParticles(ServerLevel level, ServerPlayer player, int progress) {
        float fraction = progress / (float) TELEPORT_TICKS;

        double radius = 1.4 - 0.9 * fraction;
        double baseAngle = progress * 0.5;
        for (int i = 0; i < 3; i++) {
            double angle = baseAngle + i * (Math.PI * 2 / 3);
            double y = player.getY() + (progress * 0.05 % 2.0);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX() + Math.cos(angle) * radius, y, player.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0.01);
        }

        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                3 + (int) (fraction * 12), 0.5, 0.8, 0.5, 0.05);

        ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, player.position().add(0, 1, 0), 1 + (int) (fraction * 6), .4, .8, .4, 0.02);
    }

    private void travelThroughGate(ServerLevel level, ServerPlayer player) {
        boolean inUnderworld = level.dimension() == ModDimensions.UNDERWORLD_DIMENSION;
        ServerLevel target = level.getServer().getLevel(inUnderworld ? Level.OVERWORLD : ModDimensions.UNDERWORLD_DIMENSION);
        if (target == null) return;

        int x = Mth.floor(player.getX());
        int z = Mth.floor(player.getZ());

        int minY = inUnderworld ? target.getMinBuildHeight() + 1 : UNDERWORLD_MIN_Y;
        int maxY = inUnderworld ? target.getMaxBuildHeight() - 1 - CARVE_HEIGHT : UNDERWORLD_MAX_Y;

        BlockPos dest = findSafeSpot(target, x, z, minY, maxY, Mth.floor(player.getY()));

        level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.6f, 1.2f);
        player.teleportTo(target, dest.getX() + .5, dest.getY(), dest.getZ() + .5, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        target.playSound(null, dest, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    private static BlockPos findSafeSpot(ServerLevel level, int x, int z, int minY, int maxY, int preferredY) {
        int pref = Mth.clamp(preferredY, minY, maxY);
        int range = Math.max(pref - minY, maxY - pref);
        boolean anyNonSolid = false;

        for (int offset = 0; offset <= range; offset++) {
            for (int side = 0; side < 2; side++) {
                if (offset == 0 && side == 1) continue;

                int y = side == 0 ? pref + offset : pref - offset;
                if (y < minY || y > maxY) continue;

                BlockPos feet = new BlockPos(x, y, z);
                if (!isFree(level, feet)) {
                    BlockState state = level.getBlockState(feet);
                    if (!state.getFluidState().isEmpty() || state.getCollisionShape(level, feet).isEmpty()) {
                        anyNonSolid = true;
                    }
                    continue;
                }
                anyNonSolid = true;

                BlockPos floor = feet.below();
                BlockState floorState = level.getBlockState(floor);
                if (isFree(level, feet.above())
                        && floorState.getFluidState().isEmpty()
                        && floorState.isFaceSturdy(level, floor, Direction.UP)) {
                    return feet;
                }
            }
        }

        BlockPos feet = new BlockPos(x, pref, z);

        if (!anyNonSolid) {
            for (int dx = -CARVE_RADIUS; dx <= CARVE_RADIUS; dx++) {
                for (int dz = -CARVE_RADIUS; dz <= CARVE_RADIUS; dz++) {
                    for (int dy = 0; dy < CARVE_HEIGHT; dy++) {
                        level.setBlockAndUpdate(feet.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        } else {
            level.setBlockAndUpdate(feet.below(), PLATFORM_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(feet, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(feet.above(), Blocks.AIR.defaultBlockState());
        }
        return feet;
    }

    private static boolean isFree(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && ownerUUID != null && OPEN_GATES.get(ownerUUID) == this) {
            OPEN_GATES.remove(ownerUUID);
        }
        super.remove(reason);
    }

    public void setMode(Mode mode) {
        entityData.set(MODE, mode.ordinal());
    }

    public Mode getMode() {
        return Mode.byId(entityData.get(MODE));
    }

    public boolean hasTentacles() {
        return getMode() == Mode.TENTACLES;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MODE, Mode.SPIRITS.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setMode(Mode.byId(tag.getInt("Mode")));
        logicalAge = tag.getDouble("LogicalAge");
        nextActionAt = tag.getDouble("NextActionAt");
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Mode", getMode().ordinal());
        tag.putDouble("LogicalAge", logicalAge);
        tag.putDouble("NextActionAt", nextActionAt);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }
}