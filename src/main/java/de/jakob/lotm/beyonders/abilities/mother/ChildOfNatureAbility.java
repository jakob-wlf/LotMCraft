package de.jakob.lotm.beyonders.abilities.mother;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.damage.ModDamageTypes;
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
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ChildOfNatureAbility extends SelectableAbility {
    public ChildOfNatureAbility(String id) { super(id, 15); canBeShared = false; }
    @Override public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("mother", 1)); }
    @Override protected float getSpiritualityCost() { return 2400; }
    @Override protected String[] getAbilityNames() { return new String[]{"ability.lotmcraft.child_of_nature.domain", "ability.lotmcraft.child_of_nature.traverse"}; }
    @Override protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        switch (abilityIndex) { case 0 -> domain(serverLevel, entity); case 1 -> traverse(serverLevel, entity); }
    }
    private void domain(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 center = entity.position(); serverLevel.playSound(null, entity.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2f, 0.7f);
        ServerScheduler.scheduleForDuration(0, 5, 120, () -> {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SPORE_BLOSSOM_AIR, center.add(0, 2, 0), 24, 8, 2.5, 8, 0.01);
            AbilityUtil.getNearbyEntities(entity, serverLevel, center, 18).forEach(target -> {
                if (target == entity) { target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 2, false, false, false)); target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false, false)); return; }
                if (target.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) { target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.MOTHER_GENERIC, entity), (float) (5 * multiplier(entity))); target.invulnerableTime = 0; } else if (!AbilityUtil.areAllied(entity, target)) { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false, false)); }
            });
        }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(center, serverLevel)));
    }
    private void traverse(ServerLevel serverLevel, LivingEntity entity) {
        Vec3 target = AbilityUtil.getTargetLocation(entity, 48, 1.5f, false); BlockPos pos = BlockPos.containing(target);
        for (int i = 0; i < 8; i++) { BlockPos test = pos.above(i); if (serverLevel.getBlockState(test).getCollisionShape(serverLevel, test).isEmpty() && serverLevel.getBlockState(test.above()).getCollisionShape(serverLevel, test.above()).isEmpty()) { Vec3 startEye = entity.getEyePosition(); ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, startEye, 26, 0.45); entity.teleportTo(test.getX() + 0.5, test.getY(), test.getZ() + 0.5); ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SPORE_BLOSSOM_AIR, entity.getEyePosition(), 26, 0.45); serverLevel.playSound(null, test, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1f, 0.8f); AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.child_of_nature.traverse_notice").withColor(0x8ed38f)); return; } }
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.child_of_nature.invalid").withColor(0x8ed38f));
    }
}
