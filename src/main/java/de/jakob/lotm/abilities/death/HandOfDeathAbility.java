package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HandOfDeathAbility extends SelectableAbility {

    private static final int WITHER_DURATION  = 20 * 30;
    private static final int EFFECT_AMPLIFIER = 1;

    private static final Map<UUID, UUID> activeMarks = new ConcurrentHashMap<>();

    public HandOfDeathAbility(String id) {
        super(id, 60f);
        canBeCopied = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 2000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.hand_of_death.left",
                "ability.lotmcraft.hand_of_death.right_self",
                "ability.lotmcraft.hand_of_death.right_others"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        switch (abilityIndex) {
            case 0 -> leftHand(serverLevel, entity);
            case 1 -> rightHandSelf(serverLevel, entity);
            case 2 -> rightHandOthers(serverLevel, entity);
        }
    }

    private void leftHand(ServerLevel level, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 30 * (int) Math.max(multiplier(caster) / 4, 1), 1.5f, true);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_death.no_target").withColor(0xFF334f23));
            return;
        }

        UUID existing = activeMarks.remove(target.getUUID());
        if (existing != null) ServerScheduler.cancel(existing);

        target.addEffect(new MobEffectInstance(MobEffects.WITHER,    WITHER_DURATION, EFFECT_AMPLIFIER, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,  WITHER_DURATION, EFFECT_AMPLIFIER, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, WITHER_DURATION, EFFECT_AMPLIFIER, false, true, true));

        Vec3 center = target.position().add(0, 1, 0);
        Vec3 feet   = target.position();

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, center, 4.5, 140);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, center, 3.0, 100);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 1.2, 50, 0.35);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, center, 2.5, 80, 0.20);

        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet,               3.5, 56);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet.add(0, 1,  0), 3.0, 48);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet.add(0, 2,  0), 2.5, 40);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, feet.add(0, 3,  0), 1.5, 28);

        ParticleUtil.createParticleSpirals(level, ParticleTypes.SOUL, center, 0.4, 2.8, 5.0, 1.5, 2.0, 60, 3, 7);

        ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, center, 25, 1.2, 0.05);
        ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE,        center, 40, 2.0, 0.02);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_AMBIENT,  SoundSource.PLAYERS, 1.8f, 0.45f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS, 1.2f, 0.55f);

        int   casterSeq        = BeyonderData.getSequence(caster);
        int   targetSeq        = BeyonderData.getSequence(target);
        int   seqDiff          = targetSeq - casterSeq;
        float damageMultiplier = Math.max(0f, 0.25f + (seqDiff * 0.10f));

        UUID taskId = ServerScheduler.scheduleDelayed(WITHER_DURATION, () -> {
            activeMarks.remove(target.getUUID());
            if (!target.isAlive()) return;

            Vec3 dc = target.position().add(0, 1, 0);
            Vec3 df = target.position();

            ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, dc, 1.5, 70, 0.5f);
            ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, dc, 2.5, 50, 0.3f);
            ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, dc, 20, 1.0, 0.1);

            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.5f, 0.55f);

            ServerScheduler.scheduleDelayed(5, () -> {
                ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL,           dc, 4.0, 120, 0.55f);
                ParticleUtil.spawnSphereParticles(level, ParticleTypes.REVERSE_PORTAL, dc, 4.5,  80);

                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, df,               4.5, 64);
                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, dc,               4.0, 56);
                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, dc.add(0, 2,  0), 3.0, 44);
                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, dc.add(0, 4,  0), 2.0, 32);

                ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, dc, 35, 2.5, 0.05);

                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.2f, 1.0f);
            }, level);

            ServerScheduler.scheduleDelayed(12, () -> {
                ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, dc, 7.0, 200, 0.9f);
                ParticleUtil.spawnSphereParticles(level, ParticleTypes.SOUL, dc, 9.0, 100, 0.6f);

                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, df.add(0, -0.5, 0), 9.0, 96);
                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, dc.add(0,  3,   0), 6.0, 72);
                ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, dc.add(0,  6,   0), 3.5, 48);

                ParticleUtil.createParticleSpirals(level, ParticleTypes.SOUL, dc, 1.0, 6.5, 8.0, 2.5, 1.8, 50, 4, 5);

                ParticleUtil.spawnParticles(level, ParticleTypes.SMOKE,       dc, 80, 5.0, 0.03);
                ParticleUtil.spawnParticles(level, ParticleTypes.LARGE_SMOKE, dc, 40, 3.5, 0.02);

                level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0f, 0.6f);
            }, level);

            float damage = target.getMaxHealth() * damageMultiplier;
            ModDamageTypes.trueDamage(target, damage, level, caster);
        }, level);

        activeMarks.put(target.getUUID(), taskId);
        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_death.left_applied").withColor(0xFF334f23));
    }

    private void rightHandSelf(ServerLevel level, LivingEntity caster) {
        float heal = caster.getMaxHealth() * 0.25f;
        caster.heal(heal);

        Vec3 center = caster.position().add(0, 1, 0);
        Vec3 feet   = caster.position();

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.TOTEM_OF_UNDYING, center, 1.0, 60, 0.3f);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.TOTEM_OF_UNDYING, center, 2.2, 50, 0.2f);

        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, feet,               2.5, 36);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, feet.add(0, 1,  0), 2.0, 28);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, feet.add(0, 2,  0), 1.5, 20);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, feet.add(0, 3,  0), 0.8, 12);

        ParticleUtil.createParticleCocoons(ParticleTypes.TOTEM_OF_UNDYING,
                new Location(caster.position(), level), 0.5, 1.4, 3.5, 1.5, 2.0, 50, 2, 8);

        ParticleUtil.createParticleSpirals(level, ParticleTypes.TOTEM_OF_UNDYING, center, 0.3, 1.8, 4.5, 1.5, 2.0, 50, 3, 6);

        ParticleUtil.spawnParticles(level, ParticleTypes.HEART, center.add(0, 0.5, 0), 16, 0.8);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.HAPPY_VILLAGER, center, 1.8, 30);

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.TOTEM_USE,             SoundSource.PLAYERS, 0.8f, 1.6f);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 0.9f);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_death.right_self_healed").withColor(0xFF334f23));
    }

    private void rightHandOthers(ServerLevel level, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, 30 * (int) Math.max(multiplier(caster) / 4, 1), 1.5f, true);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_death.no_target").withColor(0xFF334f23));
            return;
        }

        float heal = target.getMaxHealth() * 0.25f;
        target.heal(heal);

        Vec3 targetCenter = target.position().add(0, 1, 0);
        Vec3 targetFeet   = target.position();
        Vec3 casterHand   = caster.position().add(0, 1.2, 0);

        ParticleUtil.drawParticleLine(level, ParticleTypes.TOTEM_OF_UNDYING, casterHand, targetCenter, 0.25, 1);
        ParticleUtil.drawParticleLine(level, ParticleTypes.TOTEM_OF_UNDYING, casterHand, targetCenter, 0.25, 1, 0.15);
        ParticleUtil.drawParticleLine(level, ParticleTypes.HAPPY_VILLAGER,   casterHand, targetCenter, 0.40, 1, 0.08);

        ParticleUtil.spawnSphereParticles(level, ParticleTypes.TOTEM_OF_UNDYING, targetCenter, 1.0, 60, 0.3f);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.TOTEM_OF_UNDYING, targetCenter, 2.2, 50, 0.2f);

        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, targetFeet,               2.5, 36);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, targetFeet.add(0, 1,  0), 2.0, 28);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, targetFeet.add(0, 2,  0), 1.5, 20);
        ParticleUtil.spawnCircleParticles(level, ParticleTypes.TOTEM_OF_UNDYING, targetFeet.add(0, 3,  0), 0.8, 12);

        ParticleUtil.createParticleCocoons(ParticleTypes.TOTEM_OF_UNDYING,
                new Location(target.position(), level), 0.5, 1.4, 3.5, 1.5, 2.0, 50, 2, 8);

        ParticleUtil.createParticleSpirals(level, ParticleTypes.TOTEM_OF_UNDYING, targetCenter, 0.3, 1.8, 4.5, 1.5, 2.0, 50, 3, 6);

        ParticleUtil.spawnParticles(level, ParticleTypes.HEART, targetCenter.add(0, 0.5, 0), 16, 0.8);
        ParticleUtil.spawnSphereParticles(level, ParticleTypes.HAPPY_VILLAGER, targetCenter, 1.8, 30);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.TOTEM_USE,             SoundSource.PLAYERS, 0.8f, 1.6f);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 0.9f);

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_death.right_others_healed").withColor(0xFF334f23));
    }
}