package de.jakob.lotm.rituals.impl;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import de.jakob.lotm.rituals.RitualManager;
import de.jakob.lotm.rituals.RitualResultHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;
import java.util.Map;

public class RitualMagicPotionEffect implements RitualResultHandler {

    @Override
    public void perform(Map<String, Object> params, ServerPlayer player) {
        PotionEffectResult result = deserializeParams(params, PotionEffectResult.class);

        for (PotionEffectResult.EffectEntry effect : result.effects()) {
            ResourceLocation id = ResourceLocation.parse(effect.id());
            Holder<MobEffect> mobEffect = BuiltInRegistries.MOB_EFFECT
                    .getHolder(id)
                    .orElseThrow();

            player.addEffect(new MobEffectInstance(
                    mobEffect,
                    effect.durationTicks(),
                    effect.amplifier()
            ));
        }
    }

    public record PotionEffectResult(List<EffectEntry> effects) {

        public record EffectEntry(
                String id,
                @SerializedName("duration_ticks") int durationTicks,
                int amplifier
        ) {}
    }

    public static <T> T deserializeParams(Map<String, Object> params, Class<T> type) {
        JsonElement json = RitualManager.GSON.toJsonTree(params);
        return RitualManager.GSON.fromJson(json, type);
    }
}
