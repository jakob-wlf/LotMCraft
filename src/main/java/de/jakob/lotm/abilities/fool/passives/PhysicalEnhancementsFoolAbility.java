package de.jakob.lotm.abilities.fool.passives;

import de.jakob.lotm.abilities.AbilityItemHandler;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.util.helper.marionettes.MarionetteComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PhysicalEnhancementsFoolAbility extends PassiveAbilityItem {

    private final HashMap<Integer, List<MobEffectInstance>> effectsPerSequence = new HashMap<>();

    public PhysicalEnhancementsFoolAbility(Properties properties) {
        super(properties);

        initEffects();
    }

    private void initEffects() {
        effectsPerSequence.put(9, List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 0, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(8, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 0, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 1, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(6, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 1, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(5, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 1, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 4, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 1, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(4, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 9, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 2, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(3, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 2, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 10, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 2, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(2, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 12, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 4, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

        effectsPerSequence.put(1, List.of(
                new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 6, 3, false, false, false),
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 6, 4, false, false, false),
                new MobEffectInstance(MobEffects.SATURATION, 20 * 6, 5, false, false, false),
                new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 6, 15, false, false, false),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 30, 4, false, false, false),
                new MobEffectInstance(MobEffects.JUMP, 20 * 6, 1, false, false, false)
        ));

    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "fool", 9
        ));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);

        if (sequence < 0 || sequence > 9) {
            return;
        }

        List<MobEffectInstance> effects = getEffectsForSequence(sequence);
        applyPotionEffects(entity, effects);
    }

    @Override
    public boolean shouldApplyTo(LivingEntity entity) {
        MarionetteComponent marionetteComponent = entity.getData(ModAttachments.MARIONETTE_COMPONENT.get());
        if(marionetteComponent.isMarionette() && entity.level() instanceof ServerLevel serverLevel) {
            LivingEntity controller = getControllingEntity(marionetteComponent, serverLevel);
            if (controller != null) {
                if(((PuppeteeringEnhancementsAbility) PassiveAbilityHandler.PUPPETEERING_ENHANCEMENTS.get()).shouldApplyTo(controller)) {
                    return true;
                }
            }
        }
        return super.shouldApplyTo(entity);
    }

    private LivingEntity getControllingEntity(MarionetteComponent marionetteComponent, ServerLevel level) {
        if(!marionetteComponent.isMarionette()) {
            return null;
        }

        if(marionetteComponent.getControllerUUID().isEmpty()) {
            return null;
        }

        UUID controllerUUID = UUID.fromString(marionetteComponent.getControllerUUID());
        Entity controller = level.getEntity(controllerUUID);
        if(controller == null) {
            for(ServerLevel l : level.getServer().getAllLevels()) {
                if (l == level)
                    continue;

                controller = l.getEntity(controllerUUID);
            }
        }

        return controller instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private List<MobEffectInstance> getEffectsForSequence(int sequence) {
        if (effectsPerSequence.containsKey(sequence)) {
            return effectsPerSequence.get(sequence);
        } else {
            for (int i = sequence; i < 10; i++) {
                if (effectsPerSequence.containsKey(i)) {
                    return effectsPerSequence.get(i);
                }
            }

            return List.of();
        }
    }
}