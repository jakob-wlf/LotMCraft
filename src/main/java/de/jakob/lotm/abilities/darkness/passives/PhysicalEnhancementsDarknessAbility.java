package de.jakob.lotm.abilities.darkness.passives;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class PhysicalEnhancementsDarknessAbility extends PhysicalEnhancementsAbility {

    public PhysicalEnhancementsDarknessAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "darkness", 9
        ));
    }

    @Override
    public List<PhysicalEnhancement> getEnhancements() {
        return List.of();
    }

    @Override
    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel, LivingEntity entity) {

        int strengthModifier = 0;
        int resistanceModifier = 0;
        int speedModifier = 0;

        if(entity.level().isNight()) {
            strengthModifier = 1;
            resistanceModifier = 1;
            speedModifier = 2;
        }

        return switch (sequenceLevel) {
            case 9 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 8, 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 1 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 4),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 1)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 2)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 3 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 10),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 2)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 3 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 11),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 3)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 13),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 3)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 4 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 16),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 4)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 5 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 20),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 5),
                    new PhysicalEnhancement(EnhancementType.FIRE_RESISTANCE, 5)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(net.minecraft.world.entity.LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}