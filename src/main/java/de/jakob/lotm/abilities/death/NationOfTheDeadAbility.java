package de.jakob.lotm.abilities.death;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.impl.NationOfTheDeadEffect;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class NationOfTheDeadAbility extends Ability {

    private static final int DURATION_TICKS = 20 * 85;
    private static final float BASE_DPS_PERCENT = 0.03f;
    private static final float PER_SEQ_STEP = 0.005f;

    private static final DustParticleOptions DEATH_DUST =
            new DustParticleOptions(new Vector3f(0.08f, 0.0f, 0.18f), 1.8f);
    private static final DustParticleOptions SOUL_DUST =
            new DustParticleOptions(new Vector3f(0.30f, 0.0f, 0.55f), 1.2f);
    private static final DustParticleOptions VOID_DUST =
            new DustParticleOptions(new Vector3f(0.02f, 0.0f, 0.08f), 2.2f);
    private static final DustParticleOptions PALE_SOUL_DUST =
            new DustParticleOptions(new Vector3f(0.55f, 0.40f, 0.80f), 0.9f);
    private static final DustParticleOptions BONE_DUST =
            new DustParticleOptions(new Vector3f(0.85f, 0.82f, 0.78f), 0.7f);

    private static final Map<UUID, ActiveDomain> activeDomains = new HashMap<>();

    public NationOfTheDeadAbility(String id) {
        super(id, 180f, "death");
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 8000;
    }


    private static void spawnHelixParticles(ServerLevel level, DustParticleOptions dust,
                                            Vec3 base, double radius, double height,
                                            int turns, int steps, double offset) {
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            double angle = offset + t * turns * 2 * Math.PI;
            double x = base.x + radius * Math.cos(angle);
            double z = base.z + radius * Math.sin(angle);
            double y = base.y + t * height;
            level.sendParticles(dust, x, y, z, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    private static void spawnRingParticles(ServerLevel level, Object particleType,
                                           Vec3 center, double radius, int count,
                                           double yOffset, double spread) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = center.y + yOffset;
            if (particleType instanceof DustParticleOptions dust) {
                level.sendParticles(dust, x, y, z, 1, spread, spread * 0.3, spread, 0);
            } else if (particleType instanceof net.minecraft.core.particles.SimpleParticleType simple) {
                level.sendParticles(simple, x, y, z, 1, spread, spread * 0.3, spread, 0.01);
            }
        }
    }

    private static void spawnShellParticles(ServerLevel level, Object particleType,
                                            Vec3 center, double radius, int count) {
        java.util.Random rng = new Random();
        for (int i = 0; i < count; i++) {
            double u = rng.nextDouble() * 2 - 1;
            double theta = rng.nextDouble() * 2 * Math.PI;
            double r = Math.sqrt(1 - u * u);
            double x = center.x + radius * r * Math.cos(theta);
            double y = center.y + radius * u;
            double z = center.z + radius * r * Math.sin(theta);
            if (particleType instanceof DustParticleOptions dust) {
                level.sendParticles(dust, x, y, z, 1, 0, 0, 0, 0);
            } else if (particleType instanceof net.minecraft.core.particles.SimpleParticleType simple) {
                level.sendParticles(simple, x, y, z, 1, 0, 0, 0, 0.005);
            }
        }
    }

    private static void spawnSoulColumns(ServerLevel level, Vec3 center,
                                         double domainRadius, int columns, int particlesPerColumn) {
        java.util.Random rng = new Random();
        for (int c = 0; c < columns; c++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double dist = rng.nextDouble() * domainRadius * 0.9;
            double cx = center.x + dist * Math.cos(angle);
            double cz = center.z + dist * Math.sin(angle);
            for (int p = 0; p < particlesPerColumn; p++) {
                double y = center.y + (rng.nextDouble() * 12.0);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        cx + rng.nextGaussian() * 0.15,
                        y,
                        cz + rng.nextGaussian() * 0.15,
                        1, 0.05, 0.15, 0.05, 0.02);
            }
        }
    }

    private static void spawnShockwaveRing(ServerLevel level, Vec3 center,
                                           double currentRadius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = center.x + currentRadius * Math.cos(angle);
            double z = center.z + currentRadius * Math.sin(angle);
            level.sendParticles(VOID_DUST, x, center.y + 0.1, z, 3, 0.1, 0.5, 0.1, 0);
            level.sendParticles(ParticleTypes.SOUL, x, center.y + 0.1, z, 2, 0.2, 0.4, 0.2, 0.02);
        }
    }

    private static void spawnAmbientFog(ServerLevel level, Vec3 center,
                                        double radius, int count) {
        java.util.Random rng = new Random();
        for (int i = 0; i < count; i++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double dist = rng.nextDouble() * radius;
            double x = center.x + dist * Math.cos(angle);
            double z = center.z + dist * Math.sin(angle);
            double y = center.y + rng.nextDouble() * 4.0;
            level.sendParticles(SOUL_DUST, x, y, z, 1, 0.3, 0.2, 0.3, 0.005);
        }
    }


    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        int casterSeq = BeyonderData.getSequence(entity);
        Vec3 center = entity.position();
        double domainRadius = 35.0 * Math.max(multiplier(entity) / 4.0, 1);

        level.playSound(null, entity.blockPosition(),
                SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 4.0f, 0.35f);
        level.playSound(null, entity.blockPosition(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 2.5f, 0.50f);

        EffectManager.playEffect(EffectManager.Effect.NATION_OF_THE_DEAD, center.x, center.y, center.z, serverLevel, entity);

        List<UUID> subordinateMobs = new ArrayList<>();
        ActiveDomain domain = new ActiveDomain(entity.getUUID(), center, serverLevel, subordinateMobs);
        activeDomains.put(entity.getUUID(), domain);

        AtomicInteger ticks = new AtomicInteger(0);
        AtomicReference<UUID> taskId = new AtomicReference<>();

        for (int wave = 0; wave < 6; wave++) {
            final int waveIndex = wave;
            ServerScheduler.scheduleDelayed(waveIndex * 4, () -> {
                double waveRadius = domainRadius * ((waveIndex + 1) / 6.0);
                spawnShockwaveRing(serverLevel, center, waveRadius, 64);
            }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
        }

        taskId.set(ServerScheduler.scheduleForDuration(0, 1, DURATION_TICKS, () -> {
            Location loc = new Location(center, serverLevel);

            if (InteractionHandler.isInteractionPossibleStrictlyHigher(loc, "purification_holy", casterSeq, -1)) {
                activeDomains.remove(entity.getUUID());
                ServerScheduler.cancel(taskId.get());
                return;
            }

            int tick = ticks.get();
            double helixAngle = tick * 0.08;

            if (tick % 5 == 0) {
                spawnShellParticles(serverLevel, VOID_DUST, center, domainRadius, 18);
                spawnShellParticles(serverLevel, ParticleTypes.SOUL, center, domainRadius, 12);
            }

            if (tick % 4 == 0) {
                spawnShellParticles(serverLevel, SOUL_DUST, center, domainRadius * 0.65, 14);
            }

            spawnRingParticles(serverLevel, DEATH_DUST, center, domainRadius,
                    48, 0, 0.15);
            spawnRingParticles(serverLevel, DEATH_DUST, center, domainRadius,
                    48, 2.5, 0.12);
            spawnRingParticles(serverLevel, DEATH_DUST, center, domainRadius,
                    48, -2.5, 0.12);

            if (tick % 2 == 0) {
                spawnRingParticles(serverLevel, SOUL_DUST, center, domainRadius * 0.6,
                        36, 0, 0.10);
                spawnRingParticles(serverLevel, ParticleTypes.SOUL_FIRE_FLAME, center, domainRadius * 0.6,
                        24, 1.5, 0.08);
            }

            if (tick % 3 == 0) {
                spawnRingParticles(serverLevel, BONE_DUST, center, domainRadius * 0.3,
                        28, 0, 0.08);
                spawnRingParticles(serverLevel, PALE_SOUL_DUST, center, domainRadius * 0.45,
                        32, 0.8, 0.07);
            }

            spawnHelixParticles(serverLevel, SOUL_DUST, center,
                    domainRadius * 0.25, 18, 3, 60, helixAngle);
            spawnHelixParticles(serverLevel, DEATH_DUST, center,
                    domainRadius * 0.25, 18, 3, 60, helixAngle + Math.PI);

            if (tick % 2 == 0) {
                spawnHelixParticles(serverLevel, VOID_DUST, center,
                        domainRadius * 0.55, 22, 2, 48, helixAngle * 0.5);
                spawnHelixParticles(serverLevel, PALE_SOUL_DUST, center,
                        domainRadius * 0.55, 22, 2, 48, helixAngle * 0.5 + Math.PI);
            }

            if (tick % 8 == 0) {
                spawnSoulColumns(serverLevel, center, domainRadius, 6, 5);
            }

            if (tick % 3 == 0) {
                spawnAmbientFog(serverLevel, center, domainRadius, 20);
            }

            if (tick % 10 == 0) {
                spawnShellParticles(serverLevel, ParticleTypes.SOUL_FIRE_FLAME, center,
                        domainRadius * 0.85, 30);
                spawnShellParticles(serverLevel, ParticleTypes.SOUL_FIRE_FLAME, center,
                        domainRadius * 0.40, 20);
            }

            if (tick % 40 == 0) {
                spawnShockwaveRing(serverLevel, center, domainRadius, 72);
                spawnShockwaveRing(serverLevel, center, domainRadius * 0.7, 56);
                spawnShockwaveRing(serverLevel, center, domainRadius * 0.4, 40);
            }

            if (tick % 6 == 0) {
                java.util.Random rng = new Random();
                for (int i = 0; i < 30; i++) {
                    double angle = rng.nextDouble() * 2 * Math.PI;
                    double dist = rng.nextDouble() * domainRadius;
                    serverLevel.sendParticles(ParticleTypes.ASH,
                            center.x + dist * Math.cos(angle),
                            center.y + rng.nextDouble() * 3.0,
                            center.z + dist * Math.sin(angle),
                            1, 0.5, 0.1, 0.5, 0.01);
                }
            }

            if (tick % 40 == 0) {
                level.playSound(null, entity.blockPosition(),
                        SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.8f,
                        0.55f + serverLevel.random.nextFloat() * 0.2f);
            }
            if (tick % 20 == 0) {
                level.playSound(null, entity.blockPosition(),
                        SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), SoundSource.PLAYERS, 1.2f, 0.8f);
            }

            AbilityUtil.getNearbyEntities(entity, serverLevel, center, (int) domainRadius).forEach(target -> {
                if (AllyUtil.areAllies(entity, target)) return;
                if (subordinateMobs.contains(target.getUUID())) return;

                int targetSeq = BeyonderData.getSequence(target);
                int seqDiff = targetSeq - casterSeq;

                if (seqDiff >= 2) {
                    ModDamageTypes.trueDamage(target, Float.MAX_VALUE, serverLevel, entity);
                    return;
                }

                PhysicalEnhancementsAbility.suppressRegen(target, 2000);

                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, false, false));

                if (target instanceof ServerPlayer player) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, false));
                }

                if (tick % 20 != 0) return;

                float damagePercent = BASE_DPS_PERCENT + (seqDiff * PER_SEQ_STEP);
                if (damagePercent <= 0) return;

                float damage = target.getMaxHealth() * damagePercent;
                ModDamageTypes.trueDamage(target, damage, serverLevel, entity);

                Vec3 targetPos = target.position();
                spawnShellParticles(serverLevel, ParticleTypes.SOUL, targetPos, 1.5, 16);
                spawnShellParticles(serverLevel, SOUL_DUST, targetPos, 0.8, 12);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        targetPos.x, targetPos.y + 1, targetPos.z, 12, 0.4, 0.5, 0.4, 0.03);
                serverLevel.sendParticles(VOID_DUST,
                        targetPos.x, targetPos.y + 0.5, targetPos.z, 8, 0.3, 0.3, 0.3, 0);
            });

            ticks.getAndIncrement();
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel))));

        ServerScheduler.scheduleDelayed(DURATION_TICKS, () -> {
            activeDomains.remove(entity.getUUID());
        }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        LivingEntity dying = event.getEntity();
        Vec3 deathPos = dying.position();

        for (ActiveDomain domain : activeDomains.values()) {
            if (domain.level != serverLevel) continue;
            if (dying.getUUID().equals(domain.casterUUID)) continue;
            if (domain.subordinateMobs.contains(dying.getUUID())) continue;

            double dist = deathPos.distanceTo(domain.center);
            double domainRadius = 35.0; // base radius (multiplier not accessible statically)
            if (dist > domainRadius) continue;

            LivingEntity caster = serverLevel.getEntitiesOfClass(LivingEntity.class,
                            new net.minecraft.world.phys.AABB(
                                    domain.center.subtract(1, 1, 1),
                                    domain.center.add(1, 1, 1)))
                    .stream().filter(e -> e.getUUID().equals(domain.casterUUID))
                    .findFirst().orElse((LivingEntity) serverLevel.getEntity(domain.casterUUID));

            spawnDomainSkeleton(serverLevel, deathPos, caster, domain);

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    deathPos.x, deathPos.y + 0.5, deathPos.z, 40, 0.5, 0.7, 0.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    deathPos.x, deathPos.y + 0.5, deathPos.z, 30, 0.6, 0.6, 0.6, 0.02);
            serverLevel.sendParticles(SOUL_DUST,
                    deathPos.x, deathPos.y + 1.0, deathPos.z, 20, 0.4, 0.5, 0.4, 0);
            serverLevel.sendParticles(VOID_DUST,
                    deathPos.x, deathPos.y + 0.3, deathPos.z, 16, 0.5, 0.3, 0.5, 0);
            for (int i = 0; i < 20; i++) {
                double dy = i * 0.4;
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        deathPos.x + (serverLevel.random.nextGaussian() * 0.15),
                        deathPos.y + dy,
                        deathPos.z + (serverLevel.random.nextGaussian() * 0.15),
                        1, 0.05, 0.05, 0.05, 0.01);
            }
            serverLevel.playSound(null, dying.blockPosition(),
                    SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.HOSTILE, 2.5f, 0.65f);
            serverLevel.playSound(null, dying.blockPosition(),
                    SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 2.0f, 0.55f);
            break;
        }
    }

    private static void spawnDomainSkeleton(ServerLevel serverLevel, Vec3 spawnPos,
                                            LivingEntity caster, ActiveDomain domain) {
        Skeleton skeleton = new Skeleton(net.minecraft.world.entity.EntityType.SKELETON, serverLevel);
        skeleton.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        skeleton.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        skeleton.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        skeleton.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        skeleton.setDropChance(EquipmentSlot.HEAD, 0f);
        skeleton.setDropChance(EquipmentSlot.CHEST, 0f);
        skeleton.setDropChance(EquipmentSlot.LEGS, 0f);
        skeleton.setDropChance(EquipmentSlot.FEET, 0f);

        if (skeleton.getAttribute(Attributes.ATTACK_DAMAGE) != null
                && skeleton.getAttribute(Attributes.MAX_HEALTH) != null) {
            skeleton.getAttribute(Attributes.ATTACK_DAMAGE)
                    .setBaseValue(skeleton.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) * 6);
            skeleton.getAttribute(Attributes.MAX_HEALTH)
                    .setBaseValue(skeleton.getAttributeBaseValue(Attributes.MAX_HEALTH) * 6);
        }

        skeleton.setHealth(skeleton.getMaxHealth());
        serverLevel.addFreshEntity(skeleton);

        if (caster != null) {
            SubordinateUtils.turnEntityIntoSubordinate(skeleton, caster, false);
        }

        domain.subordinateMobs.add(skeleton.getUUID());
    }

    private static class ActiveDomain {
        final UUID casterUUID;
        final Vec3 center;
        final ServerLevel level;
        final List<UUID> subordinateMobs;

        ActiveDomain(UUID casterUUID, Vec3 center, ServerLevel level, List<UUID> subordinateMobs) {
            this.casterUUID = casterUUID;
            this.center = center;
            this.level = level;
            this.subordinateMobs = subordinateMobs;
        }
    }
}