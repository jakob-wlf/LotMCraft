package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
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

public class HandOfLifeAbility extends SelectableAbility {

    private static final int WITHER_DURATION  = 20 * 10;
    private static final int EFFECT_AMPLIFIER = 1;

    private static final Map<UUID, UUID> activeMarks = new ConcurrentHashMap<>();

    public HandOfLifeAbility(String id) {
        super(id, 60f);
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
                "ability.lotmcraft.hand_of_life.right_self",
                "ability.lotmcraft.hand_of_life.right_others"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        switch (abilityIndex) {
            case 0 -> rightHandSelf(serverLevel, entity);
            case 1 -> rightHandOthers(serverLevel, entity);
        }
    }

    private void rightHandSelf(ServerLevel level, LivingEntity caster) {
        float heal = caster.getMaxHealth() * 0.25f;
        float newHealth = Math.min(caster.getHealth() + heal, caster.getMaxHealth());
        caster.setHealth(newHealth);


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

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_life.right_self_healed").withColor(0xFF334f23));
    }

    private void rightHandOthers(ServerLevel level, LivingEntity caster) {
        LivingEntity target = AbilityUtil.getTargetEntity(caster, (int) (30 * multiplier(caster)), 1.5f, true);
        if (target == null) {
            AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_life.no_target").withColor(0xFF334f23));
            return;
        }

        float heal = target.getMaxHealth() * 0.25f;
        float newHealth = Math.min(target.getHealth() + heal, target.getMaxHealth());
        target.setHealth(newHealth);

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

        AbilityUtil.sendActionBar(caster, Component.translatable("ability.lotmcraft.hand_of_life.right_others_healed").withColor(0xFF334f23));
    }
}