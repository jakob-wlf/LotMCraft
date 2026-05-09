package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.PerformantExplosion;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StarGraftingAbility extends Ability {

    private static final DustParticleOptions STAR_GOLD =
            new DustParticleOptions(new Vector3f(1.0f, 0.90f, 0.15f), 2.5f);
    private static final DustParticleOptions STAR_WHITE =
            new DustParticleOptions(new Vector3f(0.95f, 0.95f, 1.0f), 2.0f);
    private static final DustParticleOptions STAR_CORONA =
            new DustParticleOptions(new Vector3f(1.0f, 0.65f, 0.05f), 3.5f);
    private static final DustParticleOptions STAR_VIOLET =
            new DustParticleOptions(new Vector3f(0.68f, 0.34f, 1.0f), 3.2f);
    private static final DustParticleOptions STAR_SUPERNOVA_WHITE =
            new DustParticleOptions(new Vector3f(0.98f, 0.98f, 1.0f), 4.4f);
    private static final DustParticleOptions BLAST_ORANGE =
            new DustParticleOptions(new Vector3f(1.0f, 0.42f, 0.0f), 4.0f);
    private static final DustParticleOptions BLAST_WHITE =
            new DustParticleOptions(new Vector3f(1.0f, 0.98f, 0.92f), 4.0f);
    private static final DustParticleOptions WIND_RED =
            new DustParticleOptions(new Vector3f(1.0f, 0.18f, 0.0f), 3.0f);
    private static final DustParticleOptions WIND_AMBER =
            new DustParticleOptions(new Vector3f(1.0f, 0.65f, 0.0f), 2.5f);
    private static final DustParticleOptions NEBULA_VIOLET =
            new DustParticleOptions(new Vector3f(0.55f, 0.05f, 0.9f), 3.0f);
    private static final DustParticleOptions NEBULA_INDIGO =
            new DustParticleOptions(new Vector3f(0.15f, 0.25f, 1.0f), 3.0f);
    private static final DustParticleOptions NEBULA_CRIMSON =
            new DustParticleOptions(new Vector3f(0.85f, 0.04f, 0.12f), 2.5f);
    private static final DustParticleOptions NEBULA_AMBER =
            new DustParticleOptions(new Vector3f(0.95f, 0.55f, 0.04f), 2.5f);
    private static final DustParticleOptions NEBULA_GOLD =
            new DustParticleOptions(new Vector3f(1.0f, 0.88f, 0.0f), 2.5f);

    private static final int FALSE_NIGHT_TICKS = 20 * 60 * 5;
    private static final int DAMAGE_RADIUS = 68;
    private static final int WIND_START_RADIUS = 36;
    private static final int WIND_END_RADIUS = 150;
    private static final int CORE_RADIATION_RADIUS = 52;
    private static final int NEBULA_OUTER_RADIUS = 120;
    private static final int PRELUDE_LIGHT_DURATION = 10;
    private static final int BLAST_LIGHT_DURATION = 24;
    private static final int AFTERGLOW_LIGHT_DURATION = 40;
    private static final float GRIEFING_BLAST_RADIUS = 44.0f;
    private static final float GRIEFING_CORE_BLAST_RADIUS = 28.0f;
    private static final int GRIEFING_CRATER_RADIUS = 26;
    private static final int GRIEFING_SCORCH_RADIUS = 70;

    public StarGraftingAbility() {
        super("star_grafting", 120);
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
        return 800;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 center = entity.position();
        boolean griefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        float mult = multiplier(entity);
        int foolColor = BeyonderData.pathwayInfos.get("fool").color();
        Map<BlockPos, Long> temporaryLights = new HashMap<>();

        for (Player player : serverLevel.getEntitiesOfClass(Player.class, new AABB(center, center).inflate(120))) {
            player.displayClientMessage(Component.translatable("lotmcraft.ability.star_grafting.cast").withColor(foolColor), true);
        }

        triggerFalseNight(serverLevel, center);

        AtomicInteger s1 = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(0, 1, 100, () -> {
            int t = s1.getAndIncrement();
            double prog = t / 100.0;
            double armR = 70.0 * (1.0 - prog) + 4.0;

            if (t % 4 == 0) {
                placeSupernovaLightCluster(serverLevel, temporaryLights, center, 4.0 + prog * 8.0, 2.0, PRELUDE_LIGHT_DURATION, 6);
                placeTemporaryLight(serverLevel, temporaryLights, BlockPos.containing(center.x, center.y + 8.0, center.z), PRELUDE_LIGHT_DURATION);
            }

            for (int arm = 0; arm < 12; arm++) {
                double baseAngle = arm * (Math.PI / 6.0) + t * 0.09;
                for (int bead = 0; bead < 48; bead++) {
                    double frac = bead / 48.0;
                    double r = armR * (1.0 - frac * 0.2);
                    double angle = baseAngle - frac * 0.7;
                    double px = center.x + Math.cos(angle) * r;
                    double pz = center.z + Math.sin(angle) * r;
                    double py = center.y + Math.sin(frac * Math.PI) * 10.0 + 1.5;
                    DustParticleOptions color = frac < 0.25 ? STAR_SUPERNOVA_WHITE
                            : (frac < 0.55 ? STAR_GOLD : (frac < 0.8 ? STAR_VIOLET : STAR_CORONA));
                    serverLevel.sendParticles(color, px, py, pz, 1, 0, 0, 0, 0);
                }
            }

            for (int ring = 0; ring < 3; ring++) {
                double ringR = (6.0 + ring * 5.0) + Math.sin(t * 0.22 + ring * 1.3) * 1.3;
                double ringY = center.y + 2.0 + ring * 4.0;
                int pts = 96;
                for (int i = 0; i < pts; i++) {
                    double a = i * Math.PI * 2.0 / pts + t * 0.06 * (ring + 1);
                    serverLevel.sendParticles(ring == 1 ? STAR_VIOLET : STAR_CORONA,
                            center.x + Math.cos(a) * ringR, ringY,
                            center.z + Math.sin(a) * ringR, 1, 0, 0, 0, 0);
                    if (ring != 0 && i % 2 == 0) {
                        serverLevel.sendParticles(STAR_SUPERNOVA_WHITE,
                                center.x + Math.cos(a) * (ringR - 1.1), ringY + 0.4,
                                center.z + Math.sin(a) * (ringR - 1.1), 1, 0, 0, 0, 0);
                    }
                }
            }

            double coreSpread = 2.5 + prog * 6.0;
            serverLevel.sendParticles(STAR_GOLD, center.x, center.y + 2.5, center.z, 24, coreSpread, coreSpread * 0.8, coreSpread, 0);
            serverLevel.sendParticles(STAR_WHITE, center.x, center.y + 2.5, center.z, 18, coreSpread * 0.7, coreSpread * 0.5, coreSpread * 0.7, 0);
            serverLevel.sendParticles(STAR_VIOLET, center.x, center.y + 2.5, center.z, 16, coreSpread * 0.8, coreSpread * 0.6, coreSpread * 0.8, 0);

            for (int i = 0; i < 24; i++) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        center.x + (serverLevel.random.nextDouble() - 0.5) * 10.0,
                        center.y + 2.0,
                        center.z + (serverLevel.random.nextDouble() - 0.5) * 10.0,
                        1, 0, 1.4, 0, 0.28);
            }
            for (int i = 0; i < 18; i++) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        center.x + (serverLevel.random.nextDouble() - 0.5) * 9.0,
                        center.y + 2.5 + serverLevel.random.nextDouble() * 5.0,
                        center.z + (serverLevel.random.nextDouble() - 0.5) * 9.0,
                        1, 0, 0.05, 0, 0.02);
            }

            int flashEvery = Math.max(3, 9 - (int) (prog * 7));
            if (t % flashEvery == 0) {
                serverLevel.sendParticles(ParticleTypes.FLASH, center.x, center.y + 2.5, center.z, 1, 0, 0, 0, 0);
            }

            if (t > 80) {
                serverLevel.sendParticles(BLAST_ORANGE, center.x, center.y + 2.5, center.z, 20, 7.0, 4.5, 7.0, 0);
                serverLevel.sendParticles(STAR_SUPERNOVA_WHITE, center.x, center.y + 2.5, center.z, 16, 6.0, 3.5, 6.0, 0);
                serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 2.5, center.z, 24, 5.5, 3.5, 5.5, 0.12);
            }

            if (t == 50) {
                serverLevel.playSound(null, center.x, center.y, center.z,
                        SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 3.0f, 0.4f);
            }
        }, serverLevel);

        ServerScheduler.scheduleDelayed(100, () -> {
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 10.0f, 0.45f);
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 5.0f, 0.35f);
            serverLevel.playSound(null, center.x, center.y, center.z,
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.AMBIENT, 4.0f, 0.5f);

            placeSupernovaLightCluster(serverLevel, temporaryLights, center, 8.0, 2.0, BLAST_LIGHT_DURATION, 8);
            placeSupernovaLightCluster(serverLevel, temporaryLights, center, 16.0, 4.0, BLAST_LIGHT_DURATION, 12);
            placeSupernovaLightCluster(serverLevel, temporaryLights, center, 26.0, 6.0, BLAST_LIGHT_DURATION, 16);

            ParticleUtil.spawnSphereParticles(serverLevel, STAR_SUPERNOVA_WHITE, center.add(0, 2, 0), 16.0, 900, 0.0);
            ParticleUtil.spawnSphereParticles(serverLevel, STAR_VIOLET, center.add(0, 2, 0), 12.0, 500, 0.0);

            for (int i = 0; i < 120; i++) {
                double theta = serverLevel.random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * serverLevel.random.nextDouble() - 1);
                double r = serverLevel.random.nextDouble() * 55.0;
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        center.x + r * Math.sin(phi) * Math.cos(theta),
                        center.y + r * Math.sin(phi) * Math.sin(theta) + 2.0,
                        center.z + r * Math.cos(phi), 1, 0, 0, 0, 0);
            }

            for (int i = 0; i < 140; i++) {
                double ox = (serverLevel.random.nextDouble() - 0.5) * 110.0;
                double oy = (serverLevel.random.nextDouble() - 0.5) * 40.0;
                double oz = (serverLevel.random.nextDouble() - 0.5) * 110.0;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        center.x + ox, center.y + oy + 2.0, center.z + oz, 1, 0, 0, 0, 0);
            }

            ParticleUtil.spawnSphereParticles(serverLevel, BLAST_ORANGE, center.add(0, 2, 0), 34.0, 1200, 0.0);
            ParticleUtil.spawnSphereParticles(serverLevel, BLAST_WHITE, center.add(0, 2, 0), 24.0, 800, 0.0);
            ParticleUtil.spawnSphereParticles(serverLevel, STAR_VIOLET, center.add(0, 2, 0), 28.0, 900, 0.0);

            if (griefing) {
                PerformantExplosion.create(serverLevel, entity, center.add(0, 2, 0), GRIEFING_BLAST_RADIUS, true,
                        Explosion.BlockInteraction.DESTROY_WITH_DECAY);
                PerformantExplosion.create(serverLevel, entity, center.add(0, -6, 0), GRIEFING_CORE_BLAST_RADIUS, false,
                        Explosion.BlockInteraction.DESTROY_WITH_DECAY);
                scorchSupernovaImpact(serverLevel, center);
            }

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(DAMAGE_RADIUS))) {
                if (victim == entity) continue;
                Vec3 diff = victim.position().subtract(center);
                double dist = diff.length();
                if (dist > DAMAGE_RADIUS) continue;

                double force = Math.max(0.0, (DAMAGE_RADIUS - dist) / DAMAGE_RADIUS) * 6.8;
                Vec3 kb = dist > 0 ? diff.normalize().scale(force).add(0, 2.5, 0) : new Vec3(0, 4.5, 0);
                victim.setDeltaMovement(victim.getDeltaMovement().add(kb));
                victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), 140 * mult);
            }

            for (int wave = 1; wave <= 14; wave++) {
                final int w = wave;
                ServerScheduler.scheduleDelayed(wave * 2, () -> {
                    double wr = w * 7.5;
                    int pts = 140 + w * 18;
                    placeSupernovaLightCluster(serverLevel, temporaryLights, center, wr, 2.0, BLAST_LIGHT_DURATION, Math.max(8, Math.min(20, 6 + w)));
                    for (int i = 0; i < pts; i++) {
                        double a = i * Math.PI * 2.0 / pts;
                        serverLevel.sendParticles(BLAST_ORANGE,
                                center.x + Math.cos(a) * wr, center.y + 1.5, center.z + Math.sin(a) * wr,
                                1, 0, 0, 0, 0);
                        serverLevel.sendParticles(STAR_SUPERNOVA_WHITE,
                                center.x + Math.cos(a) * wr, center.y + 3.0, center.z + Math.sin(a) * wr,
                                1, 0, 0, 0, 0);
                        serverLevel.sendParticles(STAR_VIOLET,
                                center.x + Math.cos(a) * (wr - 2.0), center.y + 4.6, center.z + Math.sin(a) * (wr - 2.0),
                                1, 0, 0, 0, 0);
                        serverLevel.sendParticles(ParticleTypes.FLAME,
                                center.x + Math.cos(a) * wr, center.y + 2.2, center.z + Math.sin(a) * wr,
                                1, 0, 0.5, 0, 0.1);
                    }
                    if (w % 2 == 0) {
                        serverLevel.playSound(null, center.x, center.y, center.z,
                                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.5f, 0.75f + w * 0.04f);
                    }
                }, serverLevel);
            }
        }, serverLevel);

        AtomicInteger s3 = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(120, 2, 480, () -> {
            int exec = s3.getAndIncrement();
            double maxExec = 480.0 / 2;
            double radius = WIND_START_RADIUS + (exec / maxExec) * (WIND_END_RADIUS - WIND_START_RADIUS);
            int pts = 168;

            double[] yOffsets = {1.0, 6.0, 12.0, 18.0};
            for (double yOff : yOffsets) {
                for (int i = 0; i < pts; i++) {
                    double a = i * Math.PI * 2.0 / pts;
                    double px = center.x + Math.cos(a) * radius;
                    double pz = center.z + Math.sin(a) * radius;
                    double py = center.y + yOff;
                    serverLevel.sendParticles(WIND_RED, px, py, pz, 1, 0, 0.4, 0, 0);
                    serverLevel.sendParticles(WIND_AMBER, px, py + 1.0, pz, 1, 0, 0.4, 0, 0);
                    serverLevel.sendParticles(STAR_VIOLET, px, py + 2.0, pz, 1, 0, 0.4, 0, 0);
                    serverLevel.sendParticles(STAR_SUPERNOVA_WHITE, px, py + 3.2, pz, 1, 0, 0.4, 0, 0);
                    serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 3, 0, 0.6, 0, 0.09);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, px, py + 2.5, pz, 1, 0, 0.3, 0, 0.05);
                }

                for (int i = 0; i < 48; i++) {
                    double a = serverLevel.random.nextDouble() * Math.PI * 2;
                    double jitter = (serverLevel.random.nextDouble() - 0.5) * 2;
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            center.x + Math.cos(a) * (radius + jitter),
                            center.y + yOff,
                            center.z + Math.sin(a) * (radius + jitter), 1, 0, 0, 0, 0);
                }
            }

            AABB ringBox = new AABB(center, center).inflate(radius + 8);
            for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, ringBox)) {
                if (victim == entity) continue;
                double dist = victim.position().distanceTo(center);
                if (dist >= radius - 6 && dist <= radius + 6) {
                    victim.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity), 28 * mult);
                    victim.setRemainingFireTicks(120);
                }
            }

            if (griefing && serverLevel.random.nextFloat() < 0.40f) {
                double a = serverLevel.random.nextDouble() * Math.PI * 2;
                BlockPos fp = BlockPos.containing(
                        center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius);
                if (serverLevel.isEmptyBlock(fp) && serverLevel.getBlockState(fp.below()).isSolid()) {
                    serverLevel.setBlockAndUpdate(fp, Blocks.FIRE.defaultBlockState());
                }
            }

            if (exec % 15 == 0) {
                serverLevel.playSound(null, center.x, center.y, center.z,
                        SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3.0f, 0.65f);
            }
        }, serverLevel);

        AtomicInteger s4 = new AtomicInteger(0);
        ServerScheduler.scheduleForDuration(600, 8, 3000, () -> {
            int exec = s4.getAndIncrement();
            double pulse = Math.sin(exec * 0.07) * 2.0;

            if (exec % 4 == 0) {
                placeSupernovaLightCluster(serverLevel, temporaryLights, center, 10.0 + pulse, 3.0, AFTERGLOW_LIGHT_DURATION, 8);
                placeSupernovaLightCluster(serverLevel, temporaryLights, center, 24.0 + pulse, 5.0, AFTERGLOW_LIGHT_DURATION, 10);
                placeSupernovaLightCluster(serverLevel, temporaryLights, center, 40.0 - pulse, 7.0, AFTERGLOW_LIGHT_DURATION, 12);
            }

            double[] radii = {NEBULA_OUTER_RADIUS, 98.0, 76.0, 54.0, 32.0};
            int[] counts = {420, 360, 300, 240, 180};
            DustParticleOptions[] colors = {NEBULA_VIOLET, NEBULA_INDIGO, NEBULA_CRIMSON, NEBULA_AMBER, NEBULA_GOLD};
            for (int shell = 0; shell < 5; shell++) {
                double radius = radii[shell] + pulse * (shell % 2 == 0 ? 1 : -1);
                ParticleUtil.spawnSphereParticles(serverLevel, colors[shell], center.add(0, 3, 0), radius, counts[shell], 0.0);
            }
            ParticleUtil.spawnSphereParticles(serverLevel, STAR_SUPERNOVA_WHITE, center.add(0, 6, 0), 88.0 + pulse, 240, 0.0);
            ParticleUtil.spawnSphereParticles(serverLevel, STAR_VIOLET, center.add(0, 5, 0), 108.0 - pulse, 280, 0.0);

            for (int i = 0; i < 56; i++) {
                double radius = 12.0 + serverLevel.random.nextDouble() * 88.0;
                double theta = serverLevel.random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * serverLevel.random.nextDouble() - 1);
                double ox = radius * Math.sin(phi) * Math.cos(theta);
                double oy = radius * Math.sin(phi) * Math.sin(theta);
                double oz = radius * Math.cos(phi);
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                        center.x + ox, center.y + oy + 3.0, center.z + oz, 1, 0.6, 0.6, 0.6, 0.03);
            }

            for (int i = 0; i < 48; i++) {
                double radius = 8.0 + serverLevel.random.nextDouble() * 92.0;
                double theta = serverLevel.random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * serverLevel.random.nextDouble() - 1);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        center.x + radius * Math.sin(phi) * Math.cos(theta),
                        center.y + radius * Math.sin(phi) * Math.sin(theta) + 3.0,
                        center.z + radius * Math.cos(phi), 1, 0, 0.02, 0, 0.01);
            }

            for (int i = 0; i < 52; i++) {
                double radius = 8.0 + serverLevel.random.nextDouble() * 90.0;
                double theta = serverLevel.random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * serverLevel.random.nextDouble() - 1);
                double x = center.x + radius * Math.sin(phi) * Math.cos(theta);
                double y = center.y + radius * Math.sin(phi) * Math.sin(theta) + 3.0;
                double z = center.z + radius * Math.cos(phi);
                serverLevel.sendParticles(STAR_SUPERNOVA_WHITE, x, y, z, 1, 0.3, 0.3, 0.3, 0);
                serverLevel.sendParticles(STAR_VIOLET, x, y, z, 1, 0.3, 0.3, 0.3, 0);
            }

            for (int i = 0; i < 28; i++) {
                double ox = (serverLevel.random.nextDouble() - 0.5) * 100.0;
                double oz = (serverLevel.random.nextDouble() - 0.5) * 100.0;
                if (ox * ox + oz * oz <= CORE_RADIATION_RADIUS * CORE_RADIATION_RADIUS) {
                    serverLevel.sendParticles(ParticleTypes.SOUL,
                            center.x + ox, center.y + serverLevel.random.nextDouble() * 8.0,
                            center.z + oz, 1, 0, 0.08, 0, 0.02);
                }
            }

            if (exec % 25 == 0) {
                for (int beam = 0; beam < 6; beam++) {
                    double a = beam * (Math.PI / 3) + exec * 0.04;
                    double bx = center.x + Math.cos(a) * 25.0;
                    double bz = center.z + Math.sin(a) * 25.0;
                    for (double by = center.y - 5.0; by < center.y + 60.0; by += 0.7) {
                        serverLevel.sendParticles(ParticleTypes.END_ROD, bx, by, bz, 1, 0, 0.01, 0, 0.04);
                    }
                }
            }

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(CORE_RADIATION_RADIUS))) {
                if (victim == entity) continue;
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            }

            if (exec % 3 == 0) {
                for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(NEBULA_OUTER_RADIUS))) {
                    if (victim == entity) continue;
                    double dist = victim.position().distanceTo(center);
                    if (dist >= CORE_RADIATION_RADIUS && dist <= NEBULA_OUTER_RADIUS) {
                        victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                        victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                    }
                }
            }

            if (griefing && serverLevel.random.nextFloat() < 0.10f) {
                double a = serverLevel.random.nextDouble() * Math.PI * 2;
                double radius = serverLevel.random.nextDouble() * 40.0;
                BlockPos fp = BlockPos.containing(
                        center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius);
                if (serverLevel.isEmptyBlock(fp) && serverLevel.getBlockState(fp.below()).isSolid()) {
                    serverLevel.setBlockAndUpdate(fp, Blocks.FIRE.defaultBlockState());
                }
            }
        }, serverLevel);
    }

    private static void triggerFalseNight(ServerLevel level, Vec3 center) {
        long originalTime = level.getDayTime();
        level.setWeatherParameters(FALSE_NIGHT_TICKS + 200, 0, false, false);

        ServerScheduler.scheduleForDuration(0, 20, FALSE_NIGHT_TICKS, () -> {
            long cycleBase = (originalTime / 24000L) * 24000L + 18000L;
            long cycleOffset = level.getGameTime() % 2200L;
            level.setDayTime(cycleBase + cycleOffset);
            level.setWeatherParameters(FALSE_NIGHT_TICKS + 200, 0, false, false);
            spawnFalseNightSky(level, center);
        }, () -> level.setDayTime(originalTime + FALSE_NIGHT_TICKS), level);
    }

    private static void spawnFalseNightSky(ServerLevel level, Vec3 center) {
        for (int i = 0; i < 180; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double radius = 40.0 + level.random.nextDouble() * 130.0;
            double height = 48.0 + level.random.nextDouble() * 44.0;
            double x = center.x + Math.cos(angle) * radius;
            double y = center.y + height;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(STAR_SUPERNOVA_WHITE, x, y, z, 1, 0.45, 0.45, 0.45, 0.0);
            level.sendParticles(STAR_VIOLET, x, y + 0.3, z, 1, 0.4, 0.4, 0.4, 0.0);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    private static void placeSupernovaLightCluster(ServerLevel level, Map<BlockPos, Long> temporaryLights, Vec3 center,
                                                   double radius, double yOffset, int duration, int points) {
        placeTemporaryLight(level, temporaryLights, BlockPos.containing(center.x, center.y + yOffset, center.z), duration);

        for (int i = 0; i < points; i++) {
            double angle = i * Math.PI * 2.0 / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            placeTemporaryLight(level, temporaryLights, BlockPos.containing(x, center.y + yOffset, z), duration);
        }
    }

    private static void placeTemporaryLight(ServerLevel level, Map<BlockPos, Long> temporaryLights, BlockPos pos, int duration) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.LIGHT) && !state.getCollisionShape(level, pos).isEmpty()) {
            return;
        }

        BlockPos immutablePos = pos.immutable();
        long expiresAt = level.getGameTime() + duration;
        temporaryLights.put(immutablePos, expiresAt);

        if (!state.is(Blocks.LIGHT)) {
            level.setBlockAndUpdate(immutablePos, Blocks.LIGHT.defaultBlockState());
        }

        ServerScheduler.scheduleDelayed(duration, () -> {
            if (temporaryLights.getOrDefault(immutablePos, -1L) != expiresAt) {
                return;
            }

            if (level.getBlockState(immutablePos).is(Blocks.LIGHT)) {
                level.setBlockAndUpdate(immutablePos, Blocks.AIR.defaultBlockState());
            }
            temporaryLights.remove(immutablePos);
        }, level);
    }

    private static void scorchSupernovaImpact(ServerLevel level, Vec3 center) {
        int centerX = (int) Math.floor(center.x);
        int centerZ = (int) Math.floor(center.z);

        for (int bx = -GRIEFING_SCORCH_RADIUS; bx <= GRIEFING_SCORCH_RADIUS; bx++) {
            for (int bz = -GRIEFING_SCORCH_RADIUS; bz <= GRIEFING_SCORCH_RADIUS; bz++) {
                int distSq = bx * bx + bz * bz;
                if (distSq > GRIEFING_SCORCH_RADIUS * GRIEFING_SCORCH_RADIUS) {
                    continue;
                }

                int worldX = centerX + bx;
                int worldZ = centerZ + bz;
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
                BlockPos surfacePos = new BlockPos(worldX, Math.max(level.getMinBuildHeight(), topY - 1), worldZ);
                BlockState surfaceState = level.getBlockState(surfacePos);
                if (surfaceState.is(Blocks.BEDROCK)) {
                    continue;
                }

                if (distSq <= GRIEFING_CRATER_RADIUS * GRIEFING_CRATER_RADIUS) {
                    if (surfaceState.getDestroySpeed(level, surfacePos) >= 0) {
                        if (distSq <= 64) {
                            level.setBlockAndUpdate(surfacePos, Blocks.LAVA.defaultBlockState());
                        } else if (distSq <= 196) {
                            level.setBlockAndUpdate(surfacePos, Blocks.MAGMA_BLOCK.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(surfacePos, Blocks.BASALT.defaultBlockState());
                        }
                    }
                    continue;
                }

                if (distSq <= 55 * 55 && surfaceState.getDestroySpeed(level, surfacePos) >= 0 && ((bx * 31 + bz * 17) & 3) == 0) {
                    level.setBlockAndUpdate(surfacePos, distSq <= 42 * 42
                            ? Blocks.MAGMA_BLOCK.defaultBlockState()
                            : Blocks.BASALT.defaultBlockState());
                }

                BlockPos firePos = surfacePos.above();
                if (level.isEmptyBlock(firePos) && level.getBlockState(firePos.below()).isSolid() && ((bx * 13 + bz * 29) & 7) == 0) {
                    level.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
                }
            }
        }
    }
}
