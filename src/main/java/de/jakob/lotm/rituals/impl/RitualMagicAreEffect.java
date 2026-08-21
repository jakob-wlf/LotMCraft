package de.jakob.lotm.rituals.impl;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.LightningEntity;
import de.jakob.lotm.entity.custom.ability_entities.tyrant_pathway.StrongLightningEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rituals.RitualManager;
import de.jakob.lotm.rituals.RitualResultHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

public class RitualMagicAreEffect implements RitualResultHandler {

    @Override
    public void perform(Map<String, Object> params, ServerPlayer player) {
        AreaEffectResult result = deserializeParams(params, AreaEffectResult.class);
        if (result == null) return;

        for (AreaEffectResult.AreaEffectEntry effect : result.effects()) {
            List<LivingEntity> targets = getTargetEntity(effect.target(), player, effect.maxDistance());
            for (LivingEntity target : targets) {
                applyAreaEffect(effect.effect(), target, player, Math.clamp(effect.power(), 1, 10));
            }
        }
    }

    private void applyAreaEffect(String effect, LivingEntity target, ServerPlayer source, int power) {
        switch(effect) {
            case "lightning" -> {
                if(power <= 6) {
                    LightningEntity lightning = new LightningEntity(
                            target.level(),
                            source,
                            target.position(),
                            50,
                            6,
                            2 * power * power,
                            false,
                            4,
                            200,
                            0x11A8DD
                    );
                    target.level().addFreshEntity(lightning);
                }
                else {
                    StrongLightningEntity strongLightning = new StrongLightningEntity(
                            target.level(),
                            source,
                            target.position(),
                            50,
                            6,
                            2 * power * power,
                            false,
                            4,
                            200,
                            0xe0ac00
                    );
                    target.level().addFreshEntity(strongLightning);
                }
            }
            case "ignite" -> {
                ParticleUtil.spawnParticles(
                        source.serverLevel(),
                        ParticleTypes.FLAME,
                        target.getEyePosition().subtract(0, target.getEyeHeight() / 2, 0),
                        80,
                        .2,
                        target.getEyeHeight() / 2,
                        .2,
                        0
                );
                ParticleUtil.spawnParticles(
                        source.serverLevel(),
                        ParticleTypes.SMOKE,
                        target.getEyePosition().subtract(0, target.getEyeHeight() / 2, 0),
                        80,
                        .2,
                        target.getEyeHeight() / 2,
                        .2,
                        0
                );

                target.setRemainingFireTicks(target.getRemainingFireTicks() + (20 * 2 * power * power));
                target.hurt(target.damageSources().onFire(), 2 * power * power);
            }
            case "bloom" -> {
                List<BlockPos> blocks = AbilityUtil.getBlocksInEllipsoid(
                        source.serverLevel(),
                        source.position(),
                        7 * power,
                        2 * power,
                        true,
                        true,
                        true
                        );

                for (BlockPos blockPos : blocks) {
                    ParticleUtil.spawnParticles(
                            source.serverLevel(),
                            ParticleTypes.HAPPY_VILLAGER,
                            blockPos.above().getCenter().subtract(0, .4, 0),
                            2,
                            .2,
                            .2,
                            .2,
                            0
                    );

                    BlockState blockState = source.level().getBlockState(blockPos);
                    if(!(blockState.getBlock() instanceof BonemealableBlock bonemealableBlock)) continue;
                    if(bonemealableBlock.isBonemealSuccess(source.level(), RandomSource.create(), blockPos, blockState))
                        bonemealableBlock.performBonemeal((ServerLevel) source.level(), RandomSource.create(), blockPos, blockState);
                }
            }
            case "cleanse" -> {
                EffectManager.playEffect(EffectManager.Effect.SPIRITUAL_BAPTISM, target.getX(), target.getY(), target.getZ(), source.serverLevel());
                target.removeAllEffects();
                target.setRemainingFireTicks(0);
                target.setAirSupply(target.getMaxAirSupply());
                target.setAbsorptionAmount(target.getMaxAbsorption());
            }
        }
    }

    public record AreaEffectResult(List<AreaEffectEntry> effects) {

        public record AreaEffectEntry(
                String effect,
                String target,
                @SerializedName("max_distance") int maxDistance,
                int power // from 1 to 10, where 1 is the weakest and 10 is the strongest
        ) {}
    }

    public static <T> T deserializeParams(Map<String, Object> params, Class<T> type) {
        JsonElement json = RitualManager.GSON.toJsonTree(params);
        return RitualManager.GSON.fromJson(json, type);
    }
}
