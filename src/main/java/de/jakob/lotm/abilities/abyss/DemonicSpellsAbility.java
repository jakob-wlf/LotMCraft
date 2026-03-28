package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.AvatarEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

public class DemonicSpellsAbility extends SelectableAbility {
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
        Vec3 swampCenter = entity.position();

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.ITEM_SLIME, swampCenter.add(0, 0.5, 0), 12, 150);
        ParticleUtil.spawnSphereParticles(level, greenDust, swampCenter.add(0, 0.5, 0), 12, 80);

        level.playSound(null, entity.blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, entity.getSoundSource(), 2f, 0.8f);

        double swampRadius = 15;
        double damage = DamageLookup.lookupDamage(4, 0.7) * multiplier(entity);

        AbilityUtil.damageNearbyEntities(level, entity, swampRadius, damage, swampCenter, true, false);

        AbilityUtil.getNearbyEntities(entity, level, swampCenter, swampRadius)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(entity, target))
                .forEach(target -> {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 6, 2, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 4, 1, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 2, false, false));
                });

        EffectManager.playEffect(EffectManager.Effect.ACID_SWAMP, swampCenter.x, swampCenter.y, swampCenter.z, level);
    }

    private void castFilthyIllusion(ServerLevel level, LivingEntity entity) {
        Vec3 clonePos = entity.position();

        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 8, 0, false, false));

        ParticleUtil.spawnSphereParticles(level, purpleDust, clonePos, 8, 100);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                entity.getSoundSource(), 1.5f, 1.2f);

        String pathway = BeyonderData.getPathway(entity);
        int sequence = BeyonderData.getSequence(entity);
        AvatarEntity clone = new AvatarEntity(ModEntities.ERROR_AVATAR.get(), level,
                entity.getUUID(), pathway, sequence);
        clone.setPos(clonePos.x, clonePos.y, clonePos.z);
        level.addFreshEntity(clone);

        AbilityUtil.getNearbyEntities(entity, level, clonePos, 20).forEach(e -> {
            if (e instanceof Mob mob && mob.getTarget() != null
                    && mob.getTarget().getUUID().equals(entity.getUUID())) {
                mob.setTarget(clone);
            }
        });

        ServerScheduler.scheduleDelayed(20 * 5, () -> {
            if (clone.isAlive()) {
                explodeClone(level, entity, clone.position());
                clone.discard();
            }
        }, level);
    }

    private void explodeClone(ServerLevel level, LivingEntity caster, Vec3 explosionPos) {
        ParticleUtil.spawnSphereParticles(level, purpleDust, explosionPos, 6, 200);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.EXPLOSION, explosionPos, 5, 80);
        level.playSound(null, explosionPos.x, explosionPos.y, explosionPos.z, SoundEvents.GENERIC_EXPLODE, caster.getSoundSource(), 1.5f, 1.0f);

        double explosionDamage = DamageLookup.lookupDamage(4, 0.65) * multiplier(caster);
        AbilityUtil.damageNearbyEntities(level, caster, 15, explosionDamage, explosionPos, true, false);
        AbilityUtil.getNearbyEntities(caster, level, explosionPos, 15)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(caster, target))
                .forEach(target -> {
                    Vec3 knockback = target.position().subtract(explosionPos).normalize().scale(1.5);
                    target.setDeltaMovement(target.getDeltaMovement().add(knockback));
                    target.hurtMarked = true;
                });
    }

    private void castHellfireWall(ServerLevel level, LivingEntity entity) {
        double wallRadius = 13;
        double damage = DamageLookup.lookupDamage(4, 0.6) * multiplier(entity);

        final double centerX = entity.getX();
        final double centerY = entity.getY();
        final double centerZ = entity.getZ();
        final Vec3 center = new Vec3(centerX, centerY, centerZ);

        level.playSound(null, entity.blockPosition(), SoundEvents.FIRE_AMBIENT, entity.getSoundSource(), 2.0f, 0.9f);

        final List<BlockPos> wallBlocks = new ArrayList<>();
        int blockCount = (int)(2 * Math.PI * wallRadius * 2);

        for (int i = 0; i < blockCount; i++) {
            double angle = (i / (double) blockCount) * Math.PI * 2;
            int bx = (int) Math.round(centerX + Math.cos(angle) * wallRadius);
            int bz = (int) Math.round(centerZ + Math.sin(angle) * wallRadius);

            for (int height = 0; height < 18; height++) {
                BlockPos pos = new BlockPos(bx,(int) Math.floor(centerY) + height, bz);
                if (!level.getBlockState(pos).isSolid()) {
                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 3);
                    wallBlocks.add(pos);
                }
            }
        }

        ServerScheduler.scheduleForDuration(0, 5, 20 * 8, () -> {
            for (BlockPos pos : wallBlocks) {
                if (level.getBlockState(pos).is(Blocks.BARRIER)) {
                    ParticleUtil.spawnParticles(level, redDust, pos.getCenter(), 2, 0.2, 0.05);
                    ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, pos.getCenter(), 1, 0.3, 0.1);
                }
            }

            AbilityUtil.damageNearbyEntities(level, entity, wallRadius - 2, wallRadius + 2, damage, center, true, false, true, 0, 60);
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