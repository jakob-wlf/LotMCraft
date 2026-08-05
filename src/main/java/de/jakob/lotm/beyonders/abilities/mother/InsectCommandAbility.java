package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class InsectCommandAbility extends SelectableAbility {
    public InsectCommandAbility(String id) { super(id, 9); }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 7)); }
    @Override protected float getSpiritualityCost() { return 65; }
    @Override protected String[] getAbilityNames() { return new String[]{"ability.lotmcraft.insect_command.swarm", "ability.lotmcraft.insect_command.ward"}; }
    @Override protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) { case 0 -> swarm(serverLevel, entity); case 1 -> ward(serverLevel, entity); }
    }
    private void swarm(ServerLevel serverLevel, LivingEntity entity) {
        LivingEntity target = AbilityUtil.getTargetEntity(entity, 18, 2);
        if (target == null) { AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.insect_command.no_target").withColor(0x8ed38f)); return; }
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.BEE_LOOP, SoundSource.PLAYERS, 1f, 0.6f);
        ServerScheduler.scheduleForDuration(0, 3, 60, () -> {
            Vec3 pos = target.getEyePosition();
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.CRIT, pos, 10, 0.45, 0.55, 0.45, 0.08);
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SPORE_BLOSSOM_AIR, pos, 4, 0.45, 0.55, 0.45, 0.02);
            target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.MOTHER_GENERIC, entity), (float) (DamageLookup.lookupDps(6, 0.35, 3, 20) * multiplier(entity)));
            target.invulnerableTime = 0;
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, false));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(target.position(), serverLevel)));
    }
    private void ward(ServerLevel serverLevel, LivingEntity entity) {
        serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEEHIVE_WORK, SoundSource.PLAYERS, 1f, 1.1f);
        ServerScheduler.scheduleForDuration(0, 5, 100, () -> {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.HAPPY_VILLAGER, entity.getEyePosition(), 6, 0.55, 0.75, 0.55, 0.02);
            AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 7).forEach(target -> {
                if (target == entity) return;
                Vec3 knockback = target.position().subtract(entity.position()).normalize().scale(0.35);
                target.setDeltaMovement(target.getDeltaMovement().add(knockback.x, 0.12, knockback.z));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false, false));
            });
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, false, false));
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(entity.position(), serverLevel)));
    }
}
