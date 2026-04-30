package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DeathSpellsAbility extends SelectableAbility {

    public DeathSpellsAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 750;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.death_spells.withering_wind",
                "ability.lotmcraft.death_spells.soul_siphon"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        switch (selectedAbility) {
            case 0 -> castWitheringWind(serverLevel, entity);
            case 1 -> castSoulSiphon(serverLevel, entity);
        }
    }

    private void castWitheringWind(ServerLevel level, LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();

        Vec3[] pos = { caster.getEyePosition().add(flatLook.scale(2.0)) };
        List<Vec3> trail = new ArrayList<>();

        ServerScheduler.scheduleForDuration(0, 1, 80, () -> {
            trail.add(pos[0].add(0, 0, 0));

            for (Vec3 trailPos : trail) {
                for(int i = 0; i < 3; i++) {
                    if (Math.random() < 0.4) {
                        Vec3 soulLoc = trailPos.add(
                                (Math.random() * 14) - 7,
                                (Math.random() * 16) - 8,
                                (Math.random() * 14) - 7
                        );
                        ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, soulLoc, 0,
                                flatLook.x * 0.25, flatLook.y * 0.25, flatLook.z * 0.25, .5);

                        Vec3 smokeLoc = trailPos.add(
                                (Math.random() * 14) - 7,
                                (Math.random() * 16) - 8,
                                (Math.random() * 14) - 7
                        );
                        ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, smokeLoc, 0,
                                flatLook.x * 0.25, flatLook.y * 0.25, flatLook.z * 0.25, .5);
                    }
                }
            }

            AbilityUtil.damageNearbyEntities(level, caster, 9, DamageLookup.lookupDamage(4, .8) * multiplier(caster), pos[0], true, false);

            pos[0] = pos[0].add(flatLook.scale(0.5));
            if(BeyonderData.isGriefingEnabled(caster)) {
                AbilityUtil.getBlocksInSphereRadius(level, pos[0], 8, true, true, false).forEach(b -> {
                    if(random.nextInt(5) == 0) return;
                    BlockState state = level.getBlockState(b);
                    if(state.getDestroySpeed(level, b) < 0) return;
                    if(state.getBlock() == Blocks.SOUL_SOIL || state.getBlock() == Blocks.BASALT) return;

                    if(random.nextBoolean()) level.setBlockAndUpdate(b, Blocks.SOUL_SOIL.defaultBlockState());
                    else                     level.setBlockAndUpdate(b, Blocks.BASALT.defaultBlockState());
                });
            }
        }, null, level, () -> AbilityUtil.getTimeInArea(caster, new Location(caster.position(), level)));

        ServerScheduler.scheduleDelayed(80, () -> {
            Vec3 finalPos = pos[0];
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, finalPos, 5, 30);
            ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, finalPos, 30, 4.0);
        }, level, () -> AbilityUtil.getTimeInArea(caster, new Location(caster.position(), level)));
    }

    private void castSoulSiphon(ServerLevel level, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 20, 0.8f, true);
        if (target == null) return;

        AtomicInteger tick = new AtomicInteger(0);

        ServerScheduler.scheduleForDuration(0, 1, 60, () -> {
            int t = tick.getAndIncrement();

            if (!target.isAlive() || !caster.isAlive()) return;
            if (target.distanceTo(caster) > 25) return;

            Vec3 from = target.getEyePosition();
            Vec3 to = caster.getEyePosition();
            Vec3 dir = to.subtract(from).normalize();

            double pulse = 0.5 + 0.35 * Math.sin(t * 0.45);

            double drainProgress = t / 60.0;
            double auraRadius = 1.1 - drainProgress * 0.5;
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL_FIRE_FLAME, from, auraRadius + pulse * 0.3, 5);
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, from, auraRadius * 0.7, 3);
            ParticleUtil.spawnParticles(level, ParticleTypes.ASH, target.position().add(0, 0.5, 0), 3, 0.25);

            ParticleUtil.drawParticleLine(level, ParticleTypes.SOUL, from, to, 0.45, 1);

            if (t % 2 == 0) {
                double beamT = (t % 20) / 20.0;
                Vec3 motePos = from.add(to.subtract(from).scale(beamT));
                double spiralAngle = t * 0.5;
                Vec3 perp = dir.cross(new Vec3(0, 1, 0)).normalize().scale(0.25 * Math.sin(spiralAngle));
                ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, motePos.add(perp), 1, 0.03);
            }

            if (t % 5 == 0) {
                ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE, caster.position().add(0, 1, 0), 2, 0.2);
            }

            if (t % 4 == 0) {
                if (!AbilityUtil.mayTarget(caster, target)) return;
                boolean hit = target.hurt(ModDamageTypes.source(level, ModDamageTypes.BEYONDER_GENERIC, caster), (float) (DamageLookup.lookupDamage(4, .6) * multiplier(caster)));
                if (hit) {
                    caster.heal(2.5f * 0.6f);
                    ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL_FIRE_FLAME, caster.getEyePosition(), 0.8, 8);
                    ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, caster.getEyePosition(), 3, 0.15);
                }
            }
        }, null, level, () -> AbilityUtil.getTimeInArea(caster, new Location(caster.position(), level)));

        ServerScheduler.scheduleDelayed(60, () -> {
            if (target.isAlive()) {
                ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, target.getEyePosition(), 1.8, 30);
                ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL_FIRE_FLAME, target.getEyePosition(), 1.2, 15);
                ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, target.position().add(0, 1, 0), 14, 0.6);
                ParticleUtil.spawnParticles(level, ParticleTypes.ASH, target.getEyePosition(), 20, 0.8);
            }
        }, level, () -> AbilityUtil.getTimeInArea(caster, new Location(caster.position(), level)));
    }
}