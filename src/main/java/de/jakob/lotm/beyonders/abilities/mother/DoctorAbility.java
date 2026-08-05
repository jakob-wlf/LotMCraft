package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DoctorAbility extends SelectableAbility {
    public DoctorAbility(String id) { super(id, 12); }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 8)); }
    @Override protected float getSpiritualityCost() { return 35; }
    @Override protected String[] getAbilityNames() { return new String[]{"ability.lotmcraft.doctor.diagnose", "ability.lotmcraft.doctor.revitalize"}; }
    @Override protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) { case 0 -> diagnose(serverLevel, entity); case 1 -> revitalize(serverLevel, entity); }
    }
    private void diagnose(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 14, 2); if (target == null) target = entity;
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9f, 1.35f);
        ParticleUtil.spawnParticles(serverLevel, ModParticles.HEALING.get(), target.getEyePosition(), 20, 0.55);
        int harmfulEffects = 0; for (MobEffectInstance effect : target.getActiveEffects()) if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) harmfulEffects++;
        int missingHealth = Math.max(0, (int) Math.ceil(target.getMaxHealth() - target.getHealth()));
        int color = 0x8ed38f;
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.doctor.diagnose.result", target.getName(), missingHealth, harmfulEffects).withColor(color));
        if (target instanceof Player player && !player.getUUID().equals(entity.getUUID())) player.displayClientMessage(Component.translatable("ability.lotmcraft.doctor.diagnose.notice", entity.getName()).withColor(color), true);
    }
    private void revitalize(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 14, 2); if (target == null) target = entity;
        target.heal((float) (4.0f * multiplier(entity)));
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 1, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0, false, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, false, false));
        ParticleUtil.spawnParticles(serverLevel, ModParticles.HEALING.get(), target.getEyePosition(), 40, 0.75);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 1.2f);
    }
}
