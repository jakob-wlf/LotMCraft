package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlameCageAbility extends Ability {

    private static final DustParticleOptions CAGE_BLUE =
            new DustParticleOptions(new Vector3f(0.25f, 0.65f, 1.0f), 2.0f);
    private static final DustParticleOptions CAGE_SULFUR =
            new DustParticleOptions(new Vector3f(0.9f, 0.85f, 0.2f), 1.4f);

    private static final Map<UUID, Vec3> activeCages = new HashMap<>();

    public FlameCageAbility(String id) {
        super(id, 20f, "burning");
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }

    @Override
    public float getSpiritualityCost() {
        return 150;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (activeCages.containsKey(entity.getUUID())) return;

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 20, 2.0f);
        Vec3 cageCenter = targetEntity != null
                ? targetEntity.position()
                : AbilityUtil.getTargetLocation(entity, 20, 1.5f);

        double radius  = 5.0 * multiplier(entity);
        double height  = 8.0;
        int    duration = 20 * 6 * (int) Math.max(multiplier(entity) / 2, 1);

        activeCages.put(entity.getUUID(), cageCenter);

        serverLevel.playSound(null, BlockPos.containing(cageCenter),
                SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS, 3.0f, 0.6f);

        ServerScheduler.scheduleForDuration(0, 3, duration, () -> {
            drawCageParticles(serverLevel, cageCenter, radius, height);

            double dps = DamageLookup.lookupDps(6, 0.7, 3, 60) * multiplier(entity);
            AbilityUtil.damageNearbyEntities(serverLevel, entity,
                    radius - 0.5, dps, cageCenter, true, false,
                    true, 0,
                    ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, entity));

            AbilityUtil.getNearbyEntities(entity, serverLevel, cageCenter, radius + 1.5)
                    .stream()
                    .filter(t -> AbilityUtil.mayDamage(entity, t))
                    .forEach(t -> {
                        Vec3 horiz = new Vec3(t.getX() - cageCenter.x, 0, t.getZ() - cageCenter.z);
                        if (horiz.length() > radius - 0.3) {
                            Vec3 inward = horiz.normalize().scale(-0.45);
                            t.setDeltaMovement(t.getDeltaMovement().add(inward));
                            t.hurtMarked = true;
                            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 5, false, false, false));
                        }

                        if (random.nextInt(5) == 0) {
                            t.setRemainingFireTicks(20 * 3);
                        }
                    });
        }, () -> {
            activeCages.remove(entity.getUUID());
            ParticleUtil.spawnSphereParticles(serverLevel, CAGE_BLUE,
                    cageCenter.add(0, height / 2, 0), (double) radius + 1, 80);
            serverLevel.playSound(null, cageCenter.x, cageCenter.y, cageCenter.z,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 2.0f, 0.8f);
        }, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(cageCenter, serverLevel)));

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, cageCenter, entity, this,
                interactionFlags, radius, duration));
    }

    private void drawCageParticles(ServerLevel level, Vec3 center, double radius, double height) {
        int steps = 28;
        for (int i = 0; i < steps; i++) {
            double angle = (i / (double) steps) * Math.PI * 2;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            for (double y = 0; y < height; y += 1.8) {
                Vec3 p = new Vec3(x, center.y + y, z);
                if (random.nextBoolean()) {
                    ParticleUtil.spawnParticles(level, CAGE_BLUE,   p, 1, 0.12, 0.05);
                } else {
                    ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, p, 1, 0.15, 0.06);
                }
                if (random.nextInt(4) == 0) {
                    ParticleUtil.spawnParticles(level, CAGE_SULFUR, p, 1, 0.1, 0.0);
                }
            }
        }
        for (int i = 0; i < steps; i++) {
            double angle = (i / (double) steps) * Math.PI * 2;
            Vec3 fp = new Vec3(center.x + Math.cos(angle) * radius,
                               center.y, center.z + Math.sin(angle) * radius);
            ParticleUtil.spawnParticles(level, ParticleTypes.LAVA, fp, 1, 0.1, 0.05);
        }
    }
}