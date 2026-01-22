package de.jakob.lotm.abilities.mother;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LifeDeprivationAbility extends SelectableAbilityItem {
    public LifeDeprivationAbility(Properties properties) {
        super(properties, 3);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("mother", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.life_deprivation.target", "ability.lotmcraft.life_deprivation.area"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        switch (abilityIndex) {
            case 0 -> targetEntity(serverLevel, entity);
            case 1 -> targetArea(serverLevel, entity);
        }
    }

    private void targetArea(ServerLevel serverLevel, LivingEntity entity) {
        ArrayList<BlockPos> blocks = new ArrayList<>(AbilityUtil.getBlocksInEllipsoid(serverLevel, entity.position(), 55, 10, true, true, true));
        Collections.shuffle(blocks);

        int totalDuration = 20 * 3;
        int iterationsPerTick = (int) (blocks.size() / ((float) totalDuration));
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        ServerScheduler.scheduleForDuration(0, 1, totalDuration, () -> {
            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            for(int i = 0; i < iterationsPerTick; i++) {
                BlockPos blockPos = blocks.remove(0);
                if(blockPos == null) return;
                if(griefing) {
                    serverLevel.setBlockAndUpdate(blockPos, Blocks.SOUL_SOIL.defaultBlockState());
                }
                for(int j = 0; j < 2; j++) {
                    Vec3 particleDirection = entityPos.subtract(Vec3.atCenterOf(blockPos).add(0, .75, 0)).normalize();
                    Vec3 tempCenter = Vec3.atCenterOf(blockPos).add(0, .75, 0).add((random.nextDouble() - .5) * 0.5, (random.nextDouble() - .5) * 0.5, (random.nextDouble() - .5) * 0.5);
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL, tempCenter, 0, particleDirection.x, particleDirection.y, particleDirection.z, blockPos.getCenter().distanceTo(entityPos) / 20);
                }
            }
        });

        List<LivingEntity> targets = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 55);
        ServerScheduler.scheduleForDuration(0, 2, 50, () -> {
            for(LivingEntity target : targets) {
                target.hurt(target.damageSources().wither(), (float) (DamageLookup.lookupDps(3, .3, 2, 25) * multiplier(entity)));
                target.invulnerableTime = 0;

                Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2, 0);
                Vec3 casterCenter = entity.position().add(0, entity.getBbHeight() / 2, 0);
                for(int i = 0; i < 20; i++) {
                    Vec3 tempCenter = targetCenter.add((random.nextDouble() - .5) * target.getBbWidth(), (random.nextDouble() - .5) * target.getBbHeight(), (random.nextDouble() - .5) * target.getBbWidth());
                    Vec3 particleDirection = casterCenter.subtract(tempCenter).normalize();
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL, tempCenter, 0, particleDirection.x, particleDirection.y, particleDirection.z, tempCenter.distanceTo(casterCenter) / 20);
                }
            }
        });
    }

    private void targetEntity(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 20, 2);

        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.life_deprivation.no_target").withColor(0x8abd93));
            return;
        }

        ServerScheduler.scheduleForDuration(0, 2, 50, () -> {
            target.hurt(target.damageSources().wither(), (float) (DamageLookup.lookupDps(3, .8, 2, 25) * multiplier(entity)));
            target.invulnerableTime = 0;

            Vec3 targetCenter = target.position().add(0, target.getBbHeight() / 2, 0);
            Vec3 casterCenter = entity.position().add(0, entity.getBbHeight() / 2, 0);
            for(int i = 0; i < 20; i++) {
                Vec3 tempCenter = targetCenter.add((random.nextDouble() - .5) * target.getBbWidth(), (random.nextDouble() - .5) * target.getBbHeight(), (random.nextDouble() - .5) * target.getBbWidth());
                Vec3 particleDirection = casterCenter.subtract(tempCenter).normalize();
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL, tempCenter, 0, particleDirection.x, particleDirection.y, particleDirection.z, tempCenter.distanceTo(casterCenter) / 20);
            }
        });
    }
}
