package de.jakob.lotm.abilities.demoness;

import com.google.common.util.concurrent.AtomicDouble;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.AddClientSideTagPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class PetrificationAbility extends SelectableAbility {
    public PetrificationAbility(String id) {
        super(id, 5);
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
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
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
                if(serverLevel.getBlockState(b).getDestroySpeed(serverLevel, b) >= 0)
                    serverLevel.setBlockAndUpdate(b, Blocks.STONE.defaultBlockState());
            });

            AbilityUtil.getAllNearbyEntities(entity, serverLevel, startPos, radius.get(), false).forEach(target -> {
                if(target instanceof LivingEntity living) {
                    if (AbilityUtil.isTargetSignificantlyWeaker(entity, living)) {
                        living.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 20 * 60 * 10, 9));
                        return;
                    } else if (AbilityUtil.isTargetSignificantlyStronger(entity, living)) {
                        living.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 20, 9));
                        return;
                    }

                    living.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 20 * 45, 9));
                }
                else {
                    target.getTags().add("petrified");
                    PacketHandler.sendToAllPlayersInSameLevel(new AddClientSideTagPacket("petrified", target.getId()), serverLevel);
                }
            });

            radius.addAndGet(0.5);
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

    private void petrifyTarget(ServerLevel serverLevel, LivingEntity entity) {
        ServerScheduler.scheduleForDuration(0, 2, 20 * 5, () -> {
            Entity target = AbilityUtil.getTargetEntityNonLivingIncluded(entity, 15, 2, false, false, false);
            if(target != null) {
                if(target instanceof LivingEntity livingTarget) {
                    int duration = 20 * 60 * 2;
                    if(AbilityUtil.isTargetSignificantlyStronger(entity, livingTarget)) {
                        duration = 20 * 2;
                    }
                    if(AbilityUtil.isTargetSignificantlyWeaker(entity, livingTarget)) {
                        duration = 20 * 60 * 10;
                    }
                    livingTarget.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, duration, 9, false, false));
                }
                else {
                    target.getTags().add("petrified");
                }
            }

            if(BeyonderData.isGriefingEnabled(entity)) {
                BlockPos targetPos = AbilityUtil.getTargetBlock(entity, 15, false);
                AbilityUtil.getBlocksInSphereRadius(serverLevel, targetPos.getCenter(), 2, true, true, false).forEach(b -> {
                    if(!serverLevel.getBlockState(b).isAir() && serverLevel.getBlockState(b).getDestroySpeed(serverLevel, b) >= 0) {
                        serverLevel.setBlockAndUpdate(b, Blocks.STONE.defaultBlockState());
                    }
                });
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }

}
