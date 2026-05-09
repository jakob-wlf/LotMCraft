package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.SpiritWorldHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FoolingRealmOfMysteriesAbility extends Ability {

    private static final Map<UUID, Vec3> REALM_CAPTIVES = new HashMap<>();

    private static final int REALM_TICKS = 600;
    private static final int CAPTURE_RADIUS = 24;

    private static final DustParticleOptions CURTAIN_OUTER =
            new DustParticleOptions(new Vector3f(0.42f, 0.42f, 0.48f), 3.0f);
    private static final DustParticleOptions CURTAIN_MID =
            new DustParticleOptions(new Vector3f(0.62f, 0.62f, 0.68f), 2.0f);
    private static final DustParticleOptions CURTAIN_INNER =
            new DustParticleOptions(new Vector3f(0.75f, 0.75f, 0.82f), 1.5f);
    private static final DustParticleOptions VOID_FLOOR =
            new DustParticleOptions(new Vector3f(0.18f, 0.18f, 0.22f), 1.5f);
    private static final DustParticleOptions SILVER_SHIMMER =
            new DustParticleOptions(new Vector3f(0.82f, 0.84f, 0.95f), 1.2f);

    public FoolingRealmOfMysteriesAbility() {
        super("fooling_realm", 120);
        this.canBeCopied = false;
        this.canBeReplicated = false;
        this.canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("fool", 0);
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof Player player)) return;

        int color = BeyonderData.pathwayInfos.get("fool").color();
        ServerLevel spiritWorldLevel = serverLevel.getServer().getLevel(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY);
        if (spiritWorldLevel == null) {
            player.displayClientMessage(
                    Component.translatable("lotmcraft.ability.fooling_realm.no_dimension").withColor(color), true);
            return;
        }

        Vec3 origin = player.position();
        Vec3 dest = SpiritWorldHandler.getCoordinatesInSpiritWorld(origin, serverLevel);
        int terrainY = spiritWorldLevel.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BlockPos.containing(dest.x, dest.y, dest.z)).getY();
        int floorY = Math.max(118, terrainY + 18);

        double destX = dest.x;
        double destZ = dest.z;
        double destY = floorY + 1.0;

        AABB area = new AABB(origin, origin).inflate(CAPTURE_RADIUS);
        List<LivingEntity> targets = new ArrayList<>(serverLevel.getEntitiesOfClass(LivingEntity.class, area));

        Map<UUID, Vec3> capturedPositions = new HashMap<>();
        for (LivingEntity target : targets) {
            capturedPositions.put(target.getUUID(), target.position());
        }
        if (!capturedPositions.containsKey(player.getUUID())) {
            capturedPositions.put(player.getUUID(), origin);
        }
        REALM_CAPTIVES.putAll(capturedPositions);

        for (LivingEntity target : targets) {
            Vec3 tp = target.position();
            for (double hy = 0; hy < 18.0; hy += 0.45) {
                serverLevel.sendParticles(CURTAIN_OUTER, tp.x, tp.y + hy, tp.z, 1, 0.45, 0, 0.45, 0);
                serverLevel.sendParticles(CURTAIN_INNER, tp.x, tp.y + hy + 0.2, tp.z, 1, 0.35, 0, 0.35, 0);
                serverLevel.sendParticles(SILVER_SHIMMER, tp.x, tp.y + hy + 0.1, tp.z, 1, 0.25, 0, 0.25, 0);
            }
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, tp.x, tp.y + 1.5, tp.z, 16, 0.5, 1.0, 0.5, 0.02);
            serverLevel.sendParticles(ParticleTypes.FLASH, tp.x, tp.y + 1.5, tp.z, 1, 0, 0, 0, 0);
            if (target instanceof Player otherPlayer) {
                otherPlayer.displayClientMessage(Component.translatable("lotmcraft.ability.fooling_realm.activated").withColor(color), true);
            }
        }

        createPlatform(serverLevel, spiritWorldLevel, destX, floorY, destZ);
        serverLevel.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT, 3.0f, 0.45f);

        ServerScheduler.scheduleDelayed(5, () -> {
            for (LivingEntity target : targets) {
                double jx = (serverLevel.random.nextDouble() - 0.5) * 5.5;
                double jz = (serverLevel.random.nextDouble() - 0.5) * 5.5;
                target.teleportTo(spiritWorldLevel, destX + jx, destY, destZ + jz, Set.of(), target.getYRot(), target.getXRot());
            }
            boolean casterIn = targets.stream().anyMatch(target -> target.getUUID().equals(player.getUUID()));
            if (!casterIn) {
                player.teleportTo(spiritWorldLevel, destX, destY, destZ, Set.of(), player.getYRot(), player.getXRot());
            }

            spiritWorldLevel.playSound(null, destX, destY, destZ,
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.AMBIENT, 3.5f, 0.4f);
            FoolingAbility.activateRealm(player, spiritWorldLevel, new Vec3(destX, destY, destZ), FoolingAbility.REALM_RADIUS, REALM_TICKS);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.fooling.realm_enabled").withColor(color));
        }, serverLevel);

        AtomicInteger curtainTick = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(6, 2, REALM_TICKS,
                () -> spawnCurtainEffect(spiritWorldLevel, destX, destY, destZ, curtainTick.getAndIncrement()),
                () -> {
                    for (int i = 0; i < 168; i++) {
                        double a = i * Math.PI * 2.0 / 168;
                        for (double py = destY; py < destY + 28; py += 1.0) {
                            spiritWorldLevel.sendParticles(CURTAIN_OUTER,
                                    destX + Math.cos(a) * 25, py, destZ + Math.sin(a) * 25,
                                    1, 0, 0, 0, 0);
                            spiritWorldLevel.sendParticles(CURTAIN_INNER,
                                    destX + Math.cos(a) * 21, py + 0.3, destZ + Math.sin(a) * 21,
                                    1, 0, 0, 0, 0);
                        }
                    }
                    spiritWorldLevel.sendParticles(ParticleTypes.FLASH, destX, destY + 3, destZ, 1, 0, 0, 0, 0);
                    spiritWorldLevel.playSound(null, destX, destY, destZ,
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT, 2.5f, 0.6f);

                    for (Map.Entry<UUID, Vec3> entry : capturedPositions.entrySet()) {
                        Entity found = spiritWorldLevel.getPlayerByUUID(entry.getKey());
                        if (found == null) {
                            AABB search = new AABB(destX - 70, destY - 35, destZ - 70, destX + 70, destY + 35, destZ + 70);
                            for (LivingEntity spiritEntity : spiritWorldLevel.getEntitiesOfClass(LivingEntity.class, search)) {
                                if (spiritEntity.getUUID().equals(entry.getKey())) {
                                    found = spiritEntity;
                                    break;
                                }
                            }
                        }
                        if (found != null) {
                            Vec3 ret = entry.getValue();
                            found.teleportTo(serverLevel, ret.x, ret.y, ret.z, Set.of(), found.getYRot(), found.getXRot());
                            if (found instanceof Player foundPlayer) {
                                foundPlayer.displayClientMessage(Component.translatable("lotmcraft.ability.fooling_realm.deactivated").withColor(color), true);
                            }
                        }
                    }

                    REALM_CAPTIVES.keySet().removeAll(capturedPositions.keySet());
                    FoolingAbility.deactivateRealm(player.getUUID());
                },
                spiritWorldLevel);
    }

    private static void createPlatform(ServerLevel overworld, ServerLevel spiritWorld, double cx, int floorY, double cz) {
        if (overworld.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for (int bx = -10; bx <= 10; bx++) {
                for (int bz = -10; bz <= 10; bz++) {
                    double dist = Math.sqrt(bx * bx + bz * bz);
                    if (dist > 10.25) {
                        continue;
                    }

                    BlockPos pos = BlockPos.containing(cx + bx, floorY, cz + bz);
                    BlockState state;
                    if (dist <= 2.5) {
                        state = Blocks.SEA_LANTERN.defaultBlockState();
                    } else if (dist <= 5.0) {
                        state = Blocks.AMETHYST_BLOCK.defaultBlockState();
                    } else if (dist <= 8.0) {
                        state = Blocks.CALCITE.defaultBlockState();
                    } else {
                        state = Blocks.END_STONE_BRICKS.defaultBlockState();
                    }
                    spiritWorld.setBlockAndUpdate(pos, state);

                    if (dist >= 8.6 && dist <= 9.4) {
                        spiritWorld.setBlockAndUpdate(pos.above(), Blocks.PURPUR_PILLAR.defaultBlockState());
                    }
                }
            }

            int[][] spires = {{8, 0}, {-8, 0}, {0, 8}, {0, -8}, {6, 6}, {-6, 6}, {6, -6}, {-6, -6}};
            for (int[] spire : spires) {
                BlockPos base = BlockPos.containing(cx + spire[0], floorY + 1, cz + spire[1]);
                for (int py = 0; py < 4; py++) {
                    spiritWorld.setBlockAndUpdate(base.above(py), Blocks.QUARTZ_PILLAR.defaultBlockState());
                }
                spiritWorld.setBlockAndUpdate(base.above(4), Blocks.SEA_LANTERN.defaultBlockState());
            }
        } else {
            DustParticleOptions stone = new DustParticleOptions(new Vector3f(0.45f, 0.45f, 0.45f), 2.0f);
            for (int bx = -10; bx <= 10; bx++) {
                for (int bz = -10; bz <= 10; bz++) {
                    if (Math.sqrt(bx * bx + bz * bz) > 10.25) {
                        continue;
                    }
                    spiritWorld.sendParticles(stone, cx + bx, floorY + 0.1, cz + bz, 4, 0.3, 0, 0.3, 0);
                    spiritWorld.sendParticles(SILVER_SHIMMER, cx + bx, floorY + 0.2, cz + bz, 2, 0.2, 0, 0.2, 0);
                }
            }
        }
    }

    private static void spawnCurtainEffect(ServerLevel level, double cx, double cy, double cz, int tick) {
        double wallR = 24.0 + Math.sin(tick * 0.08) * 1.4;
        double curtainH = 28.0;

        for (int i = 0; i < 168; i++) {
            double a = i * Math.PI * 2.0 / 168;
            double px = cx + Math.cos(a) * wallR;
            double pz = cz + Math.sin(a) * wallR;
            double yStep = (i % 4 == 0) ? 1.0 : 1.8;
            for (double py = cy; py < cy + curtainH; py += yStep) {
                level.sendParticles(CURTAIN_OUTER, px, py, pz, 1, 0, 0, 0, 0);
                level.sendParticles(SILVER_SHIMMER, px, py + 0.25, pz, 1, 0, 0, 0, 0);
            }
        }

        for (int i = 0; i < 128; i++) {
            double a = i * Math.PI * 2.0 / 128 + 0.03;
            double px = cx + Math.cos(a) * (wallR - 1.8);
            double pz = cz + Math.sin(a) * (wallR - 1.8);
            for (double py = cy + 1; py < cy + curtainH - 1; py += 2.0) {
                level.sendParticles(CURTAIN_MID, px, py, pz, 1, 0, 0, 0, 0);
                level.sendParticles(ParticleTypes.END_ROD, px, py + 0.2, pz, 1, 0, 0.02, 0, 0.01);
            }
        }

        for (int i = 0; i < 96; i++) {
            double a = i * Math.PI * 2.0 / 96;
            double px = cx + Math.cos(a) * (wallR - 4.0);
            double pz = cz + Math.sin(a) * (wallR - 4.0);
            for (double py = cy; py < cy + curtainH * 0.75; py += 2.4) {
                level.sendParticles(CURTAIN_INNER, px, py, pz, 1, 0, 0, 0, 0);
            }
        }

        double ceilY = cy + curtainH;
        for (double capR = 0; capR <= wallR; capR += 2.5) {
            int capPts = Math.max(12, (int) (capR * 8));
            for (int i = 0; i < capPts; i++) {
                double a = i * Math.PI * 2.0 / capPts;
                level.sendParticles(CURTAIN_OUTER,
                        cx + Math.cos(a) * capR, ceilY, cz + Math.sin(a) * capR,
                        1, 0, 0, 0, 0);
                if (capR > 4) {
                    level.sendParticles(SILVER_SHIMMER,
                            cx + Math.cos(a) * capR, ceilY + 0.5, cz + Math.sin(a) * capR,
                            1, 0, 0, 0, 0);
                }
            }
        }

        for (int i = 0; i < 48; i++) {
            double ox = (level.random.nextDouble() - 0.5) * (wallR * 1.8);
            double oz = (level.random.nextDouble() - 0.5) * (wallR * 1.8);
            if (ox * ox + oz * oz <= (wallR - 1) * (wallR - 1)) {
                level.sendParticles(VOID_FLOOR, cx + ox, cy - 0.1, cz + oz, 1, 0.4, 0, 0.4, 0);
                level.sendParticles(SILVER_SHIMMER, cx + ox, cy + 0.2, cz + oz, 1, 0.3, 0, 0.3, 0);
            }
        }

        for (int i = 0; i < 36; i++) {
            double a = i * Math.PI * 2.0 / 36 + tick * 0.10;
            double r = wallR * 0.68;
            level.sendParticles(ParticleTypes.ENCHANT,
                    cx + Math.cos(a) * r, cy + curtainH * 0.75, cz + Math.sin(a) * r,
                    3, 0.3, 0.8, 0.3, 0.06);
            level.sendParticles(SILVER_SHIMMER,
                    cx + Math.cos(a) * (r - 2.2), cy + curtainH * 0.62, cz + Math.sin(a) * (r - 2.2),
                    1, 0.2, 0.5, 0.2, 0.02);
        }

        for (int i = 0; i < 24; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double r = level.random.nextDouble() * (wallR - 2);
            level.sendParticles(ParticleTypes.SOUL,
                    cx + Math.cos(a) * r, cy + level.random.nextDouble() * curtainH * 0.5,
                    cz + Math.sin(a) * r, 1, 0, 0.12, 0, 0.02);
        }

        for (int i = 0; i < 36; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double r = wallR - 0.5 - level.random.nextDouble() * 1.5;
            double py = cy + level.random.nextDouble() * curtainH;
            level.sendParticles(ParticleTypes.WITCH,
                    cx + Math.cos(a) * r, py, cz + Math.sin(a) * r, 1, 0, 0.1, 0, 0);
            level.sendParticles(ParticleTypes.DRAGON_BREATH,
                    cx + Math.cos(a) * (r - 1.2), py, cz + Math.sin(a) * (r - 1.2), 1, 0.1, 0.1, 0.1, 0.01);
        }

        if (tick % 5 == 0) {
            for (int i = 0; i < 168; i++) {
                double a = i * Math.PI * 2.0 / 168;
                level.sendParticles(ParticleTypes.PORTAL,
                        cx + Math.cos(a) * wallR, cy + 1.5, cz + Math.sin(a) * wallR,
                        3, 0, 0.6, 0, 0.12);
            }
        }

        if (tick % 8 == 0) {
            double arcAngle = level.random.nextDouble() * Math.PI * 2;
            double ax1 = cx + Math.cos(arcAngle) * wallR;
            double az1 = cz + Math.sin(arcAngle) * wallR;
            double ax2 = cx + Math.cos(arcAngle + Math.PI) * wallR;
            double az2 = cz + Math.sin(arcAngle + Math.PI) * wallR;
            for (double t2 = 0; t2 <= 1.0; t2 += 0.04) {
                double lx = ax1 + (ax2 - ax1) * t2;
                double lz = az1 + (az2 - az1) * t2;
                double ly = cy + curtainH * 0.9 + Math.sin(t2 * Math.PI) * 4.0;
                level.sendParticles(ParticleTypes.END_ROD, lx, ly, lz, 1, 0, 0.01, 0, 0.02);
            }
        }

        for (int i = 0; i < 26; i++) {
            double ox = (level.random.nextDouble() - 0.5) * (wallR * 1.6);
            double oz = (level.random.nextDouble() - 0.5) * (wallR * 1.6);
            if (ox * ox + oz * oz <= (wallR - 1) * (wallR - 1)) {
                level.sendParticles(ParticleTypes.ASH,
                        cx + ox, cy + level.random.nextDouble() * curtainH * 0.8, cz + oz,
                        1, 0.3, 0, 0.3, 0);
                level.sendParticles(SILVER_SHIMMER,
                        cx + ox, cy + level.random.nextDouble() * curtainH * 0.8, cz + oz,
                        1, 0.2, 0.1, 0.2, 0);
            }
        }

        for (int i = 0; i < 72; i++) {
            double a = i * Math.PI * 2.0 / 72 + tick * 0.06;
            double r = 4.0 + Math.sin(i * 0.5 + tick * 0.08) * 2.0;
            double py = cy + 6.0 + Math.sin(a * 2.0 + tick * 0.04) * 3.5;
            level.sendParticles(CURTAIN_INNER,
                    cx + Math.cos(a) * r, py, cz + Math.sin(a) * r, 1, 0, 0, 0, 0);
            level.sendParticles(SILVER_SHIMMER,
                    cx + Math.cos(a) * (r + 1.2), py + 0.3, cz + Math.sin(a) * (r + 1.2), 1, 0, 0, 0, 0);
        }
    }
}
