package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class RainCallingAbility extends SelectableAbility {
    public RainCallingAbility(String id) { super(id, 8); }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 7)); }
    @Override protected float getSpiritualityCost() { return 80; }
    @Override protected String[] getAbilityNames() { return new String[]{"ability.lotmcraft.rain_calling.summon", "ability.lotmcraft.rain_calling.clear"}; }
    @Override protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) { case 0 -> summonRain(serverLevel, entity); case 1 -> clearSky(serverLevel, entity); }
    }
    private void summonRain(ServerLevel serverLevel, LivingEntity entity) {
        serverLevel.setWeatherParameters(0, 20 * 60 * 4, true, true);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 0.8f, 1.6f);
        AbilityUtil.addPotionEffectToNearbyEntities(serverLevel, entity, 16, entity.position(), new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, false));
        boolean griefing = BeyonderData.isGriefingEnabled(entity); Vec3 center = entity.position();
        ServerScheduler.scheduleForDuration(0, 20, 120, () -> {
            for (BlockPos pos : AbilityUtil.getBlocksInCircle(serverLevel, center, 8 * multiplier(entity), 40)) {
                ParticleUtil.spawnParticles(serverLevel, ParticleTypes.RAIN, Vec3.atCenterOf(pos).add(0, 2.2, 0), 2, 0.3, 0.2, 0.3, 0.02);
                if (!griefing) continue;
                BlockState state = serverLevel.getBlockState(pos);
                if (state.getBlock() instanceof BonemealableBlock bonemealableBlock) { var random = net.minecraft.util.RandomSource.create(); if (bonemealableBlock.isBonemealSuccess(serverLevel, random, pos, state)) bonemealableBlock.performBonemeal(serverLevel, net.minecraft.util.RandomSource.create(), pos, state); }
            }
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
    }
    private void clearSky(ServerLevel serverLevel, LivingEntity entity) {
        serverLevel.setWeatherParameters(20 * 20, 0, false, false);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, entity.getEyePosition(), 30, 0.8);
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.WEATHER, 0.8f, 1.5f);
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.rain_calling.clear_notice").withColor(0x8ed38f));
    }
}
