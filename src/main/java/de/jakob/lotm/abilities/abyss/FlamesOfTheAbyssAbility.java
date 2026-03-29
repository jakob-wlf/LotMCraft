package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
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

public class FlamesOfTheAbyssAbility extends SelectableAbility {

    public FlamesOfTheAbyssAbility(String id) {
        super(id, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.flames_of_the_abyss.meteor_rain",
                "ability.lotmcraft.flames_of_the_abyss.abyss_pillars",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> meteorRain(level, entity);
            case 1 -> abyssPillars(level, entity);
        }
    }

    // ── Spell 1: Abyssal Meteor Rain ──────────────────────────────────────────

    private void meteorRain(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        Vec3 targetPos = AbilityUtil.getTargetLocation(entity, 80, 1.5f);
        boolean griefing = BeyonderData.isGriefingEnabled(entity);
        float damage = (float) (DamageLookup.lookupDamage(1, 0.85) * multiplier(entity));

        level.playSound(null, BlockPos.containing(entity.position()),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3f, 0.6f);

        // Stagger 5 meteors every 15 ticks
        for (int i = 0; i < 8; i++) {
            final int idx = i;
            ServerScheduler.scheduleDelayed(i * 15, () -> {
                // Slight random scatter around the target
                double ox = (random.nextDouble() - 0.5) * 14;
                double oz = (random.nextDouble() - 0.5) * 14;
                Vec3 landPos = targetPos.add(ox, 0, oz);

                MeteorEntity meteor = new MeteorEntity(serverLevel,
                        1.6f, damage, 2.5f,
                        entity, griefing, 16f, 10f);
                meteor.setColor(0.05f, 1.0f, 0.05f);
                meteor.setCustomColor(true);
                meteor.setAbyssImpact(false);
                meteor.setPosition(landPos);
                serverLevel.addFreshEntity(meteor);
            }, serverLevel);
        }
    }

    // ── Spell 2: Pillars of the Abyss ────────────────────────────────────────

    private void abyssPillars(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        level.playSound(null, BlockPos.containing(entity.position()),
                SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 3f, 0.5f);

        // Apply heavy debuffs to all nearby entities
        float damage = (float) (DamageLookup.lookupDamage(1, 0.6) * multiplier(entity));
        AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 18).forEach(e -> {
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 20, 4));          // Poison V
            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 6, 1));           // Wither II
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 3)); // Slowness IV
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 0));
            e.hurt(e.damageSources().mobAttack(entity), damage);
        });

        // First wave — 8 pillars in a ring around the caster
        for (int i = 0; i < 12; i++) {
            double angle = (i / 8.0) * Math.PI * 2;
            double dist = 3 + random.nextDouble() * 7;
            double px = entity.getX() + Math.cos(angle) * dist;
            double pz = entity.getZ() + Math.sin(angle) * dist;
            EffectManager.playEffect(EffectManager.Effect.ABYSS_PILLAR, px, entity.getY(), pz, serverLevel);
        }

        // Second wave — 5 random pillars, staggered
        ServerScheduler.scheduleDelayed(12, () -> {
            for (int i = 0; i < 20; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = random.nextDouble() * 16;
                double px = entity.getX() + Math.cos(angle) * dist;
                double pz = entity.getZ() + Math.sin(angle) * dist;
                EffectManager.playEffect(EffectManager.Effect.ABYSS_PILLAR, px, entity.getY(), pz, serverLevel);
            }
        }, serverLevel);
    }
}
