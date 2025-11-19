package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.A;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class PetrificationAbility extends SelectableAbilityItem {
    public PetrificationAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.petrification.target", "ability.lotmcraft.petrification.area"};
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if(!(entity instanceof Player)) {
            abilityIndex = 0;
        }

        switch (abilityIndex) {
            case 0 -> petrifyTarget(serverLevel, entity);
            case 1 -> petrifyArea(serverLevel, entity);
        }
    }

    private void petrifyArea(ServerLevel serverLevel, LivingEntity entity) {
        if(!BeyonderData.isGriefingEnabled(entity)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.petrification.griefing_disabled").withColor(0x7532a8));
            return;
        }

        AtomicDouble radius = new AtomicDouble(0.5);
        Vec3 startPos = entity.position();

        ServerScheduler.scheduleForDuration(0, 1, 120, () -> {
            AbilityUtil.getBlocksInSphereRadius(serverLevel, startPos, radius.get(), true, true, false).forEach(b -> {
                serverLevel.setBlockAndUpdate(b, Blocks.STONE.defaultBlockState());
            });

            AbilityUtil.getNearbyEntities(entity, serverLevel, startPos, radius.get(), false).forEach(e -> {
                if(AbilityUtil.isTargetSignificantlyWeaker(entity, e)) {
                    e.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 20 * 60 * 10, 9));
                    return;
                }
                else if(AbilityUtil.isTargetSignificantlyStronger(entity, e)) {
                    e.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 20, 9));
                    return;
                }

                entity.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 20 * 45, 9));
            });

            radius.addAndGet(0.5);
        });
    }

    private void petrifyTarget(ServerLevel serverLevel, LivingEntity entity) {
        ServerScheduler.scheduleForDuration(0, 2, 20 * 5, () -> {
            LivingEntity target = AbilityUtil.getTargetEntity(entity, 15, 2);
            if(target != null) {
                int duration = 20 * 60 * 2;
                if(AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
                    duration = 20 * 2;
                }
                if(AbilityUtil.isTargetSignificantlyWeaker(entity, target)) {
                    duration = 20 * 60 * 10;
                }
                target.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, duration, 9, false, false));
            }

            if(BeyonderData.isGriefingEnabled(entity)) {
                BlockPos targetPos = AbilityUtil.getTargetBlock(entity, 15, false);
                AbilityUtil.getBlocksInSphereRadius(serverLevel, targetPos.getCenter(), 2, true, true, false).forEach(b -> {
                    if(!serverLevel.getBlockState(b).isAir()) {
                        serverLevel.setBlockAndUpdate(b, Blocks.STONE.defaultBlockState());
                    }
                });
            }
        });
    }

}
