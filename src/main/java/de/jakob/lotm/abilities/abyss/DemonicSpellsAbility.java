package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public class DemonicSpellsAbility extends SelectableAbility {
    private final Random random = new Random();
    private final DustParticleOptions greenDust = new DustParticleOptions(new Vector3f(0.2f, 0.8f, 0.2f), 1.2f);
    private final DustParticleOptions purpleDust = new DustParticleOptions(new Vector3f(0.6f, 0.2f, 0.8f), 1.2f);
    private final DustParticleOptions redDust = new DustParticleOptions(new Vector3f(0.9f, 0.2f, 0.2f), 1.2f);

    public DemonicSpellsAbility(String id) {
        super(id, 3f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.demonic_spells.acid_swamp",
                "ability.lotmcraft.demonic_spells.filthy_illusion",
                "ability.lotmcraft.demonic_spells.hellfire_wall"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        switch (abilityIndex) {
            case 0 -> castAcidSwamp(serverLevel, entity);
            case 1 -> castFilthyIllusion(serverLevel, entity);
            case 2 -> castHellfireWall(serverLevel, entity);
        }
    }

    private void castAcidSwamp(ServerLevel level, LivingEntity entity) {
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.ITEM_SLIME, entity.position().add(0, 0.5, 0), 12, 150);
        ParticleUtil.spawnSphereParticles(level, greenDust, entity.position().add(0, 0.5, 0), 12, 80);

        level.playSound(null, entity.blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, entity.getSoundSource(), 2f, 0.8f);

        double swampRadius = 15;
        double damage = DamageLookup.lookupDamage(4, 0.7) * multiplier(entity);

        AbilityUtil.getNearbyEntities(entity, level, entity.position(), swampRadius)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(entity, target))
                .forEach(target -> {
                    target.hurt(level.damageSources().magic(), (float) damage);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 6, 2, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 4, 1, false, false));
                });

        ServerScheduler.scheduleForDuration(0, 2, 60, () -> {
            for (int i = 0; i < 16; i++) {
                double angle = (i / 16.0) * Math.PI * 2;
                double x = entity.getX() + Math.cos(angle) * 15;
                double z = entity.getZ() + Math.sin(angle) * 15;
                ParticleUtil.spawnParticles(level, greenDust, new Vec3(x, entity.getY() + 0.5, z), 3, 1, 0.05);
            }
        }, level);
    }

    private void castFilthyIllusion(ServerLevel level, LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 8, 0, false, false));

        ParticleUtil.spawnSphereParticles(level, purpleDust, entity.position(), 8, 100);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.5f, 1.2f);

        LivingEntity targetEntity = AbilityUtil.getTargetEntity(entity, 30, 2.0f);

        if (targetEntity == null) {
            Vec3 clonePos = entity.position().add(entity.getLookAngle().scale(5));
            createClone(level, entity, clonePos, null);
        } else {
            createClone(level, entity, entity.position(), targetEntity);
        }
    }

    private void createClone(ServerLevel level, LivingEntity caster, Vec3 startPos, LivingEntity target) {
        ServerScheduler.scheduleForDuration(0, 1, 20 * 5, () -> {
            if (target != null && target.isAlive()) {
                double distance = startPos.distanceTo(target.position());
                if (distance < 2) {
                    explodeClone(level, caster, target.position());
                    return;
                }
            }
            ParticleUtil.spawnParticles(level, purpleDust, startPos, 2, 0.5, 0.05);
        }, () -> {
            if (target == null) {
                explodeClone(level, caster, startPos);
            }
        }, level);
    }

    private void explodeClone(ServerLevel level, LivingEntity caster, Vec3 explosionPos) {
        ParticleUtil.spawnSphereParticles(level, purpleDust, explosionPos, 6, 200);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.EXPLOSION, explosionPos, 5, 80);
        level.playSound(null, explosionPos.x, explosionPos.y, explosionPos.z, SoundEvents.GENERIC_EXPLODE, caster.getSoundSource(), 1.5f, 1.0f);

        double explosionDamage = DamageLookup.lookupDamage(4, 0.65) * multiplier(caster);
        AbilityUtil.getNearbyEntities(caster, level, explosionPos, 15)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(caster, target))
                .forEach(target -> {
                    target.hurt(level.damageSources().magic(), (float) explosionDamage);
                    Vec3 knockback = target.position().subtract(explosionPos).normalize().scale(1.5);
                    target.setDeltaMovement(target.getDeltaMovement().add(knockback));
                    target.hurtMarked = true;
                });
    }

    private void castHellfireWall(ServerLevel level, LivingEntity entity) {
        double wallRadius = 13;
        int particleCount = 60;
        double damage = DamageLookup.lookupDamage(4, 0.6) * multiplier(entity);

        final double centerX = entity.getX();
        final double centerY = entity.getY();
        final double centerZ = entity.getZ();
        final Vec3 center = new Vec3(centerX, centerY, centerZ);

        level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_AMBIENT, entity.getSoundSource(), 2.0f, 0.9f);

        List<BlockPos> wallBlocks = new ArrayList<>();
        int blockCount = (int)(2 * Math.PI * wallRadius * 2);

        for (int i = 0; i < blockCount; i++) {
            double angle = (i / (double) blockCount) * Math.PI * 2;
            int bx = (int) Math.round(centerX + Math.cos(angle) * wallRadius);
            int bz = (int) Math.round(centerZ + Math.sin(angle) * wallRadius);

            for (int height = 0; height < 30; height++) {
                BlockPos pos = new BlockPos(bx,(int) Math.floor(centerY) + height, bz);
                if (!level.getBlockState(pos).isSolid()) {
                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                    wallBlocks.add(pos);
                }
            }
        }

        for (int i = 0; i < particleCount; i++) {
            double angle = (i / (double) particleCount) * Math.PI * 2;
            double x = entity.getX() + Math.cos(angle) * wallRadius;
            double z = entity.getZ() + Math.sin(angle) * wallRadius;
            ParticleUtil.spawnParticles(level, redDust, new Vec3(x, entity.getY() + 2, z), 5, 0.5, 0.1);
        }

        ServerScheduler.scheduleForDuration(0, 5, 20 * 8, () -> {
            for (int i = 0; i < particleCount; i++) {
                double angle = (i / (double) particleCount) * Math.PI * 2;
                double x = entity.getX() + Math.cos(angle) * wallRadius;
                double z = entity.getZ() + Math.sin(angle) * wallRadius;
                ParticleUtil.spawnParticles(level, redDust, new Vec3(x, centerY + 1, z), 2, 0.2, 0.05);
                ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, new Vec3(x, centerY + 2, z), 3, 0.3, 0.1);
            }

            AbilityUtil.getNearbyEntities(entity, level, center, wallRadius + 2)
                    .stream()
                    .filter(target -> AbilityUtil.mayDamage(entity, target))
                    .forEach(target -> {
                        double dx = target.getX() - centerX;
                        double dz = target.getZ() - centerZ;
                        double distFromCenter = Math.sqrt(dx * dx + dz * dz);
                        if (Math.abs(distFromCenter - wallRadius) < 2) {
                            target.hurt(level.damageSources().magic(), (float) damage);
                            target.setRemainingFireTicks(60);

                        }
                    });
        }, level);

        ServerScheduler.scheduleDelayed(20 * 8 + 1, () -> {
            for (BlockPos pos : wallBlocks) {
                if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                    level.removeBlock(pos, false);
                }
            }
        }, level);
    }
}