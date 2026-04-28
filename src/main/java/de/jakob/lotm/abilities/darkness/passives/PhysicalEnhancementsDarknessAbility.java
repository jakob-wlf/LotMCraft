package de.jakob.lotm.abilities.darkness.passives;

import de.jakob.lotm.abilities.PhysicalEnhancementsAbility;
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private boolean isNocturnal(LivingEntity entity) {
        return entity.level().isNight()
                || ConcealedDomainEntity.ENTITIES_INSIDE_DOMAIN.contains(entity.getUUID());
    }

    @Override
    protected List<PhysicalEnhancement> getEnhancementsForSequence(int sequenceLevel, LivingEntity entity) {

        int strengthModifier = 0;
        int resistanceModifier = 0;
        int speedModifier = 0;

        if (isNocturnal(entity)) {
            strengthModifier = 2;
            resistanceModifier = 2;
            speedModifier = 2;
        }

        return switch (sequenceLevel) {
            case 9 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, strengthModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, speedModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1)
            );

            case 8 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 1 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 1 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 5),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );
            case 7 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 6),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 1)
            );

            case 6 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 1 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 7),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 5 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 2 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 2 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 2 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 9),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 2)
            );

            case 4 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 7 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 16),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)
            );

            case 3 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 3 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 8 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 4 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 17),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 3)
            );

            case 2 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 4 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 11 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 25),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)
            );

            case 1 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 5 + strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 13 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 5 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 32),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 4)
            );

            case 0 -> List.of(
                    new PhysicalEnhancement(EnhancementType.STRENGTH, 6+ strengthModifier),
                    new PhysicalEnhancement(EnhancementType.NIGHT_VISION, 1),
                    new PhysicalEnhancement(EnhancementType.RESISTANCE, 15 + resistanceModifier),
                    new PhysicalEnhancement(EnhancementType.SPEED, 6 + speedModifier),
                    new PhysicalEnhancement(EnhancementType.HEALTH, 45),
                    new PhysicalEnhancement(EnhancementType.REGENERATION, 6)
            );

            default -> List.of();
        };
    }

    @Override
    protected int getCurrentSequenceLevel(LivingEntity entity) {
        return BeyonderData.getSequence(entity);
    }
}
