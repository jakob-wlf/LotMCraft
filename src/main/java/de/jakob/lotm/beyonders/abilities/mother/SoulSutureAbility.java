package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class SoulSutureAbility extends SelectableAbility {
    private static final DustParticleOptions THREAD = new DustParticleOptions(new Vector3f(0.67f, 0.95f, 0.74f), 1.1f);
    public SoulSutureAbility(String id) { super(id, 12); }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 8)); }
    @Override protected float getSpiritualityCost() { return 45; }
    @Override protected String[] getAbilityNames() { return new String[]{"ability.lotmcraft.soul_suture.mend", "ability.lotmcraft.soul_suture.sever"}; }
    @Override protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) { case 0 -> mend(serverLevel, entity); case 1 -> sever(serverLevel, entity); }
    }
    private void mend(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 2); if (target == null) target = entity;
        ParticleUtil.drawParticleLine(serverLevel, THREAD, entity.getEyePosition(), target.getEyePosition(), 18, 0.02);
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, target.getEyePosition(), 18, 0.35);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.75f, 1.6f);
        target.heal((float) (6 * multiplier(entity)));
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false, false));
        target.getActiveEffects().stream().map(MobEffectInstance::getEffect).filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL).limit(2).toList().forEach(target::removeEffect);
    }
    private void sever(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 12, 2);
        if (target == null) { AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.soul_suture.no_target").withColor(0x8ed38f)); return; }
        ParticleUtil.drawParticleLine(serverLevel, THREAD, entity.getEyePosition(), target.getEyePosition(), 24, 0.04);
        ServerScheduler.scheduleForDuration(0, 4, 24, () -> {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SOUL, target.getEyePosition(), 5, 0.3, 0.2, 0.3, 0.02);
            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.MOTHER_GENERIC, entity), (float) (DamageLookup.lookupDamage(8, 0.75) * multiplier(entity)));
            target.invulnerableTime = 0;
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, false, false));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(target.position(), serverLevel)));
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.PLAYERS, 1f, 0.5f);
    }
}
