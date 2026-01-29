package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.VectorUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NatureSpellsAbility extends SelectableAbility {
    private final HashSet<UUID> castingSwamp = new HashSet<>();
    private final HashSet<UUID> castingChildOfOak = new HashSet<>();
    private final HashSet<UUID> affectedByNatureWrath = new HashSet<>();

    public NatureSpellsAbility(String id) {
        super(id, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 350;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.nature_spells.swamp", "ability.lotmcraft.nature_spells.child_of_oak", "ability.lotmcraft.nature_spells.natures_wrath"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch(abilityIndex) {
            case 0 -> swamp(serverLevel, entity);
            case 1 -> childOfOak(serverLevel, entity);
            case 2 -> naturesWrath(serverLevel, entity);
        }
    }

    private final DustParticleOptions brownDust = new DustParticleOptions(new Vector3f(82 / 255f, 56 / 255f, 33 / 255f), 6f);
    private final DustParticleOptions brownDustSmall = new DustParticleOptions(new Vector3f(82 / 255f, 56 / 255f, 33 / 255f), 1.5f);
    private final DustParticleOptions greenDustSmall = new DustParticleOptions(new Vector3f(73 / 255f, 110 / 255f, 82 / 255f), 1.5f);
    private final DustParticleOptions plantDust = new DustParticleOptions(new Vector3f(33 / 255f, 163 / 255f, 52 / 255f), .7f);


    private void naturesWrath(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2);
        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.nature_spells.natures_wrath.no_target").withColor(0x496e52));
            return;
        }

        if(affectedByNatureWrath.contains(target.getUUID())) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.nature_spells.natures_wrath.already_affected").withColor(0x496e52));
            return;
        }

        affectedByNatureWrath.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 2, 20 * 25, () -> {
            if(target.isDeadOrDying()) {
                affectedByNatureWrath.remove(target.getUUID());
                return;
            }

            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.COMPOSTER, target.position().add(0, entity.getEyeHeight() / 2, 0), 10, .2, entity.getEyeHeight() / 2, .2, 0);
            ParticleUtil.spawnParticles(serverLevel, brownDustSmall, target.position().add(0, entity.getEyeHeight() / 2, 0), 10, .2, entity.getEyeHeight() / 2, .2, 0);
            ParticleUtil.spawnParticles(serverLevel, greenDustSmall, target.position().add(0, entity.getEyeHeight() / 2, 0), 10, .2, entity.getEyeHeight() / 2, .2, 0);

            if(random.nextInt(20) == 0) {
                target.hurt(target.damageSources().mobAttack(entity), (float) (DamageLookup.lookupDamage(5, .775f) * multiplier(entity)));
            }

            if(random.nextInt(25) == 0) {
                for(int i = 0; i < 5; i++) {
                    Vec3 entityPos = target.position();
                    BlockPos pos = BlockPos.containing(target.position().add(0, .75, 0));
                    BlockState state = serverLevel.getBlockState(pos);
                    if(state.getCollisionShape(serverLevel, pos).isEmpty()) {
                        target.teleportTo(entityPos.x, entityPos.y - .32, entityPos.z);
                    }
                }
            }

            if(random.nextInt(30) == 0) {
                Vec3 targetLoc = entity.position().add(0, .5, 0);
                Vec3 startPos = entity.position().add((random.nextDouble() - .5) * 9, 0, (random.nextDouble() - .5) * 9);

                float distance = (float) targetLoc.distanceTo(startPos);
                float bezierSteps = .15f / distance;

                List<Vec3> points = VectorUtil.createBezierCurve(startPos, targetLoc, bezierSteps, 5.5f, 5);

                ServerScheduler.scheduleForDuration(0, 0, 30, () -> {
                    for (Vec3 point : points) {
                        ParticleUtil.spawnParticles((ServerLevel) serverLevel, plantDust, point, 1, 0, 0);
                    }
                });

                serverLevel.playSound(null, entity, Blocks.GRASS_BLOCK.getSoundType(Blocks.GRASS_BLOCK.defaultBlockState(), serverLevel, BlockPos.containing(entity.position().x, entity.position().y, entity.position().z), null).getBreakSound(), SoundSource.BLOCKS, 5,1);
                LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 16, 2);
                if(targetEntity == null)
                    return;

                targetEntity.hurt(entity.damageSources().mobAttack(entity), (float) DamageLookup.lookupDamage(5, .85) * (float) multiplier(entity));
            }
        });
    }

    private void childOfOak(ServerLevel serverLevel, LivingEntity entity) {
        if (castingChildOfOak.remove(entity.getUUID())) {
            return;
        }

        castingChildOfOak.add(entity.getUUID());
        AtomicBoolean finished = new AtomicBoolean(false);

        ServerScheduler.scheduleUntil(serverLevel, () -> {
            if(entity.isDeadOrDying()) {
                castingChildOfOak.remove(entity.getUUID());
            }

            if(!castingChildOfOak.contains(entity.getUUID())) {
                finished.set(true);
                return;
            }

            ParticleUtil.spawnParticles(serverLevel, brownDustSmall, entity.position().add(0, entity.getEyeHeight() / 2, 0), 10, .2, entity.getEyeHeight() / 2, .2, 0);
            ParticleUtil.spawnParticles(serverLevel, greenDustSmall, entity.position().add(0, entity.getEyeHeight() / 2, 0), 10, .2, entity.getEyeHeight() / 2, .2, 0);

            BeyonderData.addModifierWithTimeLimit(entity, "child_of_oak", 1.25f, 1500);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1, false, false, false));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 0, false, false, false));

        }, 5, () -> castingChildOfOak.remove(entity.getUUID()), finished);
    }

    private void swamp(ServerLevel serverLevel, LivingEntity entity) {
        if(castingSwamp.contains(entity.getUUID())) {
            return;
        }
        final Vec3 startLoc = entity.position().add(0, .25, 0);

        final List<BlockPos> blocks = AbilityUtil.getBlocksInSphereRadius(serverLevel, startLoc, 25, true, true, true);
        blocks.removeIf(b -> {
            BlockState state = serverLevel.getBlockState(b);
            return state.getCollisionShape(serverLevel, b).isEmpty();
        });

        castingSwamp.add(entity.getUUID());

        ServerScheduler.scheduleForDuration(0, 2, 20 * 10, () -> {
            blocks.forEach(b -> {
                if(random.nextInt(6) != 0)
                    return;

                if(random.nextBoolean()) {
                    ParticleUtil.spawnParticles(serverLevel, brownDust, b.getCenter().add(0, .75, 0), 1, .16);
                }else {
                    ParticleUtil.spawnParticles(serverLevel, ModParticles.EARTHQUAKE.get(), b.getCenter().add(0, .75, 0), 1, .16);
                }
            });
            AbilityUtil.getNearbyEntities(entity, serverLevel, startLoc, 35).forEach(e -> {
                Vec3 entityPos = e.position();
                BlockPos pos = BlockPos.containing(e.position().add(0, .75, 0));
                BlockState state = serverLevel.getBlockState(pos);
                if(state.getCollisionShape(serverLevel, pos).isEmpty()) {
                    e.teleportTo(entityPos.x, entityPos.y - .32, entityPos.z);
                }
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 20, false, false, false));

            });
        }, () -> castingSwamp.remove(entity.getUUID()), serverLevel);
    }
}
