package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.ability_entities.MeteorEntity;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
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
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, targetPos, entity, this, new String[]{"explosion", "burning"}, 12, 20 * 10));
    }

    // ── Spell 2: Pillars of the Abyss ────────────────────────────────────────

    private void abyssPillars(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        level.playSound(null, BlockPos.containing(entity.position()),
                SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 3f, 0.5f);

        float damage = (float) (DamageLookup.lookupDamage(1, 0.4) * multiplier(entity));

        List<Vec3> pillarPositions = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * Math.PI * 2;
            double dist = 3 + random.nextDouble() * 7;
            Vec3 candidate = new Vec3(
                    entity.getX() + Math.cos(angle) * dist,
                    entity.getY(),
                    entity.getZ() + Math.sin(angle) * dist
            );
            BlockPos ground = findGroundPos(level, candidate);
            pillarPositions.add(new Vec3(ground.getX(), ground.getY(), ground.getZ()));
        }

        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = random.nextDouble() * 16;
            Vec3 candidate = new Vec3(
                    entity.getX() + Math.cos(angle) * dist,
                    entity.getY(),
                    entity.getZ() + Math.sin(angle) * dist
            );
            BlockPos ground = findGroundPos(level, candidate);
            pillarPositions.add(new Vec3(ground.getX(), ground.getY(), ground.getZ()));
        }

        for (int i = 0; i < pillarPositions.size(); i++) {
            Vec3 pos = pillarPositions.get(i);

            int delay = i * 2;

            ServerScheduler.scheduleDelayed(delay, () -> {
                EffectManager.playEffect(EffectManager.Effect.ABYSS_PILLAR, pos.x, pos.y, pos.z, serverLevel);
            }, serverLevel);

            ServerScheduler.scheduleForDuration(delay, 4, 20 * 7, () -> {
                AbilityUtil.damageNearbyEntities(serverLevel, entity, 1.5, damage, pos, true, false);
                AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), 1.5).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 20, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 6, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 0));
                });
            }, null, serverLevel, () -> AbilityUtil.getTimeInArea(entity, new Location(pos, serverLevel)));
        }

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent((ServerLevel) level, entity.position(), entity, this,
                new String[]{"corruption", "burning"}, 7, 20 * 3));
    }

    private BlockPos findGroundPos(Level level, Vec3 xzPos) {
        int startY = (int) Math.floor(xzPos.y) + 4;
        for (int dy = 0; dy <= 8; dy++) {
            BlockPos check = new BlockPos((int) Math.floor(xzPos.x), startY - dy, (int) Math.floor(xzPos.z));
            BlockPos below = check.below();
            if (level.getBlockState(below).isSolidRender(level, below)) {
                return check;
            }
        }
        return new BlockPos((int) Math.floor(xzPos.x), startY, (int) Math.floor(xzPos.z));
    }
}
