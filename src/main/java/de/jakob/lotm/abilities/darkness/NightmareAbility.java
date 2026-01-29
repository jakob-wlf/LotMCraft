package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncNightmareAbilityPacket;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.data.NightmareCenter;
import de.jakob.lotm.util.helper.*;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NightmareAbility extends SelectableAbility {
    private static final HashMap<UUID, NightmareCenter> activeNightmaresServer = new HashMap<>();
    private static final HashMap<UUID, NightmareCenter> activeNightmaresClient = new HashMap<>();
    private static final HashSet<UUID> isReshaping = new HashSet<>();
    private static final HashMap<UUID, RegionSnapshot> storedRegions = new HashMap<>();

    public NightmareAbility(String id) {
        super(id, .15f);

        canBeCopied = false;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.nightmare.nightmare", "ability.lotmcraft.nightmare.reshape", "ability.lotmcraft.nightmare.restrict", "ability.lotmcraft.nightmare.attack", "ability.lotmcraft.nightmare.teleport"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch(abilityIndex) {
            case 0 -> nightmare(level, entity);
            case 1 -> reshape(level, entity);
            case 2 -> restrict(level, entity);
            case 3 -> attack(level, entity);
            case 4 -> teleport(level, entity);
        }
    }

    private void teleport(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!activeNightmaresServer.containsKey(entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You need to create a Nightmare first.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        Vec3 targetLoc = getTargetBlock(entity, 8).getCenter().add(0, 1, 0);
        level.playSound(null, targetLoc.x, targetLoc.y, targetLoc.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .5f, 1);

        entity.teleportTo(targetLoc.x, targetLoc.y, targetLoc.z);
        ParticleUtil.spawnParticles((ServerLevel) level, dustSmall, targetLoc.add(0, .5, 0), 30, .4, 1, .4, 0);
    }

    private void attack(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!activeNightmaresServer.containsKey(entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You need to create a Nightmare first.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        Vec3 targetLoc = AbilityUtil.getTargetLocation(entity, 20, 2);
        if(!isBlockInRadius(targetLoc, entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You cant attack outside the nightmare.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        Vec3 startLoc = VectorUtil.getRelativePosition(targetLoc, entity.getLookAngle().normalize(), 0, random.nextDouble(-3, 3), random.nextDouble(1, 3));
        Vec3 dir = targetLoc.subtract(startLoc).normalize().scale(.6);

        AtomicReference<Vec3> currentPos = new AtomicReference<>(startLoc);

        AtomicBoolean hasHit = new AtomicBoolean(false);

        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            Vec3 pos = currentPos.get();

            if(AbilityUtil.damageNearbyEntities((ServerLevel) level, entity, 1.2f, DamageLookup.lookupDamage(7, 1) * multiplier(entity), pos, true, false, true, 0)) {
                hasHit.set(true);
                return;
            }
            BlockState state = level.getBlockState(BlockPos.containing(pos));
            if(!state.getCollisionShape(level, BlockPos.containing(pos)).isEmpty() || state.getBlock() == Blocks.VOID_AIR) {
                hasHit.set(true);
                return;
            }
            ParticleUtil.spawnParticles((ServerLevel) level, dustVerySmall, pos, 20, .25, 0);

            currentPos.set(pos.add(dir));
        }, null, hasHit);
    }

    private void restrict(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(!activeNightmaresServer.containsKey(entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You need to create a Nightmare first.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 20, 2);
        if(targetEntity == null || !isAffectedByNightmare(targetEntity)) {
            Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 15, 1.5f, true);
            ParticleUtil.createParticleSpirals((ServerLevel) level, dustSmall, targetPos, 2, 2, 2.5, .5, 8, 20 * 5, 11, 8);
            return;
        }

        Location loc = new Location(targetEntity.position(), level);
        ParticleUtil.createParticleSpirals(dustVerySmall, loc, 1.2, 1.2, 2.5, .5, 8, 20 * 20, 11, 8);

        ServerScheduler.scheduleForDuration(0, 2, 20 * 20, () -> {
            if(entity.level().isClientSide)
                return;
            loc.setPosition(targetEntity.position());
            loc.setLevel(entity.level());
            Vec3 startPos = targetEntity.getEyePosition().subtract(0, .15, 0);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(2.75, -3, 0), .25, 1);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(-2.75, -3, 0), .25, 1);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(0, -3, -2.75), .25, 1);
            ParticleUtil.drawParticleLine((ServerLevel) level, dustSmall, startPos, startPos.add(0, -3, 2.75), .25, 1);
            targetEntity.setDeltaMovement(0, 0, 0);
            targetEntity.hurtMarked = true;
            targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 10, false, false, false));
        }, (ServerLevel) level);
    }

    private void reshape(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;

        if(isReshaping.contains(entity.getUUID())) {
            isReshaping.remove(entity.getUUID());
            return;
        }

        if(!activeNightmaresServer.containsKey(entity.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You need to create a Nightmare first.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        BlockPos targetLoc = AbilityUtil.getTargetBlock(entity, 35, false);
        BlockState state = level.getBlockState(targetLoc);
        if(!isBlockInRadius(targetLoc.getCenter(), entity.getUUID()) || state.getCollisionShape(level, targetLoc).isEmpty()) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("Select a solid block inside the nightmare.").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        isReshaping.add(entity.getUUID());

        double radius = entity.getEyePosition().distanceTo(targetLoc.getCenter());
        Block block = state.getBlock();

        AtomicBoolean shouldStop = new AtomicBoolean(false);

        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            if(!activeNightmaresServer.containsKey(entity.getUUID())) {
                shouldStop.set(true);
                isReshaping.remove(entity.getUUID());
                return;
            }

            if(!isReshaping.contains(entity.getUUID())) {
                shouldStop.set(true);
                return;
            }

            BlockPos currentTarget = AbilityUtil.getTargetBlock(entity, radius - 2, radius, false);

            if(currentTarget.getCenter().distanceTo(entity.position()) < 5)
                return;

            createBlockSphere(block, (ServerLevel) level, currentTarget.getCenter(), entity.getUUID());
        }, 2, null, shouldStop);

    }

    public static void stopNightmare(UUID uuid) {
        activeNightmaresServer.remove(uuid);
    }

    public BlockPos getTargetBlock(LivingEntity entity, double radius) {
        Vec3 lookDirection = entity.getLookAngle().normalize();
        Vec3 playerPosition = entity.position().add(0, entity.getEyeHeight(), 0);

        Vec3 targetPosition = playerPosition;

        for(int i = 0; i < radius; i++) {
            targetPosition = playerPosition.add(lookDirection.scale(i));

            BlockState block = entity.level().getBlockState(BlockPos.containing(targetPosition));

            if (!block.getCollisionShape(entity.level(), BlockPos.containing(targetPosition)).isEmpty()) {
                targetPosition = playerPosition.add(lookDirection.scale(i - 1));
            }
        }

        return BlockPos.containing(targetPosition);
    }

    private final DustParticleOptions dustBig = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 10f);
    private final DustParticleOptions dustSmall = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), 1.2f);
    private final DustParticleOptions dustVerySmall = new DustParticleOptions(new Vector3f(250 / 255f, 40 / 255f, 64 / 255f), .7f);

    private void nightmare(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        if(activeNightmaresServer.containsKey(entity.getUUID())) {
            activeNightmaresServer.remove(entity.getUUID());
            return;
        }

        int radius = 40;
        NightmareCenter center = new NightmareCenter(level, entity.position(), radius * radius);

        for(NightmareCenter c : activeNightmaresServer.values()) {
            double maxRadius = Math.max(center.radiusSquared(), c.radiusSquared());
            if(c.pos().distanceToSqr(center.pos()) <= maxRadius) {
                if(entity instanceof ServerPlayer player) {
                    ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("You are already near another Nightmare.").withColor(0xFFff124d));
                    player.connection.send(packet);
                }
                return;
            }
        }

        activeNightmaresServer.put(entity.getUUID(), center);
        PacketHandler.sendToAllPlayers(new SyncNightmareAbilityPacket(entity.position().x, entity.position().y, entity.position().z, radius, true));

        // Create an empty snapshot - it will capture blocks as they're modified
        RegionSnapshot region = new RegionSnapshot(level, BlockPos.containing(center.pos()), radius);
        storedRegions.put(entity.getUUID(), region);

        AtomicBoolean shouldStop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil((ServerLevel) level, () -> {
            if(!activeNightmaresServer.containsKey(entity.getUUID())) {
                shouldStop.set(true);
                stopNightmare((ServerLevel) level, entity.getUUID(), radius, center);
                return;
            }

            if(entity.level() != center.level()) {
                shouldStop.set(true);
                stopNightmare((ServerLevel) level, entity.getUUID(), radius, center);
                return;
            }

            if(entity.position().distanceToSqr(center.pos()) > (radius * radius)) {
                shouldStop.set(true);
                stopNightmare((ServerLevel) level, entity.getUUID(), radius, center);
                return;
            }

            ParticleUtil.spawnParticles((ServerLevel) level, dustBig, center.pos(), 50, radius, 0);
        }, 5, null, shouldStop);
    }

    private void stopNightmare(ServerLevel level, UUID uuid, int radius, NightmareCenter center) {
        level.playSound(null, center.pos().x, center.pos().y, center.pos().z, Blocks.GLASS.getSoundType(Blocks.ICE.defaultBlockState(), level, BlockPos.containing(center.pos().x, center.pos().y, center.pos().z), null).getBreakSound(), SoundSource.BLOCKS, 10.0f, 1.0f);
        ParticleUtil.spawnParticles(level, dustBig, center.pos(), 8000, radius, 20, radius, 0);

        if(storedRegions.containsKey(uuid))
            storedRegions.get(uuid).restore(level);
        storedRegions.remove(uuid);
        activeNightmaresServer.remove(uuid);

        PacketHandler.sendToAllPlayers(new SyncNightmareAbilityPacket(0, 0, 0, 0, false));
    }

    public static boolean hasActiveNightmare(LivingEntity entity) {
        if(FMLEnvironment.dist == Dist.CLIENT)
            return activeNightmaresClient.containsKey(entity.getUUID());
        return activeNightmaresServer.containsKey(entity.getUUID());
    }

    public static boolean isAffectedByNightmare(LivingEntity entity) {
        if(FMLEnvironment.dist == Dist.CLIENT) {
            for(Map.Entry<UUID, NightmareCenter> entry : activeNightmaresClient.entrySet()) {
                if(entity.getUUID() == entry.getKey())
                    continue;

                if(entity.position().distanceToSqr(entry.getValue().pos()) <= entry.getValue().radiusSquared())
                    return true;
            }

        }
        else {
            for(Map.Entry<UUID, NightmareCenter> entry : activeNightmaresServer.entrySet()) {
                if(entity.getUUID() == entry.getKey())
                    continue;

                if(entity.position().distanceToSqr(entry.getValue().pos()) <= entry.getValue().radiusSquared())
                    return true;
            }

        }
        return false;
    }

    private boolean isBlockInRadius(Vec3 pos, UUID casterUUID) {
        if(!activeNightmaresServer.containsKey(casterUUID))
            return false;

        NightmareCenter center = activeNightmaresServer.get(casterUUID);
        return pos.distanceToSqr(center.pos()) <= center.radiusSquared();
    }

    private void createBlockSphere(Block block, ServerLevel level, Vec3 center, UUID casterUuid) {
        int radius = 3;
        BlockPos centerPos = BlockPos.containing(center);

        // Capture the sphere BEFORE modifying it so we can restore it later
        if (storedRegions.containsKey(casterUuid)) {
            storedRegions.get(casterUuid).captureSphere(centerPos, radius);
        }

        // Iterate through all positions in a cube around the center
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Calculate distance from center
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    // Only place blocks within the sphere radius
                    if (distance <= radius) {
                        BlockPos pos = centerPos.offset(x, y, z);
                        BlockState currentState = level.getBlockState(pos);

                        // Only replace passable blocks (air, water, lava, etc.)
                        if (currentState.getCollisionShape(level, pos).isEmpty() && isBlockInRadius(pos.getCenter(), casterUuid)) {
                            level.setBlock(pos, block.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncToClient(UUID uuid, double x, double y, double z, double radius, boolean active) {
        if(FMLEnvironment.dist != Dist.CLIENT)
            return;

        if(!active) {
            activeNightmaresClient.remove(uuid);
            return;
        }

        NightmareCenter center = new NightmareCenter(Minecraft.getInstance().level, new Vec3(x, y, z), radius * radius);
        activeNightmaresClient.put(uuid, center);
    }

}